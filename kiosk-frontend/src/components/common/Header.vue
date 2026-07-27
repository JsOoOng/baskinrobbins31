<!--
  [화면 흐름 안내] Header
  역할: 공통 프론트엔드 화면에서 재사용되는 UI 컴포넌트다.
  진입: 상위 라우트 또는 부모 컴포넌트 -> 이 Vue 파일 렌더링
  데이터: 사용자 동작 -> props·Pinia·상위 화면 상태 -> 응답/상태 반영
  다음 이동: /kiosk
-->
<template>
  <header class="kiosk-header">
    <img src="@/assets/images/logo.png" alt="로고" class="logo" />
    
    <button class="home-reset-btn" @click="resetToHome">❌</button>
  </header>
</template>

<script setup>
import { useRouter } from 'vue-router';
import { useBasketStore } from '@/stores/customer/basket';

const router = useRouter();
const basketStore = useBasketStore();

const resetToHome = async () => {
  if(confirm('주문 내역을 모두 지우고 처음으로 돌아가시겠습니까?')) {
    await basketStore.clearBasket(); // 백엔드 쿠키 및 프론트 상태 초기화
    router.push('/kiosk');
  }
};
</script>

<style scoped>
.kiosk-header {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 80px; /* 헤더 높이 고정 */
  background-color: #ffffff;
  border-bottom: 3px solid #ff66b2;
}

.logo {
  height: 50px; /* 🌟 로고가 헤더 밖으로 안 삐져나가게 높이 제한! */
  object-fit: contain;
}
</style>