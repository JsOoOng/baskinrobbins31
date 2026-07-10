<script setup>
import { RouterLink, RouterView } from 'vue-router'
import Header from '@/components/common/Header.vue';
</script>

<template>

  <router-view />

</template>



<style>
/* 기본 마진 없애기 */
body, html {
  margin: 0;
  padding: 0;
  width: 100%;
  height: 100%;
}

.app-container {
  display: flex;
  flex-direction: column;
  width: 100vw; /* 가로 꽉 채우기 */
  height: 100vh; /* 세로 꽉 채우기 */
}

.main-content {
  flex: 1; /* 헤더 빼고 남은 공간 다 먹기 */
  overflow-y: auto;
  width: 100%;
}
</style>