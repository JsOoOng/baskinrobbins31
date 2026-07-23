<!--
  [화면 흐름 안내] BranchKioskRegister
  역할: 지점 운영에서 사용자가 보는 화면이다.
  진입: /branch/kiosk/register -> 이 Vue 파일 렌더링
  데이터: 사용자 동작 -> @/api/axios -> 응답/상태 반영
  다음 이동: /branch/kiosk
-->
<script setup>

import { ref } from 'vue'
import { useRouter } from 'vue-router'
import api from '@/api/axios'


const router = useRouter()


const kioskNumber = ref('')
const deviceSerial = ref('')


const user = JSON.parse(
    localStorage.getItem('branchUser')
)



// 뒤로가기
const goBack = ()=>{

    router.push('/branch/kiosk')

}



// 등록
const registerKiosk = async()=>{


    if(!kioskNumber.value){

        alert('키오스크 번호를 입력해주세요.')
        return

    }


    if(!deviceSerial.value){

        alert('장비 시리얼 번호를 입력해주세요.')
        return

    }



    try{


        await api.post(
            '/branch/kiosk',
            {

                storeId:user.storeId,

                kioskNumber:
                    Number(kioskNumber.value),

                deviceSerial:
                    deviceSerial.value

            }
        )


        alert('키오스크가 등록되었습니다.')


        router.push(
            '/branch/kiosk'
        )


    }catch(e){


        console.error(
            '키오스크 등록 실패',
            e
        )


        alert(
            '키오스크 등록에 실패했습니다.'
        )


    }


}


</script>



<template>

<div class="register-page">


    <div class="header">


        <h1>
            키오스크 등록
        </h1>


        <button @click="goBack">
            돌아가기
        </button>


    </div>



    <div class="register-box">


        <div class="form-group">


            <label>
                키오스크 번호
            </label>


            <input
                type="number"
                v-model="kioskNumber"
                placeholder="예) 1"
            />


        </div>




        <div class="form-group">


            <label>
                장비 시리얼 번호
            </label>


            <input
                type="text"
                v-model="deviceSerial"
                placeholder="예) SN-A1B2C3D4"
            />


        </div>




        <button
            class="register-btn"
            @click="registerKiosk"
        >
            등록
        </button>



    </div>


</div>


</template>



<style scoped>


.register-page{

    padding:30px;

    min-height:100vh;

    background:#f8f9fa;

    box-sizing:border-box;

}



.header{

    display:flex;

    justify-content:space-between;

    align-items:center;

    margin-bottom:30px;

}



.header button{

    padding:10px 20px;

    border:none;

    border-radius:8px;

    cursor:pointer;

}




.register-box{

    width:500px;

    background:white;

    padding:30px;

    border-radius:15px;

    box-shadow:0 4px 12px rgba(0,0,0,0.08);

}



.form-group{

    display:flex;

    flex-direction:column;

    gap:10px;

    margin-bottom:20px;

}



.form-group label{

    font-weight:bold;

}



.form-group input{

    height:40px;

    padding:0 12px;

    border:1px solid #ddd;

    border-radius:8px;

    font-size:16px;

}



.register-btn{

    width:100%;

    height:45px;

    border:none;

    border-radius:8px;

    background:#333;

    color:white;

    cursor:pointer;

    font-size:16px;

}



</style>