<script setup>
import { RouterLink, RouterView } from 'vue-router';
import Header from '@/components/common/Header.vue';

// App.vue는 가장 큰 껍데기이므로 특별한 로직 없이 비워둡니다.
</script>

<template>
  <div class="app-container">
    <main class="main-content">
      <router-view />
    </main>
  </div>
</template>

<style>
/* 폰트, 배경색 등 프로젝트 전체에 적용될 공통 CSS만 여기에 작성합니다 */
/* 기본 초기화 */
html,
body,
#app {
  margin: 0;
  padding: 0;
  width: 100%;
  min-height: 100%;
  font-family: 'Pretendard', sans-serif;
  background-color: #f4f4f4; /* 키오스크 기본 배경색 예시 */
}

/* 전체 레이아웃 */
.app-container {
  width: 100%;
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: flex-start;
}

/* router-view 영역 */
.main-content {
  width: 100%;
  max-width: 1400px;
  min-height: 100vh;
  padding: 20px;
  box-sizing: border-box;
}
</style>