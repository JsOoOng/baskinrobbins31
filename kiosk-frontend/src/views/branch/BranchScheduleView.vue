<script setup>

import {
    ref,
    onMounted
} from 'vue'

import api from '@/api/axios'

import { useRouter } from 'vue-router'

const router = useRouter()

// 뒤로가기
const goBack = () => {

router.push('/branch/main')

}

const schedules = ref([])


const user =
JSON.parse(
    localStorage.getItem('branchUser')
)


const storeId =
user.storeId



const days = [

'MON',
'TUE',
'WED',
'THU',
'FRI',
'SAT',
'SUN'

]



const dayName = {

MON:'월요일',
TUE:'화요일',
WED:'수요일',
THU:'목요일',
FRI:'금요일',
SAT:'토요일',
SUN:'일요일'

}



const getSchedule = async()=>{


try{


const response =
await api.get(

`/branch/parttime/stores/${storeId}/schedule`

)



console.log(
'전체 스케줄',
response.data
)



schedules.value =
response.data



}catch(error){

console.error(error)

alert(
'스케줄 조회 실패'
)

}


}




const getDaySchedule = (day)=>{


return schedules.value.filter(

item =>
item.dayOfWeek === day

)


}




onMounted(()=>{


getSchedule()


})


</script>



<template>


<div class="schedule-box">


<h1>
주간 근무 스케줄
</h1>



<div
v-for="day in days"
:key="day"
class="day-box"
>


<h2>
{{dayName[day]}}
</h2>



<div
v-if="
getDaySchedule(day).length===0
"
>

근무자 없음

</div>



<div
v-for="staff in getDaySchedule(day)"
:key="staff.staffId"
class="staff-row"
>


<strong>
{{staff.staffName}}
</strong>


<span>

{{staff.startTime}}

~

{{staff.endTime}}

</span>


</div>



</div>



<button
@click="goBack"
>

돌아가기

</button>


</div>


</template>



<style scoped>


.schedule-box{

width:700px;

margin:40px auto;

background:white;

padding:30px;

border-radius:15px;

}


.day-box{

margin-bottom:20px;

border:1px solid #ddd;

padding:15px;

border-radius:10px;

}


.staff-row{

display:flex;

justify-content:space-between;

padding:10px;

background:#f5f5f5;

margin-top:8px;

border-radius:8px;

}



button{

width:100%;

padding:12px;

background:#222;

color:white;

border:none;

border-radius:10px;

}



</style>