<!-- 지점 업무를 카테고리별로 나누어 보여주는 공통 왼쪽 메뉴이다. -->
<script setup>
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'

defineProps({ open: { type: Boolean, default: false } })
const emit = defineEmits(['close'])
const route = useRoute()
const router = useRouter()
const collapsed = ref({})

// 본사 사이드바와 같은 방식으로 지점 업무를 역할별 그룹으로 묶는다.
const groups = [
  { title: '', items: [{ label: '운영 대시보드', icon: '⌂', route: 'branch-main' }] },
  { title: '매장 운영', items: [
    { label: '주문 관리', icon: '▤', route: 'branch-order' },
    { label: '판매 메뉴 관리', icon: '◫', route: 'branch-menu' },
    { label: '재고 관리', icon: '▦', route: 'branch-inventory' },
    { label: '재고 신청 내역', icon: '↻', route: 'branch-restock-history' },
    { label: '키오스크 관리', icon: '▣', route: 'branch-kiosk' }
  ] },
  { title: '인력 관리', items: [
    { label: '직원 관리', icon: '♙', route: 'branch-staff' },
    { label: '주간 스케줄', icon: '▧', route: 'branch-week-schedule' }
  ] },
  { title: '매출 및 회계', items: [
    { label: '매출 통계', icon: '↗', route: 'branch-statistics' },
    { label: '지출 관리', icon: '₩', route: 'ExpenseCreate' }
  ] }
]

const user = computed(() => {
  // 저장 정보가 없거나 깨져 있어도 사이드바 자체는 정상 표시되도록 빈 객체를 사용한다.
  try { return JSON.parse(localStorage.getItem('branchUser') || '{}') } catch { return {} }
})
const storeName = computed(() => user.value.storeName || '지점 관리 시스템')
const isActive = (item) => route.name === item.route ||
  // 직원·키오스크의 등록/수정 화면에서도 상위 메뉴의 선택 상태를 유지한다.
  (item.route === 'branch-staff' && route.path.startsWith('/branch/staff')) ||
  (item.route === 'branch-kiosk' && route.path.startsWith('/branch/kiosk'))
const move = async (item) => {
  // 이름 기반 라우팅을 사용해 URL이 바뀌어도 메뉴 연결을 안정적으로 유지한다.
  await router.push({ name: item.route })
  emit('close')
}
</script>

<template>
  <aside class="branch-sidebar" :class="{ open }">
    <div class="sidebar-brand">
      <span class="brand-logo">31</span>
      <span class="brand-copy"><strong>Baskin Robbins</strong><small>{{ storeName }}</small></span>
      <button class="close-button" type="button" aria-label="메뉴 닫기" @click="emit('close')">×</button>
    </div>
    <nav class="sidebar-navigation" aria-label="지점 관리 메뉴">
      <section v-for="group in groups" :key="group.title || 'dashboard'" class="menu-group">
        <button v-if="group.title" class="group-title" type="button" @click="collapsed[group.title] = !collapsed[group.title]">
          <span>{{ group.title }}</span><span :class="{ folded: collapsed[group.title] }">⌄</span>
        </button>
        <button
          v-for="item in group.items"
          v-show="!collapsed[group.title]"
          :key="item.route"
          class="menu-item"
          :class="{ active: isActive(item) }"
          type="button"
          @click="move(item)"
        ><span class="menu-icon">{{ item.icon }}</span><span>{{ item.label }}</span></button>
      </section>
    </nav>
    <footer class="sidebar-footer">
      <div class="status"><span class="status-dot"></span><span><strong>매장 시스템 정상</strong><small>운영 서비스 연결됨</small></span></div>
      <p>Branch Admin v1.0</p>
    </footer>
  </aside>
</template>

<style scoped>
.branch-sidebar { position: fixed; z-index: 1200; inset: 0 auto 0 0; width: 270px; display: flex; flex-direction: column; border-right: 1px solid #e8eaf0; background: #fff; transition: transform .25s ease; }
.sidebar-brand { min-height: 76px; display: flex; align-items: center; padding: 14px 20px; border-bottom: 1px solid #eceef3; box-sizing: border-box; }
.brand-logo { flex: 0 0 auto; width: 44px; height: 44px; display: grid; place-items: center; border-radius: 14px; color: #fff; font-size: 17px; font-weight: 900; background: linear-gradient(140deg,#ef3e91,#735ee9); box-shadow: 0 10px 22px rgba(115,82,218,.2); }
.brand-copy { min-width: 0; display: grid; gap: 3px; margin-left: 12px; }
.brand-copy strong,.brand-copy small { overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.brand-copy strong { color: #262b38; font-size: 14px; }
.brand-copy small { color: #969cab; font-size: 11px; }
.close-button { display: none; margin-left: auto; border: 0; color: #777e8e; font-size: 27px; background: transparent; }
.sidebar-navigation { flex: 1; overflow-y: auto; padding: 18px 13px 25px; }
.menu-group + .menu-group { margin-top: 22px; }
.group-title { width: 100%; display: flex; justify-content: space-between; margin: 0 0 7px; padding: 0 12px; border: 0; color: #a0a6b3; font-size: 10px; font-weight: 800; letter-spacing: 1.1px; background: transparent; cursor: pointer; }
.group-title span:last-child { transition: transform .18s; }
.group-title .folded { transform: rotate(-90deg); }
.menu-item { width: 100%; min-height: 44px; display: flex; align-items: center; gap: 11px; padding: 9px 12px; border: 0; border-radius: 11px; color: #626a7a; font-family: inherit; font-size: 13px; font-weight: 650; text-align: left; background: transparent; cursor: pointer; transition: color .18s,background .18s,transform .18s; }
.menu-item + .menu-item { margin-top: 3px; }
.menu-item:hover { color: #5f50d8; background: #f4f2ff; transform: translateX(1px); }
.menu-item.active { color: #6554df; background: linear-gradient(90deg,#f2efff,#fff4fa); }
.menu-icon { width: 22px; display: inline-flex; justify-content: center; font-size: 16px; font-weight: 800; }
.sidebar-footer { padding: 15px; border-top: 1px solid #eceef3; }
.status { display: flex; align-items: center; gap: 10px; padding: 12px; border-radius: 12px; background: #f7f8fb; }
.status > span:last-child { display: grid; gap: 2px; }
.status strong { color: #39404e; font-size: 11px; }
.status small { color: #989ead; font-size: 10px; }
.status-dot { width: 9px; height: 9px; border-radius: 50%; background: #38b97f; box-shadow: 0 0 0 4px rgba(56,185,127,.13); }
.sidebar-footer p { margin: 11px 0 0; color: #adb2bd; font-size: 10px; text-align: center; }
@media (max-width: 900px) {
  .branch-sidebar { width: 280px; transform: translateX(-100%); box-shadow: 15px 0 45px rgba(25,29,42,.16); }
  .branch-sidebar.open { transform: translateX(0); }
  .close-button { display: block; }
}
</style>
