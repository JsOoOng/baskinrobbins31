<script setup>

import { ref, computed } from "vue";

import {
    Line,
    Bar,
    Doughnut
} from "vue-chartjs";


import {
    Chart as ChartJS,
    Title,
    Tooltip,
    Legend,

    LineElement,
    PointElement,

    CategoryScale,
    LinearScale,

    BarElement,

    ArcElement

} from "chart.js";



ChartJS.register(

    Title,
    Tooltip,
    Legend,

    LineElement,
    PointElement,

    CategoryScale,
    LinearScale,

    BarElement,

    ArcElement

);




const props = defineProps({

    statistics:Object

});





// 현재 선택 메뉴

const reportMenu = ref("sales");





function changeReport(type){

    reportMenu.value = type;

}



/*
==========================
요일별 매출
==========================
*/

const dayChart = computed(()=>{


    return {


        labels:

            props.statistics?.dayOfWeekSales

            ?.map(

                item => {

                    switch(item.day){

                        case 1:
                            return "일요일";

                        case 2:
                            return "월요일";

                        case 3:
                            return "화요일";

                        case 4:
                            return "수요일";

                        case 5:
                            return "목요일";

                        case 6:
                            return "금요일";

                        case 7:
                            return "토요일";

                        default:
                            return "";

                    }

                }

            )

            || [],





        datasets:[

    {

        label:"요일별 매출",

        data:

            props.statistics?.dayOfWeekSales

            ?.map(
                item=>item.sales
            )

            || [],


        borderColor:"#4F46E5",

        backgroundColor:"#818CF8",

        borderWidth:3,

        tension:0.4


    }

]

    };


});





/*
==========================
매출 추이 그래프
==========================
*/


const salesTrendChart = computed(()=>{


    return {


        labels:

            props.statistics?.dailySales

            ?.map(

                item=>item.date

            )

            || [],




        datasets:[

{

    label:"매출",

    data:

        props.statistics?.dailySales

        ?.map(
            item=>item.sales
        )

        || [],


    borderColor:"#2563EB",

    backgroundColor:"rgba(37,99,235,0.2)",

    borderWidth:3,

    fill:true,

    tension:0.4


}

]

    };


});









/*
==========================
시간대 매출
==========================
*/


const hourlyChart = computed(()=>{


    return {


        labels:

            props.statistics?.hourlySales

            ?.map(

                item=>item.hour+"시"

            )

            || [],




        datasets:[

{

    label:"시간대 매출",

    data:

        props.statistics?.hourlySales

        ?.map(
            item=>item.sales
        )

        || [],


    backgroundColor:"#22C55E",

    borderRadius:8


}

]


    };


});









/*
==========================
카테고리 매출
==========================
*/


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

                label:"카테고리",

                data:

                    props.statistics?.categorySales

                    ?.map(

                        item=>item.sales

                    )

                    || [],

backgroundColor:[

"#3B82F6",

"#22C55E",

"#F97316",

"#A855F7",

"#EF4444",

"#14B8A6"

],


borderWidth:1

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


<div class="report">







<!-- 메뉴 -->

<nav class="report-nav">


<button

    :class="{active:reportMenu==='sales'}"

    @click="changeReport('sales')"

>
매출 및 추이 분석
</button>




<button

    :class="{active:reportMenu==='product'}"

    @click="changeReport('product')"

>
상품 및 옵션 현황
</button>




<button

    :class="{active:reportMenu==='payment'}"

    @click="changeReport('payment')"

>
결제/할인/마일리지
</button>




<button

    :class="{active:reportMenu==='expense'}"

    @click="changeReport('expense')"

>
지출 및 순이익 분석
</button>



</nav>










<!-- =====================
매출 분석
===================== -->


<section v-if="reportMenu==='sales'">



<h2>
매출 및 추이 분석
</h2>




<div class="summary-card">


<div>

<h3>
총 매출
</h3>

<p>
{{formatMoney(statistics?.totalSales)}}
</p>

</div>



<div>

<h3>
순매출
</h3>

<p>
{{formatMoney(statistics?.profit)}}
</p>

</div>



<div>

<h3>
주문 건수
</h3>

<p>
{{statistics?.orderCount}}건
</p>

</div>




<div>

<h3>
객단가
</h3>

<p>
{{formatMoney(statistics?.averageOrderPrice)}}
</p>

</div>


</div>






<div class="chart-row">


<div class="chart-box">

<h3>
시간대별 매출 추이
</h3>


<Bar

:data="hourlyChart"

/>


</div>





<div class="chart-box">

<h3>
요일별 매출 현황
</h3>


<Line

:data="dayChart"

/>


</div>


</div>





</section>









<!-- =====================
상품 현황
===================== -->


