<!--
  지점 로그인 이후의 모든 화면이 공유하는 바깥 레이아웃이다.
  사이드바와 상단 헤더는 고정하고, 가운데 RouterView에 선택한 업무 화면을 표시한다.
-->
<script setup>
import { onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import BranchSidebar from './BranchSidebar.vue'
import BranchHeader from './BranchHeader.vue'
import AppMessageToast from '@/components/common/AppMessageToast.vue'

const route = useRoute()
const sidebarOpen = ref(false)
const alertMessage = ref('')
const alertType = ref('info')
let originalAlert

// 기존 화면의 alert()도 본사 화면과 같은 토스트 메시지로 보여준다.
const showAlertToast = (value) => {
  const message = String(value ?? '')
  alertMessage.value = ''
  requestAnimationFrame(() => {
    alertMessage.value = message
    alertType.value = /실패|오류|없습니다|불가|반려/.test(message)
      ? 'error'
      : /입력|선택|필요/.test(message)
        ? 'warning'
        : /성공|완료|되었습니다/.test(message)
          ? 'success'
          : 'info'
  })
}

onMounted(() => {
  // 레이아웃이 살아 있는 동안에만 브라우저 기본 alert를 토스트로 교체한다.
  originalAlert = window.alert
  window.alert = showAlertToast
})

watch(() => route.fullPath, () => {
  // 모바일에서 메뉴를 고른 뒤에는 사이드바를 자동으로 닫는다.
  sidebarOpen.value = false
})

watch(sidebarOpen, (open) => {
  document.body.style.overflow = open ? 'hidden' : ''
})

onBeforeUnmount(() => {
  // 로그인 화면 등으로 빠져나갈 때 전역 상태를 원래대로 복구한다.
  document.body.style.overflow = ''
  window.alert = originalAlert
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
    <AppMessageToast :message="alertMessage" :type="alertType" @close="alertMessage = ''" />
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
