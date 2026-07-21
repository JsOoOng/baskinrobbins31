<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

const user = JSON.parse(localStorage.getItem('branchUser'))

const moveOrder = () => {
  router.push('/branch/order')
}

const goInventory = () => {
    router.push('/branch/inventory')
}

const goMenu = () => {
    router.push('/branch/menu')
}

const goStatistics = () => {
    router.push('/branch/statistics')
}

const goKiosk = () => {
    router.push('/branch/kiosk')
}

const goPartTime = () => {

router.push('/branch/staff')

}

const goWeekSchedule = () => {

router.push(
    '/branch/week-schedule'
)

}

const goExpenses = () => {
  router.push('/branch/expense')
}


const logout = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('branchUser')

  alert('로그아웃되었습니다.')

  router.push('/branch/login')
}
</script>

<template>
<div class="container">

    <div class="main-box">


        <h1>
            {{ user.storeName }}
        </h1>


        <p>
            관리자 : {{ user.name }}
        </p>


        <hr>


        <button @click="moveOrder">
            주문 관리
        </button>


        <button @click="goMenu">
            메뉴 관리
        </button>


        <button @click="goInventory">
            재고 관리
        </button>


        <button @click="goStatistics">
            통계 관리
        </button>

        <button @click="goKiosk">
            키오스크 관리
        </button>

        <button @click="goPartTime">
            알바 관리
        </button>

        <button @click="goWeekSchedule">
            한 주 스케줄 확인
        </button>

        <button @click="goExpenses">
            지출 입력
        </button>


        <hr>


        <button @click="logout">
            로그아웃
        </button>


    </div>

</div>
</template>

<style scoped>


.container{

    min-height:100vh;

    padding:40px;

    background:#f8f9fa;

    box-sizing:border-box;


    display:flex;

    justify-content:center;

    align-items:flex-start;

}



.main-box{

    width:500px;

    margin-top:40px;

    background:white;

    padding:40px;

    border-radius:15px;


    box-shadow:
    0 4px 12px rgba(0,0,0,0.08);

    text-align:center;

}




h1{

    margin-bottom:15px;

    color:#333;

    font-size:32px;

}



p{

    color:#666;

    font-size:16px;

    margin-bottom:30px;

}




hr{

    border:none;

    border-top:1px solid #eee;

    margin:25px 0;

}




button{


    width:100%;

    padding:15px;

    margin:8px 0;


    border:none;

    border-radius:10px;


    background:#222;

    color:white;


    font-size:16px;

    font-weight:bold;


    cursor:pointer;


    transition:0.2s;

}



button:hover{

    background:#555;

    transform:translateY(-2px);

}




/* 로그아웃 버튼 */

button:last-child{


    background:#f8d7da;

    color:#842029;


}



button:last-child:hover{


    background:#f1b0b7;


}



</style>