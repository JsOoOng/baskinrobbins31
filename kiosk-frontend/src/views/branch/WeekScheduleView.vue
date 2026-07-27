<!--
  [화면 흐름 안내] WeekScheduleView
  역할: 지점 운영에서 사용자가 보는 화면이다.
  진입: /branch/staff/:staffId/schedule -> 이 Vue 파일 렌더링
  데이터: 사용자 동작 -> @/api/axios -> 응답/상태 반영
  다음 이동: /branch/staff
-->
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

const staffId = route.params.staffId



const days = [

    {
        code:'MON',
        name:'월요일'
    },

    {
        code:'TUE',
        name:'화요일'
    },

    {
        code:'WED',
        name:'수요일'
    },

    {
        code:'THU',
        name:'목요일'
    },

    {
        code:'FRI',
        name:'금요일'
    },

    {
        code:'SAT',
        name:'토요일'
    },

    {
        code:'SUN',
        name:'일요일'
    }

]



const schedules = ref([])



const createDefault = ()=>{


    schedules.value =
        days.map(day=>({

            dayOfWeek:
                day.code,

            startTime:'',
            
            endTime:'',

            isHoliday:false

        }))


}




const getSchedule = async()=>{


    try{


        const response =
        await api.get(

            `/branch/parttime/staff/${staffId}/schedule`

        )



        createDefault()



        response.data.forEach(saved=>{


            const target =
            schedules.value.find(
                item =>
                item.dayOfWeek
                === saved.dayOfWeek
            )



            if(target){

                target.scheduleId =
                    saved.scheduleId

                target.startTime =
                    saved.startTime

                target.endTime =
                    saved.endTime

                target.isHoliday =
                    saved.isHoliday

            }


        })



    }catch(error){


        console.error(error)


        alert(
            '스케줄 조회 실패'
        )

    }


}





const saveSchedule = async()=>{


    try{


        await api.put(

            `/branch/parttime/staff/${staffId}/schedule`,

            {

                schedules:
                    schedules.value

            }

        )


        alert(
            '스케줄 저장 완료'
        )


        router.back()



    }catch(error){


        console.error(error)


        alert(
            '저장 실패'
        )

    }


}





onMounted(()=>{


    getSchedule()


})


</script>



<template>


<div class="schedule-box">


<h1>
스케줄 관리
</h1>



<div
v-for="schedule in schedules"
:key="schedule.dayOfWeek"
class="schedule-row"
>


<h3>
{{ 
days.find(
d=>d.code===schedule.dayOfWeek
)?.name
}}
</h3>


<input
type="time"
v-model="schedule.startTime"
/>


~
 

<input
type="time"
v-model="schedule.endTime"
/>



<label>


<input

type="checkbox"

v-model="schedule.isHoliday"

/>

공휴일


</label>


</div>




<button
@click="saveSchedule"
>

저장

</button>



<button
class="back"
@click="goBack"

>

돌아가기

</button>


</div>



</template>



<style scoped>


.schedule-box{

width:600px;

margin:40px auto;

padding:30px;

background:white;

border-radius:15px;

}


.schedule-row{

padding:15px;

border-bottom:1px solid #ddd;

}


input{

margin:5px;

padding:8px;

}



button{

width:100%;

padding:12px;

margin-top:15px;

border:none;

border-radius:10px;

background:#222;

color:white;

cursor:pointer;

}



.back{

background:#666;

}


</style>