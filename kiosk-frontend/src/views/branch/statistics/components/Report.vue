<script setup>

import { ref, computed } from "vue";
import ChartDataLabels from "chartjs-plugin-datalabels";
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

    ArcElement,

    ChartDataLabels

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



/*
==========================
결제 / 할인 / 마일리지
==========================
*/

const paymentStatistics = computed(()=>{

    return [

        {
            title:"총 결제금액",
            value:
                props.statistics?.totalPaymentAmount || 0
        },

        {
            title:"쿠폰 할인금액",
            value:
                props.statistics?.couponDiscountAmount || 0
        },

        {
            title:"포인트 사용금액",
            value:
                props.statistics?.pointAmount || 0
        },

        {
            title:"최종 결제금액",
            value:
                props.statistics?.finalPaymentAmount || 0
        }

    ];

});





function formatMoney(value){


    if(!value)

        return "0원";


    return Number(value)

        .toLocaleString()

        +"원";

}

function categoryName(value){

const category = {

    RENT:"임대료",

    UTILITY:"공과금",

    LABOR:"인건비",

    SUPPLIES:"비품",

    MAINTENANCE:"유지보수",

    INGREDIENTS:"재료비",

    INSURANCE:"보험",

    ETC:"기타"

};


return category[value] || value;

}



function paymentName(value){

const payment = {

    CARD:"카드",

    CASH:"현금",

    TRANSFER:"계좌이체"

};


return payment[value] || value;

}

const discountChart = computed(()=>{


    const coupon =
        props.statistics?.couponDiscountAmount || 0;


    const point =
        props.statistics?.pointAmount || 0;


    const total = coupon + point;



    return {


        labels:[

            "쿠폰 할인",

            "포인트 사용"

        ],



        datasets:[

            {

                data:[

                    coupon,

                    point

                ],



                backgroundColor:[

                    "#3B82F6",

                    "#F97316"

                ],


                borderWidth:3,


                borderColor:"#ffffff"


            }

        ],



        plugins:[ChartDataLabels]

    };


});

const discountOption = {


    responsive:true,


    maintainAspectRatio:false,


    plugins:{


        legend:{


            position:"bottom"


        },


        datalabels:{


            color:"#ffffff",


            font:{


                weight:"bold",

                size:14


            },


            formatter:(value, context)=>{


                const coupon =
                    props.statistics?.couponDiscountAmount || 0;


                const point =
                    props.statistics?.pointAmount || 0;



                const total = coupon + point;



                const percent =
                    total === 0
                    ? 0
                    : ((value / total) * 100)
                        .toFixed(1);



                return [

                    value.toLocaleString()+"원",

                    percent+"%"

                ];


            }


        }


    }


};

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
결제 / 할인 / 마일리지
===================== -->

<div 
    v-if="reportMenu==='payment'"
    class="payment-container"
>


    <div
        v-for="(item,index) in paymentStatistics"
        :key="index"
        class="payment-card"
    >

        <h3>
            {{item.title}}
        </h3>


        <p>
            {{formatMoney(item.value)}}
        </p>


    </div>


</div>

<div v-if="reportMenu==='payment'">

    <div class="payment-box">

        <Doughnut
            :data="discountChart"
            :options="discountOption"
        />

    </div>

</div>

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
지출 상세 정산 내역
</h3>


<table>

<thead>

<tr>

<th>
날짜
</th>

<th>
내용
</th>

<th>
분류
</th>

<th>
결제
</th>

<th>
금액
</th>

</tr>

</thead>



<tbody>


<tr
    v-for="item in statistics?.expenseDetail"
    :key="item.expenseId"
>


<td>
{{item.expenseDate}}
</td>


<td>
{{item.description}}
</td>


<td>
{{categoryName(item.expenseCategory)}}
</td>


<td>
{{paymentName(item.paymentMethod)}}
</td>


<td>
{{formatMoney(item.amount)}}
</td>


</tr>


</tbody>


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

    gap:15px;

    margin-bottom:30px;

}


.report-nav button{

    width:160px;

    min-height:60px;

    padding:12px 20px;

    border:none;

    border-radius:20px;

    background:#f1f5f9;

    cursor:pointer;

    font-size:15px;

    font-weight:bold;

    transition:.2s;

}


.report-nav button:hover{

    background:#e2e8f0;

}



.report-nav button.active{

    background:#2563eb;

    color:white;

}



/*
=========================
요약 카드
=========================
*/


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

    margin-bottom:15px;

    font-size:16px;

}



.summary-card p{

    font-size:28px;

    font-weight:bold;

    margin:0;

}



/*
=========================
결제/할인 카드
=========================
*/


/* =========================
   결제/할인/마일리지 카드
========================= */

.payment-card-container {

    display:grid;

    grid-template-columns: repeat(4, 1fr);

    gap:20px;

    width:100%;

    margin-bottom:40px;

}


.payment-card {


    background:white;

    border-radius:15px;

    padding:25px;

    height:130px;

    box-shadow:
    0 4px 12px rgba(0,0,0,0.08);

    display:flex;

    flex-direction:column;

    justify-content:center;


}



.payment-card h3 {


    margin:0 0 15px 0;

    color:#64748b;

    font-size:16px;

}



.payment-card p {


    margin:0;

    font-size:28px;

    font-weight:bold;

    color:#1e293b;

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

    border-radius:18px;

    padding:25px;

    box-shadow:

    0 4px 15px rgba(0,0,0,0.08);

    min-height:400px;

}



.chart-box h2{

    margin-bottom:20px;

    color:#334155;

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



td:nth-child(2){

    white-space:nowrap;

    overflow:hidden;

    text-overflow:ellipsis;

}



/*
=========================
결제 탭 전용
=========================
*/


.payment-chart{

    display:grid;

    grid-template-columns:

    repeat(2,1fr);

    gap:30px;

}



.payment-chart .chart-box{

    min-height:350px;

}



/*
=========================
반응형
=========================
*/


@media(max-width:1200px){


    .summary-card-container,
    .payment-card-container{

        grid-template-columns:
        repeat(2,1fr);

    }


}

.payment-box {


    width:350px;

    height:350px;

    margin:auto;

    display:flex;

    justify-content:center;

    align-items:center;


}

</style>