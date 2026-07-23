<!--
  [화면 흐름 안내] BranchKiosk
  역할: 지점 운영에서 사용자가 보는 화면이다.
  진입: /branch/kiosk -> 이 Vue 파일 렌더링
  데이터: 사용자 동작 -> @/api/axios -> 응답/상태 반영
  다음 이동: /branch/main, /branch/kiosk/register
-->
<script setup>

import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import api from '@/api/axios'


const router = useRouter()


// 키오스크 목록
const kiosks = ref([])

// 배너 목록 및 매핑 정보
const banners = ref([])
const kioskBanners = ref({})


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
        const response = await api.get(`/branch/kiosk/${user.storeId}`)
        kiosks.value = response.data

        // 키오스크마다 현재 매핑된 배너 조회
        await Promise.all(kiosks.value.map(async (kiosk) => {
            try {
                const bRes = await api.get(`/api/kiosk-banners/${kiosk.kioskId}`)
                if (bRes.data && bRes.data.bannerId) {
                    kioskBanners.value[kiosk.kioskId] = bRes.data.bannerId
                } else {
                    kioskBanners.value[kiosk.kioskId] = 0 // 기본 화면
                }
            } catch(e) {
                kioskBanners.value[kiosk.kioskId] = 0 // 기본 화면
            }
        }))
    } catch(e) {
        console.error('키오스크 조회 실패', e)
    }
}

// 배너 목록 조회
const loadBanners = async () => {
    try {
        const response = await api.get(`/api/banners`)
        banners.value = response.data
    } catch(e) {
        console.error('배너 목록 조회 실패', e)
    }
}

// 배너 변경 저장
const saveBanner = async (kioskId) => {
    try {
        const bannerId = kioskBanners.value[kioskId]
        if (bannerId === '' || bannerId === undefined) {
            alert('배너를 선택해주세요.')
            return
        }
        await api.put(`/api/kiosk-banners/${kioskId}`, { bannerId: bannerId })
        alert('배너가 성공적으로 변경 및 즉시 반영되었습니다.')
    } catch(e) {
        console.error('배너 변경 실패', e)
        alert('배너 변경에 실패했습니다.')
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
    loadBanners()
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

            <!-- 배너 설정 -->
            <div class="banner-box">
                <p class="banner-title">메인 배너 설정</p>
                <div class="banner-controls">
                    <select v-model="kioskBanners[kiosk.kioskId]" class="banner-select">
                        <option :value="0">기본 화면</option>
                        <option v-for="banner in banners" :key="banner.id" :value="banner.id">
                            {{ banner.title }}
                        </option>
                    </select>
                    <button class="banner-save-btn" @click="saveBanner(kiosk.kioskId)">저장</button>
                </div>
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

.banner-box {
    margin-top: 15px;
    padding-top: 15px;
    border-top: 1px solid #eee;
}

.banner-title {
    font-size: 14px;
    font-weight: bold;
    color: #555;
    margin-bottom: 8px;
}

.banner-controls {
    display: flex;
    gap: 10px;
}

.banner-select {
    flex: 1;
    padding: 8px;
    border: 1px solid #ddd;
    border-radius: 6px;
    font-size: 14px;
}

.banner-save-btn {
    padding: 8px 15px;
    background: #4a90e2;
    color: white;
    border: none;
    border-radius: 6px;
    cursor: pointer;
    font-weight: bold;
}

.banner-save-btn:hover {
    background: #357abd;
}
</style>