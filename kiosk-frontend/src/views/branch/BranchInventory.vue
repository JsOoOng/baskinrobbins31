<template>

<div class="menu-page">


    <button @click="goBack">
        ← 메인으로 돌아가기
    </button>


    <h2>상품 품절 관리</h2>


    <table>

        <thead>

            <tr>
                <th>상품명</th>
                <th>재고</th>
                <th>상태</th>
                <th>변경</th>
            </tr>

        </thead>


        <tbody>

            <tr
                v-for="menu in menus"
                :key="menu.storeProductId"
            >

                <td>
                    {{ menu.productName }}
                </td>


                <td>
                    {{ menu.currentStock }}
                </td>


                <td>

                    <span v-if="menu.soldOut">
                        품절
                    </span>

                    <span v-else>
                        판매중
                    </span>

                </td>


                <td>

                    <button
                        @click="changeProductSoldOut(menu)"
                    >

                        {{
                            menu.soldOut
                            ? '판매 재개'
                            : '품절 처리'
                        }}

                    </button>

                </td>


            </tr>


        </tbody>


    </table>



    <hr>



    <h2>맛 품절 관리</h2>



    <table>

        <thead>

            <tr>

                <th>맛</th>

                <th>통 수</th>

                <th>상태</th>

                <th>변경</th>

            </tr>

        </thead>



        <tbody>


            <tr
                v-for="flavor in flavors"
                :key="flavor.storeFlavorId"
            >


                <td>
                    {{ flavor.flavorName }}
                </td>


                <td class="container-count">


                    <button
                        class="count-btn"
                        @click="changeContainer(flavor, -1)"
                    >
                        -
                    </button>


                    <span>
                        {{ flavor.container }}
                    </span>


                    <button
                        class="count-btn"
                        @click="changeContainer(flavor, 1)"
                    >
                        +
                    </button>


                </td>


                <td>

                    <span v-if="flavor.soldOut">
                        품절
                    </span>


                    <span v-else>
                        판매중
                    </span>


                </td>



                <td>


                    <button
                        @click="changeFlavorSoldOut(flavor)"
                    >

                        {{
                            flavor.soldOut
                            ? '판매 재개'
                            : '품절 처리'
                        }}


                    </button>


                </td>


            </tr>


        </tbody>


    </table>



</div>


</template>




<script setup>

import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import api from '@/api/axios'

const router = useRouter()

let intervalId = null

const user =
JSON.parse(
    localStorage.getItem('branchUser')
)



// 상품 목록
const menus = ref([])



// 맛 목록
const flavors = ref([])





// 메인 이동
const goBack = () => {

    router.push('/branch/main')

}





// 상품 조회
const loadMenus = async()=>{


    try{


        const response =
        await api.get(
            `/branch/status/product/${user.storeId}`
        )


        menus.value =
        response.data



    }catch(e){

        console.error(
            '상품 조회 실패',
            e
        )

    }


}





// 맛 조회
const loadFlavors = async()=>{


    try{


        const response =
        await api.get(
            `/branch/status/flavor/${user.storeId}`
        )


        flavors.value =
        response.data



    }catch(e){


        console.error(
            '맛 조회 실패',
            e
        )


    }


}





// 상품 품절 변경
const changeProductSoldOut = async(menu)=>{


    try{


        await api.patch(

            `/branch/status/product/${menu.storeProductId}`,

            {

                soldOut:
                !menu.soldOut

            }

        )


        await loadMenus()



    }catch(e){


        console.error(e)


        alert(
            '상품 상태 변경 실패'
        )


    }


}





// 맛 품절 변경
const changeFlavorSoldOut = async(flavor)=>{


    try{


        await api.patch(

            `/branch/status/flavor/${flavor.storeFlavorId}`,

            {

                soldOut:
                !flavor.soldOut

            }

        )



        await loadFlavors()



    }catch(e){


        console.error(e)


        alert(
            '맛 상태 변경 실패'
        )


    }


}

