<script setup>

import { ref, onMounted } from 'vue'
import api from '@/api/axios'
import { useRouter } from 'vue-router'

const router = useRouter()

const staffs = ref([])

// 뒤로가기
const goBack = () => {

router.push('/branch/main')

}

const user =
    JSON.parse(
        localStorage.getItem('branchUser')
    )


const storeId = user.storeId



const getStaffList = async () => {

    try {

        const response =
            await api.get(
                `/branch/parttime/stores/${storeId}/staff`
            )


        console.log(
            '직원 목록:',
            response.data
        )


        staffs.value =
            response.data


    } catch(error) {

        console.error(
            '직원 조회 실패:',
            error
        )

        alert(
            '직원 정보를 불러오지 못했습니다.'
        )

    }

}



onMounted(() => {

    getStaffList()

})


</script>


<template>

<div class="container">


    <div class="staff-box">


        <h1>
            알바 관리
        </h1>
        <button
            class="register-button"
            @click="$router.push('/branch/staff/register')"
        >
            알바생 등록
        </button>

        <button 
        class="back-button"
        @click="goBack"
    >
        ← 메인으로 돌아가기
        </button>


        <hr>



        <div
            v-if="staffs.length === 0"
            class="empty"
        >
            등록된 알바생이 없습니다.
        </div>



        <div
            v-for="staff in staffs"
            :key="staff.staffId"
            class="staff-card"
        >


            <h3>
                {{ staff.name }}
            </h3>


            <p>
                연락처 :
                {{ staff.hp }}
            </p>


            <p>
                시급 :
                {{ staff.hourlyWage?.toLocaleString() }}
                원
            </p>


            <p>
                상태 :
                {{ staff.status }}
            </p>

            <button
                @click="$router.push(`/branch/staff/${staff.staffId}/update`)"
            >
                직원 정보 수정
            </button>

            <button
                @click="$router.push(`/branch/staff/${staff.staffId}/schedule`)"
            >
                스케줄 관리
            </button>

            <button
                @click="$router.push(`/branch/staff/${staff.staffId}/history`)"
            >
                근무 이력
            </button>

            <button
                @click="$router.push(`/branch/staff/${staff.staffId}/salary`)"
            >
                급여 관리
            </button>

        </div>



    </div>


</div>


</template>



<style scoped>


.container{

    min-height:100vh;

    padding:40px;

    background:#f8f9fa;

    box-sizing:border-box;

}



.staff-box{

    width:600px;

    margin:40px auto;

    background:white;

    padding:40px;

    border-radius:15px;

    box-shadow:
    0 4px 12px rgba(0,0,0,0.08);

}



h1{

    text-align:center;

    margin-bottom:20px;

}



hr{

    border:none;

    border-top:1px solid #eee;

    margin:25px 0;

}



.back-button{

    width:100%;

    padding:12px;

    border:none;

    border-radius:10px;

    background:#666;

    color:white;

    cursor:pointer;

}



.staff-card{

    padding:20px;

    margin-bottom:15px;

    border:1px solid #ddd;

    border-radius:10px;

}



.staff-card h3{

    margin-bottom:15px;

}



.staff-card p{

    color:#555;

}



.empty{

    text-align:center;

    color:#777;

}

.register-button{

width:100%;

padding:12px;

margin-bottom:15px;

border:none;

border-radius:10px;

background:#222;

color:white;

cursor:pointer;

}

</style>