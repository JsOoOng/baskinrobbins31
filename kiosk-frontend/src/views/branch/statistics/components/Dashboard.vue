<!--
  [화면 흐름 안내] Dashboard
  역할: 지점 운영에서 사용자가 보는 화면이다.
  진입: 상위 라우트 또는 부모 컴포넌트 -> 이 Vue 파일 렌더링
  데이터: 사용자 동작 -> props·Pinia·상위 화면 상태 -> 응답/상태 반영
  다음 이동: 현재 상태를 갱신하거나 부모 화면에 이벤트를 전달
-->
<script setup>


import {
    Bar,
    Doughnut
} from "vue-chartjs";


import {

    Chart as ChartJS,

    Title,
    Tooltip,
    Legend,

    CategoryScale,
    LinearScale,

    BarElement,

    ArcElement

} from "chart.js";



import {
    computed
} from "vue";



ChartJS.register(

    Title,
    Tooltip,
    Legend,

    CategoryScale,
    LinearScale,

    BarElement,

    ArcElement

);





const props = defineProps({

    statistics:Object

});





// 오늘 매출
const todaySales = computed(()=>{


    return props.statistics?.totalSales ?? 0;


});




// 오늘 주문
const todayOrders = computed(()=>{


    return props.statistics?.orderCount ?? 0;


});








// 시간대별 매출 차트

const hourlySalesChart = computed(()=>{


    return {


        labels:

            props.statistics?.hourlySales
            ?.map(
                item=>item.hour+"시"
            )

            || [],



        datasets:[

            {

                label:"시간대별 매출",

                data:

                    props.statistics?.hourlySales
                    ?.map(
                        item=>item.sales
                    )

                    || [],


                backgroundColor:"#4F46E5",

                borderColor:"#4338CA",

                borderWidth:1,

                borderRadius:10


            }

            ]


    };


});









// 카테고리 판매 비중

const categoryChart = computed(()=>{


    return {


        labels:

            props.statistics?.categorySales
            ?.map(
                item=>item.categoryName
            )

            || [],




        datasets:[

{

    label:"판매 비중",

    data:

        props.statistics?.categorySales
        ?.map(
            item=>item.sales
        )

        || [],



    backgroundColor:[

        "#6366F1",

        "#22C55E",

        "#F59E0B",

        "#EF4444",

        "#06B6D4",

        "#A855F7"

    ],



    borderWidth:2

}

]


    };


});






function formatMoney(value){


    if(!value)

        return "0원";


    return Number(value)
        .toLocaleString()
        +"원";

}



</script>






<template>


<div class="dashboard">








    <!-- 카드 -->


    <div class="card-container">



        <div class="card">


            <h3>
                기간별 매출
            </h3>


            <p>
                {{ formatMoney(todaySales) }}
            </p>


        </div>






        <div class="card">


            <h3>
                기간별 주문 건수
            </h3>


            <p>

                {{ todayOrders }} 건

            </p>


        </div>




    </div>







    <!-- 그래프 -->


    <div class="chart-container">



        <div class="chart-box">


            <h2>
                기간 시간대별 매출
            </h2>



            <Bar

                v-if="hourlySalesChart"

                :data="hourlySalesChart"

            />



        </div>








        <div class="chart-box">


            <h2>
                카테고리별 판매 비중
            </h2>




            <Doughnut

                v-if="categoryChart"

                :data="categoryChart"

            />



        </div>



    </div>





</div>


</template>








<style scoped>



.dashboard{

    width:100%;

}




.card-container{

    display:flex;

    gap:20px;

    margin-bottom:30px;

}




.card{


    flex:1;

    background:white;

    padding:25px;

    border-radius:15px;

    box-shadow:0 3px 10px rgba(0,0,0,0.1);

}



.card h3{

    margin-bottom:15px;

}



.card p{

    font-size:28px;

    font-weight:bold;

}







.chart-container{


    display:grid;

    grid-template-columns:1fr 1fr;

    gap:30px;


}





.chart-box{


    background:white;

    padding:25px;

    border-radius:15px;


}


.chart-box h2{

    font-size:20px;

    margin-bottom:20px;

    color:#333;

}



.card:first-child{

    border-top:5px solid #4F46E5;

}



.card:nth-child(2){

    border-top:5px solid #22C55E;

}



.chart-box{

    min-height:350px;

}


</style>