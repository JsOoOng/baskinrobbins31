<template>
  <div class="kiosk-home">
    <div class="kiosk-selector-wrapper">
      <select v-model="selectedKioskId" @change="handleKioskChange" class="kiosk-select">
        <option v-for="kiosk in kiosks" :key="kiosk.kioskId" :value="kiosk.kioskId">
          [{{ kiosk.storeName }}] {{ kiosk.kioskNumber }}번 키오스크
        </option>
      </select>
    </div>

    <!-- 다국어 선택기 (우측 상단) -->
    <div class="lang-toggle-wrapper">
      <button class="main-lang-btn" @click="isLangMenuOpen = !isLangMenuOpen">
        {{ currentLangLabel }} <span class="arrow">{{ isLangMenuOpen ? '▲' : '▼' }}</span>
      </button>
      
      <div v-if="isLangMenuOpen" class="lang-dropdown">
        <button v-if="$i18n.locale !== 'ko'" @click="changeLang('ko')">한국어</button>
        <button v-if="$i18n.locale !== 'en'" @click="changeLang('en')">English</button>
        <button v-if="$i18n.locale !== 'ja'" @click="changeLang('ja')">日本語</button>
      </div>
    </div>

    <div class="logo-area">
      <img v-if="resolvedBannerImage" :src="resolvedBannerImage" alt="Kiosk Banner" class="banner-image" />
      <template v-else>
        <img src="@/assets/images/logo.png" alt="Baskin Robbins" class="logo" />
        <h1>{{ $t('배스킨라빈스에 오신 것을 환영합니다') }}</h1>
      </template>
    </div>

    <div class="button-group">
      <button class="btn-here" @click="handleOrderType('HERE')">
        <span class="icon">🍽️</span>
        <span class="text">{{ $t('먹고가기') }}</span>
      </button>

      <button class="btn-togo" @click="handleOrderType('TOGO')">
        <span class="icon">🛍️</span>
        <span class="text">{{ $t('포장하기') }}</span>
      </button>
    </div>

    <!-- 비활성화 모달 -->
    <div v-if="showDisableModal" class="modal-overlay">
      <div class="modal-content">
        <h2>안내</h2>
        <p>현재 사용 중인 키오스크가 비활성화되었습니다.<br>기본 화면으로 이동합니다.</p>
        <button class="modal-close-btn" @click="showDisableModal = false">확인</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useBasketStore } from '@/stores/customer/basket'
import { useI18n } from 'vue-i18n'
import api from '@/api/axios'

const router = useRouter()
const basketStore = useBasketStore()
const { locale } = useI18n({ useScope: 'global' })

const isLangMenuOpen = ref(false)
const currentLangLabel = computed(() => {
  if (locale.value === 'ko') return '한국어'
  if (locale.value === 'en') return 'English'
  if (locale.value === 'ja') return '日本語'
  return '한국어'
})

const changeLang = (lang) => {
  locale.value = lang
  isLangMenuOpen.value = false
}

const handleOrderType = (type) => {
  basketStore.setOrderType(type)
  
  if (type === 'HERE') {
    basketStore.setDryIceCount(0)
    basketStore.setDryIceMins(0)
  }
  router.push('/menu')
}

// 키오스크 배너 관리
const kiosks = ref([])
const selectedKioskId = ref(Number(localStorage.getItem('kioskId')) || 1)
const bannerImageUrl = ref('')
const showDisableModal = ref(false)
let eventSource = null
let kioskEventSource = null

const resolvedBannerImage = computed(() => {
  if (!bannerImageUrl.value) return ''
  try {
    const filename = bannerImageUrl.value.split('/').pop()
    return new URL(`../../assets/images/banners/${filename}`, import.meta.url).href
  } catch(e) {
    return ''
  }
})

const loadBanner = async () => {
  try {
    const response = await api.get(`/api/kiosk-banners/${selectedKioskId.value}`)
    if (response.data && response.data.bannerImageUrl) {
      bannerImageUrl.value = response.data.bannerImageUrl
    } else {
      bannerImageUrl.value = ''
    }
  } catch(e) {
    bannerImageUrl.value = ''
  }
}

const connectSSE = () => {
  if (eventSource) {
    eventSource.close()
  }
  // Vite Proxy가 /proxy-api를 잡아서 백엔드로 전달
  eventSource = new EventSource(`/proxy-api/api/kiosk-banners/stream/${selectedKioskId.value}`)
  
  eventSource.addEventListener('BANNER_UPDATE', (event) => {
    console.log('Received Banner Update:', event.data)
    if (event.data === 'DEFAULT') {
      bannerImageUrl.value = ''
    } else {
      bannerImageUrl.value = event.data
    }
  })
  
  eventSource.onerror = (error) => {
    console.error('배너 SSE 연결 에러 발생 (브라우저가 자동 재연결 시도함):', error)
  }
}

