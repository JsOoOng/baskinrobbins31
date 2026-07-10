<script setup>
import { RouterLink, RouterView } from 'vue-router'
import Header from '@/components/common/Header.vue';
</script>


<template>

  <div class="app-container">

    <main class="main-content">

      <router-view />

    </main>

  </div>

</template>


<style>

/* 기본 초기화 */
html,
body,
#app {

  margin:0;

  padding:0;

  width:100%;

  min-height:100%;

}


/* 전체 레이아웃 */
.app-container {

  width:100%;

  min-height:100vh;

  display:flex;

  justify-content:center;

  align-items:flex-start;

}


/* router-view 영역 */
.main-content {

  width:100%;

  max-width:1400px;

  min-height:100vh;

  padding:20px;

  box-sizing:border-box;

}

</style>