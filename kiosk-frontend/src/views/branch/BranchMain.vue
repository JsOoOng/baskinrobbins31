<!-- 지점 로그인 직후 보이는 운영 대시보드와 주요 업무 바로가기를 제공한다. -->
<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

// 로그인 정보가 손상돼도 대시보드가 멈추지 않도록 안전하게 읽는다.
const user = computed(() => {
  try { return JSON.parse(localStorage.getItem('branchUser') || '{}') } catch { return {} }
})

const storeName = computed(() => user.value.storeName || '우리 지점')
const managerName = computed(() => user.value.name || user.value.employeeName || '관리자')

// 매장에서 자주 쓰는 네 가지 업무를 대시보드 상단에 노출한다.
const quickMenus = [
  { title: '주문 확인', description: '접수된 주문과 진행 상태를 확인합니다.', icon: '▤', route: 'branch-order', tone: 'pink' },
  { title: '재고 확인', description: '현재 재고와 부족 품목을 확인합니다.', icon: '▦', route: 'branch-inventory', tone: 'violet' },
  { title: '직원 관리', description: '직원 정보와 근무 상태를 관리합니다.', icon: '♙', route: 'branch-staff', tone: 'blue' },
  { title: '매출 분석', description: '기간별 매출과 운영 지표를 확인합니다.', icon: '↗', route: 'branch-statistics', tone: 'green' }
]

const managementMenus = [
  { label: '판매 메뉴 설정', route: 'branch-menu' },
  { label: '재고 신청 내역', route: 'branch-restock-history' },
  { label: '키오스크 관리', route: 'branch-kiosk' },
  { label: '주간 스케줄', route: 'branch-week-schedule' },
  { label: '지출 등록', route: 'ExpenseCreate' }
]

const move = (route) => router.push({ name: route })
</script>

<template>
  <section class="branch-dashboard">
    <div class="welcome-card">
      <div>
        <p class="welcome-label">BRANCH OVERVIEW</p>
        <h2>{{ storeName }} 운영 현황</h2>
        <p>{{ managerName }} 관리자님, 오늘도 원활한 매장 운영을 시작해 보세요.</p>
      </div>
      <span class="welcome-mark">31</span>
    </div>

    <div class="section-heading">
      <div><h3>빠른 업무</h3><p>자주 사용하는 운영 메뉴로 바로 이동합니다.</p></div>
    </div>

    <div class="quick-grid">
      <button v-for="menu in quickMenus" :key="menu.route" class="quick-card" type="button" @click="move(menu.route)">
        <span class="quick-icon" :class="menu.tone">{{ menu.icon }}</span>
        <span class="quick-copy"><strong>{{ menu.title }}</strong><small>{{ menu.description }}</small></span>
        <span class="quick-arrow">→</span>
      </button>
    </div>

    <div class="dashboard-grid">
      <article class="management-card">
        <div class="card-heading"><div><h3>매장 관리</h3><p>운영 항목을 카테고리별로 관리하세요.</p></div></div>
        <div class="management-list">
          <button v-for="menu in managementMenus" :key="menu.route" type="button" @click="move(menu.route)">
            <span>{{ menu.label }}</span><span>→</span>
          </button>
        </div>
      </article>

      <article class="guide-card">
        <span class="guide-badge">TODAY</span>
        <h3>운영 체크 가이드</h3>
        <ul>
          <li><span>✓</span> 접수된 주문의 처리 상태를 확인해 주세요.</li>
          <li><span>✓</span> 최소 재고 미만인 품목을 확인해 주세요.</li>
          <li><span>✓</span> 오늘 근무자의 스케줄을 확인해 주세요.</li>
        </ul>
        <button type="button" @click="move('branch-week-schedule')">오늘 스케줄 확인</button>
      </article>
    </div>
  </section>
</template>

