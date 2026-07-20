<script setup>

import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import api from '@/api/axios'


const router = useRouter()


// 키오스크 목록
const kiosks = ref([])


// 로그인 사용자 정보
const user = JSON.parse(
    localStorage.getItem('branchUser')
)

// 뒤로가기
const goBack = () => {

    router.push('/branch/main')

}


// 키오스크 목록 조회
const loadKiosks = async () => {

    try {

        const response = await api.get(
            `/branch/kiosk/${user.storeId}`
        )


        kiosks.value = response.data


    } catch(e) {

        console.error(
            '키오스크 조회 실패',
            e
        )

    }

}



// 등록 화면 이동
const goRegister = () => {

    router.push(
        '/branch/kiosk/register'
    )

}



// 상태 변경
const changeStatus = async (
    kioskId,
    status
) => {


    try {


        await api.patch(
            `/branch/kiosk/${kioskId}/status`,
            {
                status: status
            }
        )


        alert(
            '키오스크 상태가 변경되었습니다.'
        )


        // 변경 후 다시 조회
        await loadKiosks()



    } catch(e) {


        console.error(
            '상태 변경 실패',
            e
        )


        alert(
            '상태 변경에 실패했습니다.'
        )


    }


}



// 상태 한글 표시
const getStatusText = (status) => {


    switch(status) {


        case 'ONLINE':
            return '운영중'


        case 'OFFLINE':
            return '오프라인'


        case 'MAINTENANCE':
            return '점검중'


        default:
            return status

    }

}



// 상태 CSS
const getStatusClass = (status) => {


    switch(status) {


        case 'ONLINE':
            return 'online'


        case 'OFFLINE':
            return 'offline'


        case 'MAINTENANCE':
            return 'maintenance'


        default:
            return ''

    }


}



onMounted(() => {

    loadKiosks()

})


</script>



<template>


<div class="kiosk-page">


    <!-- 상단 -->
    <div class="kiosk-header">

        <button 
            class="back-button"
            @click="goBack"
        >
            ← 메인으로 돌아가기
        </button>

        <h1>
            키오스크 관리
        </h1>


        <button
            class="register-btn"
            @click="goRegister"
        >
            키오스크 등록
        </button>


    </div>




    <!-- 키오스크 목록 -->

    <div class="kiosk-list">


        <div
            v-for="kiosk in kiosks"
            :key="kiosk.kioskId"
            class="kiosk-card"
        >


            <h2>
                {{ kiosk.kioskNumber }}번 키오스크
            </h2>



            <div class="info">


                <p>
                    장비 번호 :
                    {{ kiosk.deviceSerial }}
                </p>



                <p>
                    등록일 :
                    {{ kiosk.createdAt }}
                </p>



                <p>

                    상태 :

                    <span
                        :class="
                            getStatusClass(
                                kiosk.kioskStatus
                            )
                        "
                    >

                        {{
                            getStatusText(
                                kiosk.kioskStatus
                            )
                        }}

                    </span>

                </p>


            </div>




            <!-- 상태 변경 -->

            <div class="status-box">


                <button
                    @click="
                        changeStatus(
                            kiosk.kioskId,
                            'ONLINE'
                        )
                    "
                >
                    운영중
                </button>



                <button
                    @click="
                        changeStatus(
                            kiosk.kioskId,
                            'OFFLINE'
                        )
                    "
                >
                    OFFLINE
                </button>



                <button
                    @click="
                        changeStatus(
                            kiosk.kioskId,
                            'MAINTENANCE'
                        )
                    "
                >
                    점검중
                </button>



            </div>



        </div>



    </div>



</div>


</template>



<style scoped>


.kiosk-page {

    min-height:100vh;

    padding:30px;

    background:#f8f9fa;

    box-sizing:border-box;

}



.kiosk-header {

    display:flex;

    justify-content:space-between;

    align-items:center;

    margin-bottom:30px;

}



.kiosk-header h1 {

    margin:0;

}



.register-btn {

    padding:12px 20px;

    border:none;

    border-radius:8px;

    background:#333;

    color:white;

    cursor:pointer;

}



.kiosk-list {

    display:flex;

    flex-wrap:wrap;

    gap:25px;

}



.kiosk-card {

    width:320px;

    background:white;

    padding:25px;

    border-radius:15px;

    box-shadow:0 4px 12px rgba(0,0,0,0.08);

}



.kiosk-card h2 {

    margin-top:0;

}



.info p {

    margin:12px 0;

}



.online {

    color:green;

    font-weight:bold;

}



.offline {

    color:red;

    font-weight:bold;

}



.maintenance {

    color:#e67e22;

    font-weight:bold;

}



.status-box {

    display:flex;

    gap:8px;

    margin-top:20px;

}



.status-box button {

    flex:1;

    height:35px;

    border:none;

    border-radius:6px;

    cursor:pointer;

}


.kiosk-header > button {

margin-bottom:25px;

padding:10px 18px;

border:none;

border-radius:10px;

background:#222;

color:white;

font-size:14px;

font-weight:bold;

cursor:pointer;

transition:0.2s;


}

</style>