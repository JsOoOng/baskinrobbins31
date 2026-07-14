<template>
  <div class="kiosk-home">
    <div class="logo-area">
      <img src="@/assets/images/logo.png" alt="Baskin Robbins" class="logo" />
      <h1>배스킨라빈스에 오신 것을 환영합니다</h1>
    </div>

    <div class="button-group">
      <button class="btn-here" @click="handleOrderType('HERE')">
        <span class="icon">🍽️</span>
        <span class="text">먹고가기</span>
      </button>

      <button class="btn-togo" @click="handleOrderType('TOGO')">
        <span class="icon">🛍️</span>
        <span class="text">포장하기</span>
      </button>
    </div>

  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { useBasketStore } from '@/stores/customer/basket'

const router = useRouter()
const basketStore = useBasketStore()

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