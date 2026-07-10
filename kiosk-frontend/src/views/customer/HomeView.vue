<template>
  <div class="home-container" @click="goToMenu">
    <h1>Baskin Robbins</h1>
    <p>주문하시려면 화면을 터치해 주세요 🍦</p>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

// 화면 이동을 도와주는 라우터 함수 꺼내기
const router = useRouter()

// 화면을 터치(클릭)했을 때 메뉴판 화면으로 이동하는 함수
const goToMenu = () => {
  router.push('/menu')
}
</script>

<style scoped>
.home-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100vh;
  background-color: #ffcce6; /* 배스킨라빈스 느낌의 핑크색 배경 */
  cursor: pointer;
}

h1 {
  font-size: 3rem;
  color: #004080;
  margin-bottom: 20px;
}

p {
  font-size: 1.5rem;
  color: #333;
}
</style>