const loadKiosks = async () => {
  try {
    const response = await api.get('/branch/kiosk/all')
    kiosks.value = response.data.filter(kiosk => kiosk.kioskStatus === 'ONLINE')
    
    // 현재 선택된 키오스크가 온라인 목록에 없다면 첫 번째 항목으로 변경
    if (!kiosks.value.find(k => k.kioskId === selectedKioskId.value) && kiosks.value.length > 0) {
      showDisableModal.value = true
      // 1번으로 돌아가되, 1번이 없다면 배열의 첫 번째 키오스크로 대체
      const fallback = kiosks.value.find(k => k.kioskId === 1) || kiosks.value[0]
      selectedKioskId.value = fallback.kioskId
      
      handleKioskChange() // BUG FIX: 폴백 후 배너 이미지 갱신을 위해 반드시 호출
    }
  } catch (e) {
    console.error('키오스크 목록 조회 실패', e)
  }
}

const connectKioskSSE = () => {
  if (kioskEventSource) {
    kioskEventSource.close()
  }
  kioskEventSource = new EventSource(`/proxy-api/branch/kiosk/stream/1`)
  
  kioskEventSource.addEventListener('KIOSK_UPDATE', () => {
    loadKiosks()
  })
  
  kioskEventSource.onerror = (error) => {
    console.error('키오스크 SSE 연결 에러 발생 (브라우저가 자동 재연결 시도함):', error)
  }
}

const handleKioskChange = () => {
  localStorage.setItem('kioskId', selectedKioskId.value)
  loadBanner()
  connectSSE()
}

onMounted(() => {
  loadKiosks()
  connectKioskSSE()
  loadBanner()
  connectSSE()
})

onUnmounted(() => {
  if (eventSource) {
    eventSource.close()
  }
  if (kioskEventSource) {
    kioskEventSource.close()
  }
})
</script>

<style scoped>
.lang-toggle-wrapper {
  position: absolute;
  top: 30px;
  right: 40px;
  display: flex;
  flex-direction: column;
  gap: 6px;
  width: 85px;
  z-index: 1000;
}

.lang-toggle-wrapper button {
  width: 100%;
  padding: 6px 0;
  border: 2px solid #ff7c98;
  border-radius: 14px;
  background-color: white;
  color: #ff7c98;
  font-size: 0.8rem;
  font-weight: bold;
  cursor: pointer;
  transition: 0.2s;
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 4px;
}

.lang-toggle-wrapper button:hover,
.lang-toggle-wrapper button.main-lang-btn {
  background-color: #ff7c98;
  color: white;
}

.lang-dropdown {
  display: flex;
  flex-direction: column;
  gap: 10px;
  animation: fadeIn 0.2s ease-out;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(-10px); }
  to { opacity: 1; transform: translateY(0); }
}

.kiosk-home {
  position: relative;
  width: 100%;
  height: 100vh;
  background-color: #fff;
  margin: 0;
  padding: 0;
  overflow: hidden;
}

.logo-area {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 80vh;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: #fdfdfd;
}

.button-group {
  position: absolute;
  top: 80vh;
  left: 0;
  width: 100%;
  height: 20vh;
  margin: 0;
  padding: 0;
}

button.btn-here, button.btn-togo {
  position: absolute;
  top: 50%;
  transform: translate(-50%, -50%);
  width: 160px;
  height: 110px;
  border-radius: 20px;
  border: 2px solid #ff7c98;
  background-color: white;
  cursor: pointer;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  transition: 0.2s;
  box-shadow: 0 4px 10px rgba(0,0,0,0.1);
}

button.btn-here {
  left: 33.33%;
}

button.btn-togo {
  left: 66.67%;
}

button.btn-here:hover, button.btn-togo:hover {
  background-color: #ff7c98;
  color: white;
}

.icon {
  font-size: 40px;
}
.touch-area {
  text-align: center;
}

.brand-name {
  font-size: 4rem;
  margin-bottom: 20px;
  font-weight: bold;
}

.touch-text {
  font-size: 2rem;
  /* 글씨가 깜빡거리는 애니메이션 효과 (키오스크 국룰!) */
  animation: blink 1.5s infinite; 
}

.text {
  font-size: 15px;
  font-weight: bold;
}

.kiosk-selector-wrapper {
  position: absolute;
  top: 30px;
  left: 40px;
  z-index: 1000;
}

.kiosk-select {
  padding: 4px 10px;
  font-size: 0.8rem;
  border-radius: 14px;
  border: 2px solid #ccc;
  background-color: white;
  cursor: pointer;
}

.banner-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  margin: 0;
  padding: 0;
  display: block;
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
}

.modal-content {
  background-color: white;
  padding: 40px;
  border-radius: 20px;
  text-align: center;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2);
}

.modal-content h2 {
  color: #ff7c98;
  margin-bottom: 20px;
  font-size: 2rem;
}

.modal-content p {
  font-size: 1.2rem;
  margin-bottom: 30px;
  line-height: 1.5;
}

.modal-close-btn {
  background-color: #ff7c98;
  color: white;
  border: none;
  border-radius: 10px;
  padding: 10px 30px;
  font-size: 1.2rem;
  font-weight: bold;
  cursor: pointer;
  transition: 0.2s;
}

.modal-close-btn:hover {
  background-color: #e06581;
}
</style>