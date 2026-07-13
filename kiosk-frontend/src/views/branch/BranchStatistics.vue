<script setup>

import { ref,onMounted } from "vue";
import api from "@/api/axios";
import { useRouter } from 'vue-router'
const router = useRouter();
const storeId =
    JSON.parse(localStorage.getItem("user"))
    ?.storeId;

// 뒤로가기
const goBack = () => {

    router.push('/branch/main')

}


const statistics = ref({

    totalSales:0,
    orderCount:0,
    averageOrderPrice:0,
    totalExpense:0,
    profit:0,

    topProducts:[],
    categorySales:[],
    expenseCategory:[]

});



const getStatistics = async()=>{


    try{


        const response =
            await api.get(
                `/branch/statistics/${storeId}`
            );


        statistics.value =
            response.data;



    }catch(error){

        console.log(error);

    }


}




onMounted(()=>{

    getStatistics();

});


</script>



<template>


<div class="statistics-page">


<button @click="goBack">
        ← 메인으로 돌아가기
    </button>

<h1>
지점 통계 관리
</h1>



<div class="summary">


<div class="card">

<h3>
총 매출
</h3>

<p>
{{statistics.totalSales.toLocaleString()}}원
</p>

</div>



<div class="card">

<h3>
주문 건수
</h3>

<p>
{{statistics.orderCount}}건
</p>

</div>




<div class="card">

<h3>
객단가
</h3>

<p>
{{statistics.averageOrderPrice.toLocaleString()}}원
</p>

</div>





<div class="card">

<h3>
총 지출
</h3>

<p>
{{statistics.totalExpense.toLocaleString()}}원
</p>

</div>





<div class="card profit">

<h3>
순이익
</h3>

<p>
{{statistics.profit.toLocaleString()}}원
</p>

</div>


</div>





<div class="section">


<h2>
베스트 판매 상품 TOP
</h2>


<table>


<thead>

<tr>

<th>
상품명
</th>

<th>
판매량
</th>


</tr>

</thead>


<tbody>


<tr
v-for="item in statistics.topProducts"
:key="item.productName"
>


<td>
{{item.productName}}
</td>


<td>
{{item.quantity}}개
</td>


</tr>


</tbody>


</table>


</div>






<div class="section">


<h2>
지출 카테고리
</h2>


<table>


<thead>

<tr>

<th>
카테고리
</th>


<th>
금액
</th>


</tr>

</thead>


<tbody>


<tr
v-for="item in statistics.expenseCategory"
:key="item.category"
>


<td>
{{item.category}}
</td>


<td>
{{item.amount.toLocaleString()}}원
</td>


</tr>


</tbody>


</table>



</div>




</div>


</template>





<style scoped>


.statistics-page{

padding:30px;

background:#f8f9fa;

min-height:100vh;

}



.summary{

display:grid;

grid-template-columns:
repeat(5,1fr);

gap:20px;

}



.card{

background:white;

padding:25px;

border-radius:15px;

box-shadow:
0 4px 12px rgba(0,0,0,0.08);

}



.card h3{

color:#555;

}


.card p{

font-size:24px;

font-weight:bold;

}



.profit p{

color:#2e7d32;

}




.section{

margin-top:40px;

background:white;

padding:25px;

border-radius:15px;

}




table{

width:100%;

border-collapse:collapse;

}



th,td{

padding:15px;

border-bottom:1px solid #ddd;

text-align:center;

}


</style>