const loadProducts = async () => {

    try {
        

        const response =
            await api.get(
                `/branch/status/product/${user.storeId}`
            )

        menus.value = response.data


    } catch(e){

        console.error(e)

    }

}



onMounted(()=>{


    loadMenus()

    loadFlavors()

    intervalId = setInterval(() => {

        loadProducts()

    }, 5000)

})

onUnmounted(() => {

    clearInterval(intervalId)

})


const changeContainer = async (flavor, amount) => {


    try {


        const response =
            await api.patch(
                `/branch/status/flavor/${flavor.storeFlavorId}/container`,
                {
                    amount: amount
                }
            )


        // 화면 즉시 변경
        flavor.container = response.data.container



    } catch(e){

        console.error(
            '재고 변경 실패',
            e
        )

        alert('재고 변경 실패')

    }

}

</script>

<style scoped>

.menu-page {

    height:100vh;

    overflow-y:auto;

    padding:30px;

    box-sizing:border-box;

}

table {

    width:100%;

    background:white;

    border-collapse:separate;

    border-spacing:0;

    border-radius:15px;

    overflow:hidden;

    box-shadow:0 4px 12px rgba(0,0,0,0.08);

    margin-bottom:20px;

}

.menu-page table{

    margin-bottom:30px;

}

/* 뒤로가기 버튼 */

.menu-page > button {

    margin-bottom:25px;

    padding:10px 18px;

    border:none;

    border-radius:10px;

    background:#222;

    color:white;

    font-size:14px;

    cursor:pointer;

    transition:0.2s;

}


.menu-page > button:hover {

    background:#555;

}

.menu-page > button:first-child {

    margin-top:10px;

    margin-bottom:25px;

}



h2 {

    margin:30px 0 20px;

    color:#333;

}





/* 테이블 영역 */

table {

    width:100%;

    background:white;

    border-collapse:separate;

    border-spacing:0;

    border-radius:15px;

    overflow:hidden;

    box-shadow:0 4px 12px rgba(0,0,0,0.08);

}





thead {

    background:#333;

    color:white;

}





th {

    padding:15px;

    font-size:14px;

}





td {

    padding:15px;

    text-align:center;

    border-bottom:1px solid #eee;

    color:#444;

}





tbody tr {

    transition:0.2s;

}





tbody tr:hover {

    background:#f7f7f7;

}





tbody tr:last-child td {

    border-bottom:none;

}





/* 상태 표시 */

.status {

    display:inline-block;

    min-width:80px;

    padding:6px 15px;

    border-radius:20px;

    font-size:13px;

    font-weight:bold;

}





/* 판매중 */

.sold-on {

    background:#d1e7dd;

    color:#0f5132;

}





/* 품절 */

.sold-out {

    background:#f8d7da;

    color:#842029;

}





/* 변경 버튼 */

td button {

    padding:8px 15px;

    border:none;

    border-radius:8px;

    background:#222;

    color:white;

    cursor:pointer;

    transition:0.2s;

}





td button:hover {

    background:#555;

}





/* 상품/맛 구분 */

hr {

    margin:30px 0;

    border:none;

    border-top:1px solid #ddd;

}




/* 반응형 */

@media(max-width:900px){


    .menu-page{

        padding:15px;

    }


    table{

        font-size:13px;

    }


    th,td{

        padding:10px;

    }


}

.container-count{

    display:flex;

    justify-content:center;

    align-items:center;

    gap:10px;

}



.container-count span{

    min-width:30px;

    text-align:center;

    font-weight:bold;

}


.count-btn{

    width:32px;

    height:32px;

    padding:0;

    display:flex;

    justify-content:center;

    align-items:center;


    border:none;

    border-radius:50%;


    background:#333;

    color:white;


    font-size:18px;

    font-weight:bold;


    line-height:1;

    cursor:pointer;


    transition:0.2s;

}



.count-btn:hover{

    background:#555;

}

</style>