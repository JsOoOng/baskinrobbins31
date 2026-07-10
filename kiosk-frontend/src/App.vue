<template>
  <RouterView />
</template>

<script setup>
import { RouterView } from 'vue-router'
</script>

<style>
/* 여기에 앱 전체에 적용될 간단한 리셋 스타일을 두거나, 
  지금은 비워두고 assets/main.css에서 관리해도 괜찮아.
*/
body {
  margin: 0;
  font-family: Arial, sans-serif;
}
</style>