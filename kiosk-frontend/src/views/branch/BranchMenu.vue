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

import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import api from '@/api/axios'



const router = useRouter()



const user =
JSON.parse(
    localStorage.getItem('user')
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





onMounted(()=>{


    loadMenus()

    loadFlavors()


})


</script>