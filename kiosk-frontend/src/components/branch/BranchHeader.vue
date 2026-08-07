<!-- 현재 화면 제목과 로그인한 지점 관리자 정보를 보여주는 공통 상단 헤더이다. -->
<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import api from '@/api/axios'

defineEmits(['toggle-sidebar'])
const route = useRoute()
const router = useRouter()
const profileOpen = ref(false)
const profileRef = ref(null)

// 로그인 때 저장한 지점 정보를 헤더의 지점명과 관리자명에 사용한다.
const user = computed(() => {
  try { return JSON.parse(localStorage.getItem('branchUser') || '{}') } catch { return {} }
})
const displayName = computed(() => user.value.name || user.value.employeeName || '지점 관리자')
const storeName = computed(() => user.value.storeName || '지점')
const avatar = computed(() => displayName.value.trim().charAt(0) || '관')

// 인증 정보를 모두 지운 다음 지점 로그인 화면으로 돌아간다.
const logout = async () => {
  try {
    await api.post('/branch/logout')
  } finally {
    localStorage.removeItem('branchToken')
    localStorage.removeItem('branchUser')

    await router.replace({
      name: 'branch-login'
    })
  }
}
const outsideClick = (event) => {
  // 프로필 메뉴 바깥을 누르면 펼쳐진 메뉴를 닫는다.
  if (profileRef.value && !profileRef.value.contains(event.target)) profileOpen.value = false
}
onMounted(() => document.addEventListener('click', outsideClick))
onBeforeUnmount(() => document.removeEventListener('click', outsideClick))
</script>

<template>
  <header class="branch-header">
    <div class="header-left">
      <button class="sidebar-toggle" type="button" aria-label="사이드바 열기" @click="$emit('toggle-sidebar')"><span></span><span></span><span></span></button>
      <div class="page-heading"><h1>{{ route.meta.title || '지점 관리' }}</h1><p>{{ route.meta.description || '매장 운영 시스템' }}</p></div>
    </div>
    <div ref="profileRef" class="profile-wrapper">
      <button class="profile-button" type="button" @click.stop="profileOpen = !profileOpen">
        <span class="avatar">{{ avatar }}</span>
        <span class="profile-copy"><strong>{{ displayName }}</strong><small>{{ storeName }}</small></span>
        <span class="arrow" :class="{ open: profileOpen }">⌄</span>
      </button>
      <Transition name="dropdown">
        <div v-if="profileOpen" class="profile-dropdown">
          <div class="dropdown-user"><span class="avatar">{{ avatar }}</span><span><strong>{{ displayName }}</strong><small>{{ storeName }} 관리자</small></span></div>
          <div class="divider"></div>
          <button type="button" @click="logout">로그아웃</button>
        </div>
      </Transition>
    </div>
  </header>
</template>

<style scoped>
.branch-header { position: sticky; z-index: 900; top: 0; min-height: 76px; display: flex; align-items: center; justify-content: space-between; padding: 12px 28px; border-bottom: 1px solid #e8eaf0; background: rgba(255,255,255,.94); backdrop-filter: blur(10px); box-sizing: border-box; }
.header-left { min-width: 0; display: flex; align-items: center; }
.sidebar-toggle { display: none; flex-direction: column; gap: 4px; width: 42px; height: 42px; margin-right: 12px; border: 1px solid #e2e5ec; border-radius: 11px; background: #fff; }
.sidebar-toggle span { width: 18px; height: 2px; margin: 0 auto; border-radius: 2px; background: #656d7c; }
.page-heading { min-width: 0; }
.page-heading h1 { overflow: hidden; margin: 0; color: #292e3b; font-size: 21px; font-weight: 800; letter-spacing: -.7px; text-overflow: ellipsis; white-space: nowrap; }
.page-heading p { overflow: hidden; margin: 4px 0 0; color: #969dab; font-size: 11px; text-overflow: ellipsis; white-space: nowrap; }
.profile-wrapper { position: relative; }
.profile-button { min-width: 182px; height: 48px; display: flex; align-items: center; gap: 10px; padding: 5px 10px 5px 6px; border: 1px solid #e3e6ed; border-radius: 13px; text-align: left; background: #fff; cursor: pointer; }
.profile-button:hover { background: #fafaff; }
.avatar { flex: 0 0 auto; width: 36px; height: 36px; display: grid; place-items: center; border-radius: 12px; color: #fff; font-size: 13px; font-weight: 800; background: linear-gradient(140deg,#ed3e90,#735fe8); }
.profile-copy { min-width: 0; flex: 1; display: grid; gap: 2px; }
.profile-copy strong { overflow: hidden; color: #343946; font-size: 12px; text-overflow: ellipsis; white-space: nowrap; }
.profile-copy small { color: #979dab; font-size: 10px; }
.arrow { color: #8e95a3; transition: transform .18s; }
.arrow.open { transform: rotate(180deg); }
.profile-dropdown { position: absolute; z-index: 9100; top: calc(100% + 10px); right: 0; width: 225px; padding: 9px; border: 1px solid #e4e7ed; border-radius: 15px; background: #fff; box-shadow: 0 20px 48px rgba(31,36,53,.14); }
.dropdown-user { display: flex; align-items: center; gap: 11px; padding: 10px; }
.dropdown-user > span:last-child { min-width: 0; display: grid; gap: 3px; }
.dropdown-user strong { color: #363b48; font-size: 12px; }
.dropdown-user small { color: #999fad; font-size: 10px; }
.divider { height: 1px; margin: 5px 2px; background: #eceef2; }
.profile-dropdown button { width: 100%; min-height: 40px; padding: 8px 10px; border: 0; border-radius: 9px; color: #e05269; font-family: inherit; font-size: 12px; font-weight: 600; text-align: left; background: transparent; cursor: pointer; }
.profile-dropdown button:hover { background: #fff1f3; }
.dropdown-enter-active,.dropdown-leave-active { transition: opacity .16s,transform .16s; }
.dropdown-enter-from,.dropdown-leave-to { opacity: 0; transform: translateY(-5px); }
@media (max-width: 900px) { .branch-header { padding: 11px 17px; } .sidebar-toggle { display: flex; } }
@media (max-width: 600px) { .page-heading p,.profile-copy,.arrow { display: none; } .profile-button { min-width: auto; padding-right: 6px; } }
</style>
