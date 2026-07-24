<!--
  [화면 흐름 안내] HeadLayout
  역할: 본사 관리 화면에서 재사용되는 UI 컴포넌트다.
  진입: /head -> 이 Vue 파일 렌더링
  데이터: 사용자 동작 -> props·Pinia·상위 화면 상태 -> 응답/상태 반영
  다음 이동: 현재 상태를 갱신하거나 부모 화면에 이벤트를 전달
-->
<script setup>
import {
  onMounted,
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

import AppMessageToast
  from '@/components/common/AppMessageToast.vue'

const route = useRoute()
const THEME_KEY = 'headTheme'
const alertMessage = ref('')
const alertType = ref('info')
const sidebarNotificationCounts = ref({})
let originalAlert = null

/*
 * 쉬운주석: 설정 화면에서 고른 테마를 HTML에 표시하면 모든 본사 화면의 공통 CSS가 함께 바뀐다.
 */
const applySavedTheme = () => {
  document.documentElement.dataset.headTheme =
    localStorage.getItem(THEME_KEY) === 'dark'
      ? 'dark'
      : 'light'
}

/*
 * 쉬운주석: 기존 본사 페이지의 alert() 호출을 한 곳에서 가로채 공통 토스트로 보여준다.
 * 각 관리 화면을 모두 고치지 않아도 이후 alert 메시지까지 같은 디자인을 사용한다.
 */
const showAlertToast = (value) => {
  const message = String(value ?? '')
  alertMessage.value = ''

  requestAnimationFrame(() => {
    alertMessage.value = message
    alertType.value =
      /실패|오류|찾을 수 없|할 수 없|수 없습니다|불가|올바르지|반려/.test(message)
        ? 'error'
        : /입력|선택|필요|없습니다/.test(message)
          ? 'warning'
          : /성공|완료|되었습니다|했습니다/.test(message)
            ? 'success'
            : 'info'
  })
}

const closeAlertToast = () => {
  alertMessage.value = ''
}

onMounted(() => {
  applySavedTheme()
  originalAlert = window.alert
  window.alert = showAlertToast
})

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
  window.alert = originalAlert
})
</script>

<template>
  <div class="head-layout">
    <!-- 본사 사이드바 -->
    <HeadSidebar
      :open="sidebarOpen"
      :notification-counts="sidebarNotificationCounts"
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
          <HeadNotificationBell
            @unread-routes-change="sidebarNotificationCounts = $event"
          />
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

    <!-- 쉬운주석: 본사 전체 alert 메시지를 브라우저 창 대신 공통 토스트로 표시한다. -->
    <AppMessageToast
      :message="alertMessage"
      :type="alertType"
      @close="closeAlertToast"
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
  padding: 22px 20px 38px;
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
