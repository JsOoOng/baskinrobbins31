<!--
  [화면 흐름 안내] HeadHeader
  역할: 본사 관리 화면에서 재사용되는 UI 컴포넌트다.
  진입: 상위 라우트 또는 부모 컴포넌트 -> 이 Vue 파일 렌더링
  데이터: 사용자 동작 -> props·Pinia·상위 화면 상태 -> 응답/상태 반영
  다음 이동: 현재 상태를 갱신하거나 부모 화면에 이벤트를 전달
-->
<script setup>
import {
  computed,
  onBeforeUnmount,
  onMounted,
  ref
} from 'vue'

import {
  useRoute,
  useRouter
} from 'vue-router'

import {
  storeToRefs
} from 'pinia'

import {
  useHeadAuthStore
} from '@/stores/head/headAuthStore'

const emit = defineEmits([
  'toggle-sidebar',
  'open-p2'
])

const route = useRoute()
const router = useRouter()

const headAuthStore =
  useHeadAuthStore()

const {
  displayName,
  role
} = storeToRefs(
  headAuthStore
)

const profileMenuOpen =
  ref(false)

const profileMenuRef =
  ref(null)

/*
 * 현재 라우트의 화면 제목
 */
const pageTitle = computed(() => {
  return route.meta.title ||
    '본사 관리자'
})

/*
 * 현재 라우트의 화면 설명
 */
const pageDescription = computed(() => {
  return route.meta.description ||
    '본사 관리 시스템'
})

/*
 * 관리자 권한 한글 표시
 */
const roleLabel = computed(() => {
  if (
    role.value ===
    'SUPER_ADMIN'
  ) {
    return '최고 관리자'
  }

  if (
    role.value ===
    'HEAD_ADMIN'
  ) {
    return '본사 책임 관리자'
  }

  if (
    role.value ===
    'ADMIN'
  ) {
    return '본사 관리자'
  }

  return role.value ||
    '관리자'
})

/*
 * 관리자 프로필 첫 글자
 */
const avatarText = computed(() => {
  return (
    displayName.value
      ?.trim()
      ?.charAt(0) ||
    '관'
  )
})

/*
 * 관리자 메뉴 열기·닫기
 */
const toggleProfileMenu = () => {
  profileMenuOpen.value =
    !profileMenuOpen.value
}

/*
 * 관리자 프로필
 *
 * 아직 구현 전이므로
 * 기존 P2 안내창을 사용합니다.
 */
const openMyProfile = () => {
  profileMenuOpen.value = false

  emit('open-p2', {
    title: '관리자 프로필',

    description:
      '관리자 개인정보와 비밀번호를 변경하는 기능입니다.'
  })
}

/*
 * 설정 화면 이동
 */
const goSettings = async () => {
  profileMenuOpen.value = false

  await router.push({
    name: 'head-settings'
  })
}

/*
 * 로그아웃
 */
const logout = async () => {
  profileMenuOpen.value = false

  await headAuthStore.logout()

  await router.replace({
    name: 'head-login'
  })
}

/*
 * 관리자 메뉴 바깥 클릭 시 닫기
 */
const handleDocumentClick = (
  event
) => {
  const menu =
    profileMenuRef.value

  if (
    menu &&
    !menu.contains(
      event.target
    )
  ) {
    profileMenuOpen.value = false
  }
}

onMounted(() => {
  document.addEventListener(
    'click',
    handleDocumentClick
  )
})

onBeforeUnmount(() => {
  document.removeEventListener(
    'click',
    handleDocumentClick
  )
})
</script>

<template>
  <header class="head-header">
    <div class="header-left">
      <!-- 모바일 사이드바 버튼 -->
      <button
        type="button"
        class="sidebar-toggle"
        aria-label="사이드바 열기"
        @click="
          emit('toggle-sidebar')
        "
      >
        <span />
        <span />
        <span />
      </button>

      <!-- 현재 페이지 정보 -->
      <div class="page-heading">
        <h1>
          {{ pageTitle }}
        </h1>

        <p>
          {{ pageDescription }}
        </p>
      </div>
    </div>

    <div class="header-actions">
      <!--
        HeadLayout.vue에서 전달한
        HeadNotificationBell 컴포넌트가
        이 위치에 출력됩니다.
      -->
      <div class="header-notification-area">
        <slot name="notification" />
      </div>

      <!-- 관리자 메뉴 -->
      <div
        ref="profileMenuRef"
        class="profile-menu-wrapper"
      >
        <button
          type="button"
          class="profile-button"
          @click.stop="
            toggleProfileMenu
          "
        >
          <span class="profile-avatar">
            {{ avatarText }}
          </span>

          <span class="profile-information">
            <strong>
              {{ displayName }}
            </strong>

            <small>
              {{ roleLabel }}
            </small>
          </span>

          <span
            class="profile-arrow"
            :class="{
              'profile-arrow-open':
                profileMenuOpen
            }"
          >
            ▾
          </span>
        </button>

        <Transition name="dropdown">
          <div
            v-if="profileMenuOpen"
            class="profile-dropdown"
          >
            <div class="dropdown-user">
              <span class="dropdown-avatar">
                {{ avatarText }}
              </span>

              <div>
                <strong>
                  {{ displayName }}
                </strong>

                <p>
                  {{ roleLabel }}
                </p>
              </div>
            </div>

            <div class="dropdown-divider" />

            <button
              type="button"
              @click="openMyProfile"
            >
              <span>◎</span>

              내 정보

              <small>P2</small>
            </button>

            <button
              type="button"
              @click="goSettings"
            >
              <span>⚙</span>

              설정
            </button>

            <div class="dropdown-divider" />

            <button
              type="button"
              class="logout-button"
              @click="logout"
            >
              <span>→</span>

              로그아웃
            </button>
          </div>
        </Transition>
      </div>
    </div>
  </header>
