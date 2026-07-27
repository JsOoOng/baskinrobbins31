<!--
  [화면 흐름 안내] StaffRegisterView
  역할: 지점 운영에서 사용자가 보는 화면이다.
  진입: /branch/staff/register -> 이 Vue 파일 렌더링
  데이터: 사용자 동작 -> @/api/axios -> 응답/상태 반영
  다음 이동: /branch/staff
-->
<script setup>

import { ref } from 'vue'
import { useRouter } from 'vue-router'
import api from '@/api/axios'


const router = useRouter()


const user =
    JSON.parse(
        localStorage.getItem('branchUser')
    )


const storeId = user.storeId

// 뒤로가기
const goBack = () => {

router.push('/branch/staff')

}

const staff = ref({

    name:'',
    hp:'',
    email:'',
    address:'',
    birthDate:'',
    healthCertEndDate:'',
    hourlyWage:null

})



const saveStaff = async()=>{


    try{


        await api.post(

            `/branch/parttime/stores/${storeId}/staff`,

            staff.value

        )


        alert(
            '알바생 등록 완료'
        )


        router.push(
            '/branch/staff'
        )


    }catch(error){


        console.error(
            error
        )


        alert(
            '등록 실패'
        )

    }


}



</script>



<template>


<div class="container">


<div class="register-box">


<h1>
알바생 등록
</h1>


<div class="form">


<label>
이름
</label>

<input
v-model="staff.name"
/>



<label>
전화번호
</label>

<input
v-model="staff.hp"
/>



<label>
이메일
</label>

<input
v-model="staff.email"
/>



<label>
주소
</label>

<input
v-model="staff.address"
/>



<label>
생년월일
</label>

<input
type="date"
v-model="staff.birthDate"
/>



<label>
보건증 만료일
</label>

<input
type="date"
v-model="staff.healthCertEndDate"
/>



<label>
시급
</label>

<input
type="number"
v-model="staff.hourlyWage"
/>



<button
@click="saveStaff"
>
등록
</button>


<button
class="back"
@click="goBack"
>
취소
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

}



.register-box{

width:500px;

margin:auto;

background:white;

padding:40px;

border-radius:15px;

box-shadow:
0 4px 12px rgba(0,0,0,0.08);

}



h1{

text-align:center;

margin-bottom:30px;

}



.form{

display:flex;

flex-direction:column;

}



label{

margin-top:12px;

margin-bottom:5px;

font-weight:bold;

}



input{

padding:12px;

border:1px solid #ddd;

border-radius:8px;

}



button{

margin-top:20px;

padding:13px;

border:none;

border-radius:10px;

background:#222;

color:white;

font-weight:bold;

cursor:pointer;

}



.back{

background:#777;

}



</style>