<script setup>

import { ref } from 'vue'

import { useRoute, useRouter } from 'vue-router'

import api from '@/api/axios'



const route = useRoute()

const router = useRouter()

// 뒤로가기
const goBack = () => {

router.push('/branch/staff')

}

const staffId = route.params.staffId



const now = new Date()


const year = ref(
    now.getFullYear()
)


const month = ref(
    now.getMonth() + 1
)



const salary = ref(null)



const user =
JSON.parse(
    localStorage.getItem('branchUser')
)



const payment = ref({

employeeId:user.employeeId,

paymentMethod:'TRANSFER',

description:'',

year:year.value,

month:month.value,

amount:null

})





const getSalary = async()=>{

try{

    const response =
    await api.get(

        `/branch/parttime/staff/${staffId}/salary`,

        {
            params:{

                year:
                    year.value,

                month:
                    month.value

            }

        }

    )


    console.log(
        '급여',
        response.data
    )


    salary.value =
        response.data



    // ⭐ 지급 데이터 저장
    payment.value.amount =
        response.data.totalSalary


    payment.value.year =
        year.value


    payment.value.month =
        month.value



    console.log(
        '지급 데이터',
        payment.value
    )


}catch(error){


    console.error(error)

    alert(
        '급여 조회 실패'
    )

}

}



console.log(
    '지급 데이터',
    payment.value
)

const paySalary = async()=>{

    if(salary.value.paid){

alert(
    '이미 지급된 급여입니다.'
)

return

}

if(
    payment.value.amount == null ||
    payment.value.amount <= 0
){

    alert(
        '먼저 급여를 조회해주세요.'
    )

    return

}


try{


    console.log(
        '전송 데이터',
        payment.value
    )


    await api.post(

        `/branch/parttime/staff/${staffId}/salary/pay`,

        payment.value

    )


    alert(
        '급여 지급 완료'
    )


    router.back()


}catch(error){


    console.error(error)

    alert(
        '급여 지급 실패'
    )

}


}


</script>





<template>


<h2>
    급여 관리
</h2>




<div>


    년

    <input
        type="number"
        v-model="year"
    >



    월

    <input
        type="number"
        v-model="month"
    >



    <button
        @click="getSalary"
    >

        조회

    </button>


</div>





<div
v-if="salary"
>


<p>

시급

{{ salary.hourlyWage?.toLocaleString() }}

원

</p>



<p>

총 근무시간

{{ salary.totalHours }}

시간

</p>



<p>

평일 급여

{{ salary.weekdayPay?.toLocaleString() }}

원

</p>



<p>

주말 급여

{{ salary.weekendPay?.toLocaleString() }}

원

</p>



<p>

공휴일 급여

{{ salary.holidayPay?.toLocaleString() }}

원

</p>



<h3>

총 급여

{{ salary.totalSalary?.toLocaleString() }}

원

</h3>

<p v-if="salary && salary.paid">

✅ 해당 월 급여 지급 완료

</p>

</div>






<select
v-model="payment.paymentMethod"
>


<option value="TRANSFER">

계좌이체

</option>


<option value="CASH">

현금

</option>


</select>





<input

v-model="payment.description"

placeholder="지급 내용"

/>



<button
    v-if="salary && !salary.paid"
    @click="paySalary"
>
    급여 지급
</button>


<button
    v-if="salary && salary.paid"
    disabled
>
    지급 완료
</button>




<button

class="back"

@click="goBack"

>

돌아가기

</button>



</template>

<style scoped>


.salary-container{


    width:600px;

    margin:40px auto;

    padding:40px;

    background:white;

    border-radius:15px;

    box-shadow:
    0 4px 12px rgba(0,0,0,0.1);

}



h1{

    text-align:center;

    margin-bottom:30px;

}



.search-box{

    display:flex;

    gap:10px;

    align-items:center;

    margin-bottom:25px;

}



input,
select{


    padding:10px;

    border:1px solid #ddd;

    border-radius:8px;

}



button{


    padding:12px;

    border:none;

    border-radius:10px;

    background:#222;

    color:white;

    cursor:pointer;

}



button:disabled{


    background:#aaa;

    cursor:not-allowed;

}



.salary-box{


    padding:20px;

    border:1px solid #ddd;

    border-radius:10px;

}



.payment-box{


    margin-top:20px;

}



.payment-box input,
.payment-box select{


    width:100%;

    margin-top:10px;

}



.back{


    width:100%;

    margin-top:15px;

    background:#666;

}



.empty{


    text-align:center;

    color:#777;

}


</style>