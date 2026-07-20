<template>
  <div class="kiosk-home">
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
      <img src="@/assets/images/logo.png" alt="Baskin Robbins" class="logo" />
      <h1>{{ $t('배스킨라빈스에 오신 것을 환영합니다') }}</h1>
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

  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useBasketStore } from '@/stores/customer/basket'
import { useI18n } from 'vue-i18n'

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
</script>

<style scoped>
.lang-toggle-wrapper {
  position: absolute;
  top: 30px;
  right: 40px;
  display: flex;
  flex-direction: column;
  gap: 10px;
  width: 120px;
  z-index: 1000;
}

.lang-toggle-wrapper button {
  width: 100%;
  padding: 10px 0;
  border: 2px solid #ff7c98;
  border-radius: 20px;
  background-color: white;
  color: #ff7c98;
  font-size: 1.1rem;
  font-weight: bold;
  cursor: pointer;
  transition: 0.2s;
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 5px;
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
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100vh;
  background-color: #fff;
}

.logo-area {
  text-align: center;
  margin-bottom: 50px;
}

.button-group {
  display: flex;
  gap: 20px;
}

button.btn-here, button.btn-togo {
  width: 250px;
  height: 300px;
  border-radius: 20px;
  border: 2px solid #ff7c98;
  background-color: white;
  cursor: pointer;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  transition: 0.2s;
}

button.btn-here:hover, button.btn-togo:hover {
  background-color: #ff7c98;
  color: white;
}

.icon {
  font-size: 80px;
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
  font-size: 30px;
  font-weight: bold;
}

</style>