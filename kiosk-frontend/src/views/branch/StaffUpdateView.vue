<script setup>

import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import api from '@/api/axios'


const route = useRoute()

const router = useRouter()

// 뒤로가기
const goBack = () => {

router.push('/branch/staff')

}

const staffId =
    route.params.staffId



const staff = ref({

    name:'',
    hp:'',
    email:'',
    address:'',
    birthDate:'',
    healthCertEndDate:'',
    hourlyWage:0,
    status:''

})



const getStaff = async()=>{


    try{


        const response =
        await api.get(
            `/branch/parttime/staff/${staffId}`
        )


        staff.value =
            response.data


    }catch(error){

        console.error(error)

        alert(
            '직원 정보를 불러오지 못했습니다.'
        )

    }


}




const updateStaff = async()=>{


    try{


        await api.put(

            `/branch/parttime/staff/${staffId}`,

            staff.value

        )


        alert(
            '수정 완료'
        )


        router.push(
            '/branch/staff'
        )


    }catch(error){


        console.error(error)

        alert(
            '수정 실패'
        )

    }


}



onMounted(()=>{

    getStaff()

})


</script>


<template>


<div class="container">


<div class="box">


<h1>
직원 수정
</h1>



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



<label>
상태
</label>


<select
v-model="staff.status"
>

<option value="WORKING">
근무중
</option>

<option value="ON_LEAVE">
휴직
</option>

<option value="TERMINATED">
퇴사
</option>


</select>



<button
@click="updateStaff"
>
저장
</button>



<button
class="back"
@click="goBack"
>
취소
</button>



</div>


</div>


</template>


<style scoped>


.container{

min-height:100vh;

padding:40px;

background:#f8f9fa;

}



.box{

width:500px;

margin:auto;

background:white;

padding:40px;

border-radius:15px;

}



h1{

text-align:center;

}



label{

display:block;

margin-top:15px;

font-weight:bold;

}



input,
select{

width:100%;

padding:12px;

margin-top:5px;

border-radius:8px;

border:1px solid #ddd;

}



button{

width:100%;

padding:13px;

margin-top:20px;

border:none;

border-radius:10px;

background:#222;

color:white;

}



.back{

background:#777;

}



</style>