<script setup>

import { ref, onMounted } from 'vue'
import api from '@/api/axios'
import { useRouter } from 'vue-router'


const router = useRouter()


const user = JSON.parse(
    localStorage.getItem('user')
)


// 본사 전체 맛
const allFlavors = ref([])


// 우리 지점 등록 맛
const storeFlavors = ref([])



// 뒤로가기
const goBack = () => {

    router.push('/branch/main')

}



// 전체 맛 조회
const loadAllFlavors = async () => {

    try {

        const response =
            await api.get(
                '/branch/status/all-flavors'
            )


        allFlavors.value = response.data


    } catch(e){

        console.error(
            '전체 맛 조회 실패',
            e
        )

    }

}



// 지점 등록 맛 조회
const loadStoreFlavors = async () => {

    try {

        const response =
            await api.get(
                `/branch/status/flavor/${user.storeId}`
            )


        storeFlavors.value = response.data


    } catch(e){

        console.error(
            '지점 맛 조회 실패',
            e
        )

    }

}



// 현재 지점 사용 여부 확인
const isActive = (flavorId) => {


    return storeFlavors.value.some(
        item =>
        item.flavorId === flavorId
    )


}



// 맛 활성화 / 비활성화 변경
const toggleFlavor = async (flavor) => {


    const target =
        storeFlavors.value.find(
            item =>
            item.flavorId === flavor.flavorId
        )


    try {


        // 현재 미사용 -> 추가
        if(!target){


            await api.post(
                `/branch/status/flavor/${user.storeId}`,
                {
                    flavorId: flavor.flavorId
                }
            )


        }


        // 현재 사용중 -> 삭제
        else{


            await api.delete(
                `/branch/status/flavor/${target.storeFlavorId}`
            )


        }



        // 변경 후 다시 조회
        await loadStoreFlavors()



    } catch(e){


        console.error(
            '맛 상태 변경 실패',
            e
        )


    }

}




onMounted(async()=>{


    await loadAllFlavors()

    await loadStoreFlavors()


})


</script>



<template>


<div class="container">


    

        <button 
            class="back-button"
            @click="goBack"
        >
            ← 메인으로 돌아가기
        </button>


        <div class="menu-box">


            <h2>
                메뉴 관리
            </h2>


            <!-- 기존 메뉴 테이블 -->

        </div>



    <table>


        <thead>

            <tr>

                <th>
                    맛 번호
                </th>


                <th>
                    맛 이름
                </th>


                <th>
                    사용 여부
                </th>


            </tr>

        </thead>



        <tbody>


            <tr
                v-for="flavor in allFlavors"
                :key="flavor.flavorId"
            >


                <td>
                    {{ flavor.flavorId }}
                </td>


                <td>
                    {{ flavor.flavorName }}
                </td>


                <td>


                    <button
                        class="toggle"
                        :class="
                        isActive(flavor.flavorId)
                        ?
                        'on'
                        :
                        'off'
                        "
                        @click="toggleFlavor(flavor)"
                    >


                        {{
                            isActive(flavor.flavorId)
                            ?
                            'ON'
                            :
                            'OFF'
                        }}


                    </button>


                    </td>


            </tr>


        </tbody>


    </table>


</div>


</template>


<style scoped>


/* 전체 */

.container{

    padding:30px;

    background:#f8f9fa;

    min-height:100vh;

    box-sizing:border-box;

}



/* 뒤로가기 버튼 */

.back-button{

    margin-bottom:20px;

    padding:10px 18px;

    border:none;

    border-radius:8px;

    background:#222;

    color:white;

    font-weight:bold;

    cursor:pointer;

    transition:0.2s;

}


.back-button:hover{

    background:#555;

}



/* 제목 */

h2{

    margin-bottom:25px;

    color:#333;

}



/* 메뉴 카드 */

.menu-box{

    background:white;

    padding:30px;

    border-radius:15px;

    box-shadow:
    0 4px 12px rgba(0,0,0,0.08);

}



/* 테이블 */

table{

    width:100%;

    border-collapse:separate;

    border-spacing:0;

    overflow:hidden;

    border-radius:12px;

    table-layout:fixed;

}



thead{

    background:#333;

    color:white;

}



th{

    padding:18px;

    font-size:15px;

}



td{

    padding:16px;

    text-align:center;

    font-size:15px;

    border-bottom:1px solid #eee;

}



tbody tr{

    cursor:pointer;

    transition:0.2s;

}



tbody tr:hover{

    background:#f7f7f7;

}



tbody tr:last-child td{

    border-bottom:none;

}



/* 맛 이름 */

.flavor-name{

    text-align:left;

    padding-left:30px;

    font-weight:500;

}



/* ON/OFF 버튼 */

.toggle{

    width:80px;

    padding:8px 15px;

    border:none;

    border-radius:20px;

    font-size:14px;

    font-weight:bold;

    cursor:pointer;

    transition:0.2s;

}



/* 사용중 */

.on{

    background:#d1e7dd;

    color:#0f5132;

}



/* 미사용 */

.off{

    background:#f8d7da;

    color:#842029;

}



.toggle:hover{

    transform:translateY(-2px);

}



/* 검색창 추가 예정 대비 */

input{

    padding:10px 14px;

    border-radius:8px;

    border:1px solid #ccc;

    font-size:14px;

}



</style>