</template>

<style scoped>
.head-header {
  position: sticky;
  z-index: 900;
  top: 0;

  display: flex;
  align-items: center;
  justify-content: space-between;

  min-height: 76px;
  padding: 12px 28px;

  border-bottom:
    1px solid #e8eaf0;

  background:
    rgba(255, 255, 255, 0.94);

  backdrop-filter:
    blur(10px);
}

.header-left {
  display: flex;
  align-items: center;
  min-width: 0;
}

.sidebar-toggle {
  display: none;
  flex-direction: column;
  gap: 4px;

  width: 42px;
  height: 42px;
  margin-right: 12px;

  border:
    1px solid #e2e5ec;
  border-radius: 11px;

  cursor: pointer;

  background: #ffffff;
}

.sidebar-toggle span {
  width: 18px;
  height: 2px;
  margin: 0 auto;

  border-radius: 2px;

  background: #656d7c;
}

.page-heading {
  min-width: 0;
}

.page-heading h1 {
  overflow: hidden;
  margin: 0;

  color: #292e3b;
  font-size: 21px;
  font-weight: 800;
  letter-spacing: -0.7px;

  text-overflow: ellipsis;
  white-space: nowrap;
}

.page-heading p {
  overflow: hidden;
  margin: 4px 0 0;

  color: #969dab;
  font-size: 11px;

  text-overflow: ellipsis;
  white-space: nowrap;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 11px;
}

/*
 * 실제 알림벨 영역
 */
.header-notification-area {
  position: relative;

  display: flex;
  align-items: center;
  justify-content: center;
}

.profile-menu-wrapper {
  position: relative;
}

.profile-button {
  display: flex;
  align-items: center;
  gap: 10px;

  min-width: 182px;
  height: 48px;
  padding:
    5px 10px
    5px 6px;

  border:
    1px solid #e3e6ed;
  border-radius: 13px;

  cursor: pointer;

  text-align: left;

  background: #ffffff;
}

.profile-button:hover {
  background: #fafaff;
}

.profile-avatar,
.dropdown-avatar {
  display: inline-flex;
  flex: 0 0 auto;
  align-items: center;
  justify-content: center;

  border-radius: 12px;

  color: #ffffff;
  font-weight: 800;

  background:
    linear-gradient(
      140deg,
      #ed3e90,
      #735fe8
    );
}

.profile-avatar {
  width: 36px;
  height: 36px;

  font-size: 13px;
}

.profile-information {
  display: grid;
  flex: 1;
  gap: 2px;

  min-width: 0;
}

.profile-information strong {
  overflow: hidden;

  color: #343946;
  font-size: 12px;

  text-overflow: ellipsis;
  white-space: nowrap;
}

.profile-information small {
  color: #979dab;
  font-size: 10px;
}

.profile-arrow {
  color: #8e95a3;
  font-size: 12px;

  transition:
    transform 0.18s ease;
}

.profile-arrow-open {
  transform:
    rotate(180deg);
}

.profile-dropdown {
  position: absolute;
  z-index: 9100;
  top: calc(100% + 10px);
  right: 0;

  width: 230px;
  padding: 9px;

  border:
    1px solid #e4e7ed;
  border-radius: 15px;

  background: #ffffff;

  box-shadow:
    0 20px 48px
    rgba(31, 36, 53, 0.14);
}

.dropdown-user {
  display: flex;
  align-items: center;
  gap: 11px;

  padding: 10px;
}

.dropdown-avatar {
  width: 38px;
  height: 38px;

  font-size: 13px;
}

.dropdown-user strong {
  color: #363b48;
  font-size: 12px;
}

.dropdown-user p {
  margin: 3px 0 0;

  color: #999fad;
  font-size: 10px;
}

.dropdown-divider {
  height: 1px;
  margin: 5px 2px;

  background: #eceef2;
}

.profile-dropdown button {
  display: flex;
  align-items: center;
  gap: 10px;

  width: 100%;
  min-height: 40px;
  padding: 8px 10px;

  border: 0;
  border-radius: 9px;

  cursor: pointer;

  color: #5f6675;
  font-family: inherit;
  font-size: 12px;
  text-align: left;

  background: transparent;
}

.profile-dropdown button:hover {
  background: #f5f4ff;
}

.profile-dropdown button span {
  display: inline-flex;
  justify-content: center;

  width: 18px;
}

.profile-dropdown button small {
  margin-left: auto;
  padding: 2px 5px;

  border-radius: 5px;

  color: #e77b28;
  font-size: 8px;
  font-weight: 900;

  background: #fff0df;
}

.profile-dropdown .logout-button {
  color: #e05269;
}

.dropdown-enter-active,
.dropdown-leave-active {
  transition:
    opacity 0.16s ease,
    transform 0.16s ease;
}

.dropdown-enter-from,
.dropdown-leave-to {
  opacity: 0;

  transform:
    translateY(-5px);
}

@media (max-width: 900px) {
  .head-header {
    padding: 11px 17px;
  }

  .sidebar-toggle {
    display: flex;
  }
}

@media (max-width: 600px) {
  .page-heading p {
    display: none;
  }

  .profile-button {
    min-width: auto;
    padding-right: 6px;
  }

  .profile-information,
  .profile-arrow {
    display: none;
  }
}
</style>