<section v-if="reportMenu==='product'">


<h2>
상품 및 옵션 현황
</h2>



<div class="chart-row">


<div class="table-box">


<h3>
베스트 판매 상품
</h3>



<table>


<tr>

<th>
순위
</th>

<th>
상품명
</th>

<th>
판매수량
</th>


</tr>



<tr

v-for="(item,index) in statistics?.topProducts"

:key="index"

>


<td>
{{index+1}}
</td>


<td>
{{item.productName}}
</td>


<td>
{{item.quantity}}
</td>


</tr>


</table>


</div>





<div class="chart-box">


<h3>
카테고리별 매출 비중
</h3>



<Doughnut

:data="categoryChart"

/>


</div>



</div>


</section>









<!-- =====================
지출 분석
===================== -->


<section v-if="reportMenu==='expense'">


<h2>
지출 및 순이익 분석
</h2>




<div class="summary-card">


<div>

<h3>
총 매출액
</h3>

<p>
{{formatMoney(statistics?.totalSales)}}
</p>

</div>




<div>

<h3>
운영 지출 합계
</h3>

<p>
{{formatMoney(statistics?.totalExpense)}}
</p>

</div>




<div>

<h3>
순이익
</h3>

<p>
{{formatMoney(statistics?.profit)}}
</p>

</div>


</div>





<div class="table-box">


<h3>
지출 비목별 상세 정산 내역
</h3>


<table>


<tr>

<th>
대분류
</th>


<th>
집행 금액
</th>


</tr>



<tr

v-for="item in statistics?.expenseCategory"

:key="item.category"

>


<td>
{{item.category}}
</td>


<td>
{{formatMoney(item.amount)}}
</td>


</tr>



</table>



</div>


</section>





</div>


</template>




<style scoped>


.report{

    width:100%;

    min-width:900px;

}





/*
=========================
상단 제목
=========================
*/

.report h1{

    margin-bottom:30px;

}





/*
=========================
탭 메뉴
=========================
*/


.report-nav{

    display:flex;

    gap:10px;

    margin-bottom:30px;

}



.report-nav button{

    width:150px;

    min-height:60px;

    border:none;

    background:#f3f3f3;

    cursor:pointer;

    border-radius:10px;

    font-size:15px;

}



.report-nav button.active{

    background:#333;

    color:white;

}






/*
=========================
요약 카드
=========================
*/


.summary-card{


    display:grid;


    grid-template-columns:

    repeat(4,1fr);


    gap:20px;


    margin-bottom:40px;


}



.summary-card div{


    background:white;


    padding:25px;


    border-radius:15px;


    box-shadow:

    0 3px 10px rgba(0,0,0,.08);


}



.summary-card h3{


    margin-bottom:15px;


}







/*
=========================
차트 영역
=========================
*/


.chart-row{


    display:grid;


    grid-template-columns:

    repeat(2,1fr);


    gap:30px;


    margin-bottom:40px;


}




.chart-box{


    background:white;


    padding:30px;


    border-radius:15px;


    min-height:400px;


}






/*
=========================
테이블 영역
=========================
*/


.table-box{


    background:white;


    padding:30px;


    border-radius:15px;


    overflow:hidden;


}





table{


    width:100%;


    border-collapse:collapse;


    table-layout:fixed;


}





th{


    background:#f5f5f5;


}





th,
td{


    padding:15px;


    text-align:center;


    border-bottom:

    1px solid #ddd;


}





/*
상품명 줄바꿈 방지
*/


td:nth-child(2){


    white-space:nowrap;


    overflow:hidden;


    text-overflow:ellipsis;


}

.report{

    width:100%;

}



.report-nav{

    display:flex;

    gap:15px;

    margin-bottom:30px;

}



.report-nav button{


    padding:12px 25px;

    border:none;

    border-radius:20px;

    background:#f1f5f9;

    cursor:pointer;

    font-weight:bold;


}



.report-nav button.active{


    background:#2563eb;

    color:white;


}





.summary-card-container{


    display:grid;

    grid-template-columns:
    repeat(4,1fr);

    gap:20px;

    margin-bottom:30px;


}





.summary-card{


    background:white;

    border-radius:15px;

    padding:25px;


    box-shadow:

    0 4px 12px rgba(0,0,0,0.08);


}



.summary-card h3{


    color:#64748b;

}



.summary-card p{


    font-size:28px;

    font-weight:bold;

    margin-top:10px;


}




.chart-box{


    background:white;

    border-radius:18px;

    padding:25px;


    box-shadow:

    0 4px 15px rgba(0,0,0,0.08);


}



.chart-box h2{


    margin-bottom:20px;

    color:#334155;


}


</style>