<script setup>
import {
  onBeforeUnmount,
  reactive,
  ref,
  watch
} from 'vue'

import { useRoute } from 'vue-router'

import HeadSidebar
  from './HeadSidebar.vue'

import HeadHeader
  from './HeadHeader.vue'

import HeadNotificationBell
  from '@/components/head/HeadNotificationBell.vue'

import P2ComingSoonModal
  from './P2ComingSoonModal.vue'

const route = useRoute()

const sidebarOpen =
  ref(false)

const p2Modal = reactive({
  open: false,
  title: '',
  description: ''
})

/*
 * 모바일 사이드바 열기·닫기
 */
const toggleSidebar = () => {
  sidebarOpen.value =
    !sidebarOpen.value
}

/*
 * 모바일 사이드바 닫기
 */
const closeSidebar = () => {
  sidebarOpen.value = false
}

/*
 * 아직 구현되지 않은 P2 기능 안내창
 *
 * 사이드바의 미구현 메뉴에서 사용합니다.
 */
const openP2Modal = (
  payload = {}
) => {
  p2Modal.title =
    payload.title ||
    '준비 중인 기능'

  p2Modal.description =
    payload.description || ''

  p2Modal.open = true
}

/*
 * P2 안내창 닫기
 */
const closeP2Modal = () => {
  p2Modal.open = false
}

/*
 * 주소가 변경되면
 * 모바일 사이드바를 닫습니다.
 */
watch(
  () => route.fullPath,

  () => {
    sidebarOpen.value = false
  }
)

/*
 * 모바일 메뉴가 열렸을 때
 * 뒤쪽 화면의 스크롤을 막습니다.
 */
watch(
  sidebarOpen,

  (open) => {
    document.body.style.overflow =
      open
        ? 'hidden'
        : ''
  }
)

/*
 * 레이아웃을 벗어날 때
 * body 스크롤 상태를 원상복구합니다.
 */
onBeforeUnmount(() => {
  document.body.style.overflow = ''
})
</script>

<template>
  <div class="head-layout">
    <!-- 본사 사이드바 -->
    <HeadSidebar
      :open="sidebarOpen"
      @close="closeSidebar"
      @open-p2="openP2Modal"
    />

    <!-- 모바일 사이드바 배경 -->
    <Transition name="overlay">
      <button
        v-if="sidebarOpen"
        type="button"
        class="mobile-overlay"
        aria-label="사이드바 닫기"
        @click="closeSidebar"
      />
    </Transition>

    <div class="head-main-shell">
      <!-- 본사 공통 헤더 -->
      <HeadHeader
        @toggle-sidebar="toggleSidebar"
        @open-p2="openP2Modal"
      >
        <!--
          HeadHeader의 notification 슬롯에
          실제 알림 센터를 전달합니다.
        -->
        <template #notification>
          <HeadNotificationBell />
        </template>
      </HeadHeader>

      <!-- 현재 라우트 화면 -->
      <main class="head-content">
        <RouterView />
      </main>
    </div>

    <!-- 미구현 P2 기능 안내창 -->
    <P2ComingSoonModal
      :open="p2Modal.open"
      :title="p2Modal.title"
      :description="p2Modal.description"
      @close="closeP2Modal"
    />
  </div>
</template>

<style scoped>
.head-layout {
  min-height: 100vh;
  background: #f4f6fa;
}

.head-main-shell {
  min-height: 100vh;
  margin-left: 270px;
}

.head-content {
  padding: 25px 28px 42px;
}

.mobile-overlay {
  display: none;
}

/*
 * 태블릿·모바일
 */
@media (max-width: 900px) {
  .head-main-shell {
    margin-left: 0;
  }

  .head-content {
    padding:
      20px 17px
      36px;
  }

  .mobile-overlay {
    position: fixed;
    z-index: 1100;
    inset: 0;

    display: block;

    border: 0;
    cursor: pointer;

    background:
      rgba(27, 31, 45, 0.46);
  }
}

/*
 * 모바일 사이드바 배경 전환
 */
.overlay-enter-active,
.overlay-leave-active {
  transition:
    opacity 0.2s ease;
}

.overlay-enter-from,
.overlay-leave-to {
  opacity: 0;
}
</style>