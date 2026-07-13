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

    <!-- 포장 시간 선택 모달 -->
    <div class="modal-overlay" v-if="isPackingModalOpen">
      <div class="modal-content packing-modal">
        <h2>🛍️ 이동(포장) 시간을 선택해주세요</h2>
        <p class="modal-subtitle">이동 시간에 맞춰 드라이아이스를 준비해 드립니다.</p>
        
        <div class="packing-options">
          <button class="packing-btn" @click="selectPackingTime(1)">
            <span class="time-text">10분 이하</span>
            <span class="dry-ice-text">(드라이아이스 1개)</span>
          </button>
          <button class="packing-btn" @click="selectPackingTime(2)">
            <span class="time-text">30분 이하</span>
            <span class="dry-ice-text">(드라이아이스 2개)</span>
          </button>
          <button class="packing-btn" @click="selectPackingTime(3)">
            <span class="time-text">30분 초과</span>
            <span class="dry-ice-text">(드라이아이스 3개)</span>
          </button>
        </div>

        <button class="btn-close" @click="isPackingModalOpen = false">취소</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useBasketStore } from '@/stores/customer/basket'

const router = useRouter()
const basketStore = useBasketStore()

const isPackingModalOpen = ref(false)

const handleOrderType = (type) => {
  basketStore.setOrderType(type)
  
  if (type === 'TOGO') {
    isPackingModalOpen.value = true
  } else {
    // 먹고가기일 경우 드라이아이스 0개
    basketStore.setDryIceCount(0)
    router.push('/menu')
  }
}

const selectPackingTime = (dryIceCount) => {
  basketStore.setDryIceCount(dryIceCount)
  isPackingModalOpen.value = false
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

/* 모달 스타일 */
.modal-overlay {
  position: fixed;
  top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  padding: 30px;
  border-radius: 15px;
  text-align: center;
  width: 500px;
  max-width: 90%;
}

.packing-modal h2 {
  color: #ff7c98;
  margin-bottom: 10px;
}

.modal-subtitle {
  color: #666;
  margin-bottom: 25px;
}

.packing-options {
  display: flex;
  flex-direction: column;
  gap: 15px;
  margin-bottom: 25px;
}

.packing-btn {
  background: #f8f9fa;
  border: 2px solid #e9ecef;
  padding: 20px;
  border-radius: 10px;
  font-size: 1.2rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
  cursor: pointer;
  transition: 0.2s;
}

.packing-btn:hover {
  border-color: #ff7c98;
  background: #fff0f3;
  color: #ff7c98;
}

.dry-ice-text {
  font-size: 1rem;
  color: #888;
}

.packing-btn:hover .dry-ice-text {
  color: #ff7c98;
}

.btn-close {
  background: #888;
  color: white;
  border: none;
  padding: 12px 30px;
  border-radius: 8px;
  font-size: 1.1rem;
  cursor: pointer;
}

.btn-close:hover {
  background: #666;
}
</style>