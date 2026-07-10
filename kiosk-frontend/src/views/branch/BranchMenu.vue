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

<div class="menu-page">


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


                    <td class="flavor-name">
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


</div>


</template>
<style scoped>

.order-number{
    width:120px;
}


th:first-child,
td:first-child{
    width:90px;
    white-space:nowrap;
}


.status-waiting,
.status-preparing,
.status-completed,
.status-canceled{
    white-space:nowrap;
}


/* 전체 페이지 */
.container{

    display:flex;

    gap:30px;

    padding:30px;

    background:#f8f9fa;

    min-height:100vh;

    box-sizing:border-box;

    align-items:flex-start;

    overflow-y:auto;   /* 전체 스크롤 */
}



/* 왼쪽 영역 */
.left{

    flex:0 0 45%;

    min-width:450px;


    /* 제거 */
    /* max-height:calc(100vh - 60px); */
    /* overflow-y:auto; */


    background:white;

    padding:25px;

    border-radius:15px;

    box-shadow:0 4px 12px rgba(0,0,0,0.08);

    box-sizing:border-box;

}



/* 오른쪽 영역 */
.right{

    flex:1;

    min-width:500px;


    /* 제거 */
    /* max-height:calc(100vh - 60px); */
    /* overflow-y:auto; */


    background:white;

    padding:30px;

    border-radius:15px;

    box-shadow:0 4px 12px rgba(0,0,0,0.08);

    box-sizing:border-box;

}




h2{

    margin-bottom:20px;

    color:#333;

}



/* 테이블 */

table{

    width:100%;

    min-width:400px;

    table-layout:fixed;

    border-collapse:separate;

    border-spacing:0;

    overflow:hidden;

    border-radius:12px;

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

    padding:18px;

    text-align:center;

    font-size:15px;

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



/* 상태 배지 */

.status-waiting,
.status-preparing,
.status-completed,
.status-canceled{


    display:inline-block;

    min-width:80px;

    padding:6px 14px;

    border-radius:20px;

    font-size:13px;

    font-weight:bold;

    text-align:center;

}



.status-waiting{

    background:#fff3cd;

    color:#856404;

}



.status-preparing{

    background:#cfe2ff;

    color:#084298;

}



.status-completed{

    background:#d1e7dd;

    color:#0f5132;

}



.status-canceled{

    background:#f8d7da;

    color:#842029;

}



/* 상세 정보 */

.right p{

    font-size:16px;

    margin:12px 0;

}



hr{

    margin:20px 0;

    border:none;

    border-top:1px solid #eee;

}



select{

    padding:8px 12px;

    border-radius:8px;

    border:1px solid #ccc;

    margin-right:10px;

}



button{

    padding:9px 16px;

    border:none;

    border-radius:8px;

    background:#222;

    color:white;

    cursor:pointer;

    transition:0.2s;

}



button:hover{

    background:#555;

}



</style>