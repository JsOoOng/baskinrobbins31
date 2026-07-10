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
// import { useBasketStore } from '@/stores/customer/basket' // 나중에 Pinia 스토어 연결 시 주석 해제

const router = useRouter()
// const basketStore = useBasketStore()

const handleOrderType = (type) => {
  // 1. 선택한 주문 타입(HERE or TOGO)을 전역 상태(Pinia)에 임시 저장합니다.
  // basketStore.setOrderType(type) 
  
  console.log(`선택하신 주문 타입은 ${type} 입니다.`)

  // 2. 메뉴판 화면(/menu)으로 부드럽게 이동합니다.
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

button {
  width: 250px;
  height: 300px;
  border-radius: 20px;
  border: 2px solid #ff7c98; /* 배스킨라빈스 핑크 */
  background-color: white;
  cursor: pointer;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  transition: 0.2s;
}

button:hover, button:active {
  background-color: #ff7c98;
  color: white;
}

.icon {
  font-size: 80px;
  margin-bottom: 20px;
}

.text {
  font-size: 30px;
  font-weight: bold;
}
</style>