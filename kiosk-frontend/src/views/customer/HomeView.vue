<template>
  <div class="home-container" @click="goToMenu">
    <div class="touch-area">
      <h1 class="brand-name">Baskin Robbins</h1>
      <p class="touch-text">주문하시려면 화면을 터치해 주세요 👆</p>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router';

const router = useRouter();

const goToMenu = () => {
  router.push('/menu'); // 터치 시 메뉴 화면으로 이동!
};
</script>

<style scoped>
.home-container {
  display: flex;
  justify-content: center; /* 가로 중앙 정렬 */
  align-items: center;     /* 세로 중앙 정렬 */
  height: 100%;            /* main-content의 높이를 100% 꽉 채움 */
  width: 100%;
  background-color: #ff66b2; /* 베라 핑크색 */
  color: white;
  cursor: pointer;
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

@keyframes blink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.4; }
}
</style>