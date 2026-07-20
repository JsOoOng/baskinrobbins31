<script setup>

import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import api from '@/api/axios'


const route = useRoute()

const router = useRouter()



const goBack = () => {

    router.push('/branch/staff')

}

const staffId =
    route.params.staffId



const histories = ref([])



const getHistory = async()=>{

try{


    const now = new Date()


    const response =
    await api.get(
        `/branch/parttime/staff/${staffId}/history`,
        {
            params:{
                year: now.getFullYear(),
                month: now.getMonth() + 1
            }
        }
    )


    console.log(
        '근무 이력',
        response.data
    )


    histories.value =
        response.data



}catch(error){


    console.error(error)

    alert(
        '근무 이력 조회 실패'
    )

}

}


const changeStatus = async(history)=>{

try{

    await api.patch(

        `/branch/parttime/history/${history.historyId}/status`,

        null,

        {
            params:{
                status:
                    history.workStatus
            }
        }

    )


    alert(
        '상태 변경 완료'
    )


}catch(error){

    console.error(error)

    alert(
        '변경 실패'
    )

}

}




const changeHoliday = async(history)=>{


    try{


        await api.patch(

            `/branch/parttime/history/${history.historyId}/holiday`,

            {

                isHoliday:
                    history.isHoliday

            }

        )


    }catch(error){

        console.error(error)

    }


}





onMounted(()=>{

    getHistory()

})


</script>



<template>


<div class="container">


<div class="box">


<h1>
근무 이력
</h1>



<div
v-if="histories.length===0"
>
근무 기록이 없습니다.
</div>

<button
    class="back-button"
    @click="goBack"
>
    ← 돌아가기
</button>

<div
v-for="history in histories"
:key="history.historyId"
class="history-card"
>


<h3>
{{ history.workDate }}
</h3>


<p>
요일 :
{{ history.dayOfWeek }}
</p>


<p>
시간 :

{{ history.startTime }}

~

{{ history.endTime }}

</p>



<label>

공휴일

<input
type="checkbox"
v-model="history.isHoliday"
@change="changeHoliday(history)"
/>

</label>



<select
v-model="history.workStatus"
@change="changeStatus(history)"
>


<option value="SCHEDULED">
예정
</option>


<option value="COMPLETED">
완료
</option>


<option value="ABSENT">
결근
</option>


<option value="LATE">
지각
</option>


<option value="LEAVE">
휴가
</option>


</select>



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



.box{

width:600px;

margin:auto;

background:white;

padding:40px;

border-radius:15px;

}



h1{

text-align:center;

}



.history-card{

padding:20px;

margin-bottom:15px;

border:1px solid #ddd;

border-radius:10px;

}



select{

width:100%;

padding:10px;

margin-top:15px;

}



</style>