<style scoped>
.branch-dashboard { width: min(1220px,100%); margin: 0 auto; }
.welcome-card { position: relative; overflow: hidden; min-height: 180px; display: flex; align-items: center; justify-content: space-between; padding: 34px 40px; border-radius: 20px; color: #fff; background: linear-gradient(125deg,#39205f,#6840a0 58%,#a53b89); box-shadow: 0 16px 38px rgba(67,39,103,.18); box-sizing: border-box; }
.welcome-card::after { content: ""; position: absolute; right: -65px; bottom: -125px; width: 290px; height: 290px; border: 48px solid rgba(255,255,255,.06); border-radius: 50%; }
.welcome-card > * { position: relative; z-index: 1; }
.welcome-label { margin: 0 0 10px; color: #f7a8cd; font-size: 11px; font-weight: 900; letter-spacing: .18em; }
.welcome-card h2 { margin: 0; color: #fff; font-size: clamp(25px,3vw,34px); }
.welcome-card p:last-child { margin: 13px 0 0; color: rgba(255,255,255,.7); font-size: 14px; }
.welcome-mark { width: 82px; height: 82px; display: grid; place-items: center; border-radius: 25px; font-size: 34px; font-weight: 900; background: linear-gradient(145deg,#f25a9b,#e83788); box-shadow: 0 14px 30px rgba(28,8,51,.25); }
.section-heading,.card-heading { display: flex; justify-content: space-between; margin: 30px 0 14px; }
.section-heading h3,.card-heading h3,.guide-card h3 { margin: 0; color: #292e3b; font-size: 18px; }
.section-heading p,.card-heading p { margin: 5px 0 0; color: #9299a7; font-size: 12px; }
.quick-grid { display: grid; grid-template-columns: repeat(4,minmax(0,1fr)); gap: 14px; }
.quick-card { min-width: 0; display: flex; align-items: center; gap: 13px; padding: 20px; border: 1px solid #e6e8ef; border-radius: 16px; text-align: left; background: #fff; box-shadow: 0 7px 20px rgba(37,42,59,.05); cursor: pointer; transition: transform .18s,box-shadow .18s,border-color .18s; }
.quick-card:hover { transform: translateY(-2px); border-color: #d8d1f4; box-shadow: 0 13px 28px rgba(44,38,74,.09); }
.quick-icon { flex: 0 0 auto; width: 46px; height: 46px; display: grid; place-items: center; border-radius: 13px; font-size: 20px; font-weight: 800; }
.quick-icon.pink { color: #e43e85; background: #fff0f6; }
.quick-icon.violet { color: #6c57d2; background: #f1eeff; }
.quick-icon.blue { color: #3976cf; background: #edf5ff; }
.quick-icon.green { color: #29966c; background: #eaf9f3; }
.quick-copy { min-width: 0; flex: 1; display: grid; gap: 5px; }
.quick-copy strong { color: #343947; font-size: 14px; }
.quick-copy small { overflow: hidden; color: #9299a7; font-size: 11px; line-height: 1.45; }
.quick-arrow { color: #b2b6c0; font-size: 18px; }
.dashboard-grid { display: grid; grid-template-columns: minmax(0,1.45fr) minmax(300px,.75fr); gap: 16px; margin-top: 18px; }
.management-card,.guide-card { padding: 25px; border: 1px solid #e6e8ef; border-radius: 17px; background: #fff; box-shadow: 0 7px 20px rgba(37,42,59,.05); }
.management-card .card-heading { margin: 0 0 16px; }
.management-list { display: grid; grid-template-columns: 1fr 1fr; gap: 9px; }
.management-list button { min-height: 47px; display: flex; align-items: center; justify-content: space-between; padding: 0 15px; border: 1px solid #eceef3; border-radius: 11px; color: #555d6d; font-family: inherit; font-weight: 650; background: #fafbfc; cursor: pointer; }
.management-list button:hover { color: #6554df; border-color: #ddd7f7; background: #f7f5ff; }
.guide-card { color: #fff; border: 0; background: linear-gradient(145deg,#262b3b,#3b4053); }
.guide-card h3 { margin: 13px 0 20px; color: #fff; }
.guide-badge { padding: 5px 8px; border-radius: 7px; color: #f5a5ca; font-size: 9px; font-weight: 900; background: rgba(255,255,255,.09); }
.guide-card ul { display: grid; gap: 13px; margin: 0; padding: 0; list-style: none; color: rgba(255,255,255,.72); font-size: 12px; line-height: 1.5; }
.guide-card li { display: flex; gap: 9px; }
.guide-card li span { color: #f083b5; }
.guide-card button { width: 100%; min-height: 43px; margin-top: 23px; border: 0; border-radius: 10px; color: #fff; font-family: inherit; font-weight: 750; background: linear-gradient(135deg,#e6418d,#8a52cf); cursor: pointer; }
@media (max-width: 1120px) { .quick-grid { grid-template-columns: 1fr 1fr; } }
@media (max-width: 760px) {
  .welcome-card { min-height: 160px; padding: 27px 24px; }
  .welcome-mark { display: none; }
  .quick-grid,.dashboard-grid,.management-list { grid-template-columns: 1fr; }
}
</style>
