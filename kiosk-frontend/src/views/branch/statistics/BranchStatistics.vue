<script setup>
import { ref, onMounted } from "vue";
import { useRouter } from "vue-router";
import axios from "@/api/axios";

import Dashboard from "./components/Dashboard.vue";
import Report from "./components/Report.vue";

const router = useRouter();

// 메인 이동
const goBack = () => {

    router.push('/branch/main')

}

/*
 * 현재 선택 메뉴
 */
const menu = ref("dashboard");



/*
 * 통계 데이터
 */
const statistics = ref(null);




/*
 * 로그인 사용자 정보
 */
const branchUser =
    JSON.parse(
        localStorage.getItem("branchUser")
    );



/*
 * 매장 ID
 */
const storeId =
    branchUser?.storeId
    ??
    branchUser?.user?.storeId;





/*
 * 기간 선택
 */
const startDate = ref("");

const endDate = ref("");





/*
 * 기본 날짜 설정
 * 최근 7일
 */
function setDefaultDate(){


    const today =
        new Date();



    endDate.value =
        today
        .toISOString()
        .substring(0,10);



    const before =
        new Date();


    before.setDate(
        today.getDate() - 7
    );



    startDate.value =
        before
        .toISOString()
        .substring(0,10);



}






/*
 * 통계 조회
 */
async function getStatistics(){


    try{


        const response =

            await axios.get(

                `/branch/statistics/${storeId}`,

                {

                    params:{


                        startDate:
                            startDate.value,


                        endDate:
                            endDate.value


                    }

                }

            );



        statistics.value =
            response.data;



    }catch(error){


        console.error(
            "통계 조회 실패",
            error
        );


    }


}





/*
 * 날짜 변경 조회
 */
function searchStatistics(){


    getStatistics();


}







/*
 * 좌측 메뉴 변경
 */
function changeMenu(type){


    menu.value =
        type;


}







onMounted(()=>{


    setDefaultDate();


    getStatistics();


});



</script>




<template>


<div class="statistics-page">





    <!-- 좌측 메뉴 -->

    <aside class="side-menu">

        <button @click="goBack">
        ← 메인으로 돌아가기
    </button>

        <button

            :class="{active:menu==='dashboard'}"

            @click="changeMenu('dashboard')"

        >

            대시보드


        </button>





        <button

            :class="{active:menu==='report'}"

            @click="changeMenu('report')"

        >

            통계/리포트


        </button>



    </aside>







    <!-- 우측 영역 -->

    <main class="content">






        <!-- 상단 제목 -->


        <div class="top-area">


            <h1>

                {{ 
                    menu === 'dashboard'
                    ?
                    '대시보드'
                    :
                    '통계 / 리포트'
                }}


            </h1>





            <!-- 기간 선택 -->


            <div class="date-box">


                <input

                    type="date"

                    v-model="startDate"

                />



                <span>

                    ~

                </span>




                <input

                    type="date"

                    v-model="endDate"

                />




                <button

                    @click="searchStatistics"

                >

                    조회


                </button>



            </div>



        </div>









        <!-- 대시보드 -->


        <Dashboard

            v-if="menu==='dashboard'"

            :statistics="statistics"

        />







        <!-- 리포트 -->


        <Report

            v-if="menu==='report'"

            :statistics="statistics"

        />






    </main>



</div>



</template>







<style scoped>


.statistics-page{


    display:flex;


    width:100%;


    height:100vh;


}






.side-menu{

    width:220px;

    flex-shrink:0;

    padding:20px;

}





.side-menu button{


    width:100%;


    padding:15px;


    margin-bottom:10px;


    border:none;


    cursor:pointer;


    border-radius:8px;


    font-size:16px;


}






.side-menu button.active{


    background:#333;


    color:white;


}






.content{

    flex:1;

    min-width:0;

    padding:40px;

    overflow-x:hidden;

    overflow-y:auto;

}






.top-area{


    display:flex;


    justify-content:space-between;


    align-items:center;


    margin-bottom:30px;


}






.top-area h1{


    margin:0;


}







.date-box{


    display:flex;


    align-items:center;


    gap:10px;


}






.date-box input{


    padding:8px;


    border:1px solid #ccc;


    border-radius:6px;


}






.date-box button{


    padding:9px 20px;


    border:none;


    border-radius:6px;


    background:#333;


    color:white;


    cursor:pointer;


}



</style>