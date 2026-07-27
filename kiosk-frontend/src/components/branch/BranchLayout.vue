<!--
  지점 로그인 이후의 모든 화면이 공유하는 바깥 레이아웃이다.
  사이드바와 상단 헤더는 고정하고, 가운데 RouterView에 선택한 업무 화면을 표시한다.
-->
<script setup>
import { onBeforeUnmount, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import BranchSidebar from './BranchSidebar.vue'
import BranchHeader from './BranchHeader.vue'

const route = useRoute()
const sidebarOpen = ref(false)
watch(() => route.fullPath, () => {
  // 모바일에서 메뉴를 고른 뒤에는 사이드바를 자동으로 닫는다.
  sidebarOpen.value = false
})

watch(sidebarOpen, (open) => {
  document.body.style.overflow = open ? 'hidden' : ''
})

onBeforeUnmount(() => {
  document.body.style.overflow = ''
})
</script>

<template>
  <div class="branch-admin-layout">
    <BranchSidebar :open="sidebarOpen" @close="sidebarOpen = false" />
    <Transition name="branch-overlay">
      <button
        v-if="sidebarOpen"
        class="branch-mobile-overlay"
        type="button"
        aria-label="사이드바 닫기"
        @click="sidebarOpen = false"
      />
    </Transition>
    <div class="branch-main-shell">
      <BranchHeader @toggle-sidebar="sidebarOpen = !sidebarOpen" />
      <main class="branch-admin-content"><RouterView /></main>
    </div>
  </div>
</template>

<style scoped>
.branch-admin-layout { min-height: 100vh; background: #f4f6fa; }
.branch-main-shell { min-height: 100vh; margin-left: 270px; }
.branch-admin-content { padding: 22px 20px 38px; }
.branch-mobile-overlay { display: none; }
.branch-overlay-enter-active,.branch-overlay-leave-active { transition: opacity .2s ease; }
.branch-overlay-enter-from,.branch-overlay-leave-to { opacity: 0; }
@media (max-width: 900px) {
  .branch-main-shell { margin-left: 0; }
  .branch-admin-content { padding: 20px 17px 36px; }
  .branch-mobile-overlay { position: fixed; z-index: 1100; inset: 0; display: block; border: 0; background: rgba(27,31,45,.46); }
}
</style>
