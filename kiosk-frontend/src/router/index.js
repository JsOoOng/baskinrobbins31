/**
 * [모듈 흐름 안내] index
 * 역할: URL과 Vue 화면을 연결하고 로그인·권한에 따른 이동을 제어한다.
 * 호출 흐름: 브라우저 URL -> route 매칭/가드 -> 대상 Vue 화면 또는 로그인 화면
 * 데이터 기준: 실제 요청 URL과 현재 백엔드 DTO 필드를 우선한다.
 */
import { createRouter, createWebHistory } from 'vue-router'

import pinia from '../stores/pinia'
import { useHeadAuthStore } from '../stores/head/headAuthStore'

// ====================================================
// 1. Customer (키오스크) 화면 불러오기
// ====================================================
import HomeView from '@/views/customer/HomeView.vue'
import MenuView from '@/views/customer/MenuView.vue'

// ====================================================
// 본사 관리자 권한 및 유틸리티 설정
// ====================================================
/*
 * 본사 관리자 접근 가능 역할
 *
 * 현재 백엔드 DB에 ADMIN이 존재할 수 있고,
 * 기존 데이터에는 HEAD_ADMIN이 존재할 수 있으므로
 * 세 역할을 모두 허용합니다.
 */
const HEAD_ACCESS_ROLES = [
  'ADMIN',
  'HEAD_ADMIN',
  'SUPER_ADMIN'
]

/*
 * 역할 문자열 정리
 */
const normalizeRole = (role) => {
  return String(role ?? '')
    .trim()
    .toUpperCase()
}

/*
 * 본사 관리자 권한 확인
 */
const hasHeadAccessRole = (role) => {
  return HEAD_ACCESS_ROLES.includes(
    normalizeRole(role)
  )
}

/*
 * 아직 실제 화면을 만들지 않은 기능에서
 * 공통으로 사용하는 임시 화면
 */
const loadHeadTemporaryPage = () => import('../views/head/HeadTemporaryPage.vue')

// ====================================================
// 라우터 설정
// ====================================================
const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    // ====================================================
    // 키오스크 (Customer) 경로
    // ====================================================
    {
      path: '/',
      name: 'Main',
      component: () => import('../views/main/main.vue'),
    },
    {
      path: '/kiosk',
      name: 'KioskHome',
      component: HomeView
    },
    {
      path: '/menu',
      name: 'KioskMenu',
      component: MenuView
    },
    { 
      path: '/point-discount', 
      name: 'point-discount', 
      component: () => import('@/views/customer/PointDiscountView.vue') 
    },
    { 
      path: '/payment', 
      name: 'payment', 
      component: () => import('@/views/customer/OrderConfirmView.vue') 
    },
    { 
      path: '/order-confirm', 
      name: 'order-confirm', 
      component: () => import('@/views/customer/OrderConfirmView.vue') 
    },
    {
      path: '/toss/success',
      name: 'toss-success',
      component: () => import('@/views/customer/TossSuccessView.vue')
    },
    {
      path: '/toss/fail',
      name: 'toss-fail',
      component: () => import('@/views/customer/TossFailView.vue')
    },
    {
      path: '/order-complete',
      name: 'order-complete',
      component: () => import('@/views/customer/OrderCompleteView.vue')
    },

    // ====================================================
    // 지점 관리자 (Branch) 경로
    // ====================================================
    {
      path: '/branch',
      redirect: { name: 'branch-login' } // /branch 로 들어오면 로그인으로 리다이렉트
    },
    {
      path: '/branch/login',
      name: 'branch-login',
      component: () => import('../views/branch/BranchLogin.vue'),
      meta: { title: '지점 관리자 로그인' }
    },
    {
      path: '/branch',
      // 로그인 이후 지점 화면은 모두 같은 사이드바와 헤더 안에서 열린다.
      component: () => import('../components/branch/BranchLayout.vue'),
      meta: { requiresBranchAuth: true }, // 추가
      children: [
        {
          path: 'main',
          name: 'branch-main',
          component: () => import('../views/branch/BranchMain.vue'),
          meta: { title: '운영 대시보드', description: '오늘의 매장 운영 현황을 확인하세요.' }
        },
        {
          path: 'order',
          name: 'branch-order',
          component: () => import('../views/branch/OrderView.vue'),
          meta: { title: '주문 관리', description: '접수된 주문과 처리 상태를 관리합니다.' }
        },
        {
          path: 'menu',
          name: 'branch-menu',
          component: () => import('../views/branch/BranchMenu.vue'),
          meta: { title: '판매 메뉴 관리', description: '지점에서 판매할 메뉴를 설정합니다.' }
        },
        {
          path: 'inventory',
          name: 'branch-inventory',
          component: () => import('../views/branch/BranchInventory.vue'),
          meta: { title: '재고 관리', description: '상품과 아이스크림 맛의 재고를 확인합니다.' }
        },
        {
          path: 'restock-history',
          name: 'branch-restock-history',
          component: () => import('../views/branch/BranchRestockHistory.vue'),
          meta: { title: '재고 신청 내역', description: '본사에 신청한 재고의 처리 현황을 확인합니다.' }
        },
        {
          path: 'statistics',
          name: 'branch-statistics',
          component: () => import('../views/branch/statistics/BranchStatistics.vue'),
          meta: { title: '매출 통계', description: '매장의 매출과 운영 지표를 분석합니다.' }
        },
        {
          path: 'kiosk',
          name: 'branch-kiosk',
          component: () => import('../views/branch/BranchKiosk.vue'),
          meta: { title: '키오스크 관리', description: '매장 키오스크의 등록 및 상태를 관리합니다.' }
        },
        {
          path: 'kiosk/register',
          name: 'branch-kiosk-register',
          component: () => import('../views/branch/BranchKioskRegister.vue'),
          meta: { title: '키오스크 등록', description: '새 키오스크를 매장에 등록합니다.' }
        },
        {
          path: 'staff',
          name: 'branch-staff',
          component: () => import('../views/branch/StaffView.vue'),
          meta: { title: '직원 관리', description: '직원 정보와 근무 현황을 관리합니다.' }
        },
        {
          path: 'staff/register',
          name: 'StaffRegister',
          component: () => import('@/views/branch/StaffRegisterView.vue'),
          meta: { title: '직원 등록', description: '새로운 직원을 등록합니다.' }
        },
        {
          path: 'staff/:staffId/update',
          name: 'StaffUpdate',
          component: () => import('@/views/branch/StaffUpdateView.vue'),
          meta: { title: '직원 정보 수정', description: '직원의 기본 정보를 수정합니다.' }
        },
        {
          path: 'staff/:staffId/schedule',
          name: 'WeekSchedule',
          component: () => import('@/views/branch/WeekScheduleView.vue'),
          meta: { title: '직원 스케줄', description: '직원의 주간 근무 일정을 관리합니다.' }
        },
        {
          path: 'staff/:staffId/history',
          name: 'WorkHistory',
          component: () => import('@/views/branch/WorkHistoryView.vue'),
          meta: { title: '직원 근무 내역', description: '직원의 출퇴근 및 근무 기록을 확인합니다.' }
        },
        {
          path: 'staff/:staffId/salary',
          name: 'SalaryView',
          component: () => import('@/views/branch/SalaryView.vue'),
          meta: { title: '급여 관리', description: '직원의 급여 내역을 확인합니다.' }
        },
        {
          path: 'week-schedule',
          name: 'branch-week-schedule',
          component: () => import('@/views/branch/BranchScheduleView.vue'),
          meta: { title: '주간 스케줄', description: '매장 전체의 주간 근무 일정을 확인합니다.' }
        },
        {
          path: 'expense',
          name: 'ExpenseCreate',
          component: () => import('@/views/branch/expense/ExpenseCreate.vue'),
          meta: { title: '지출 관리', description: '매장 지출 내역을 등록하고 관리합니다.' }
        }
      ]
    },


    // ====================================================
    // 본사 관리자 로그인 및 예외 처리
    // ====================================================
    {
      path: '/head/login',
      name: 'head-login',
      component: () => import('../views/head/HeadLogin.vue'),
      meta: {
        publicHeadPage: true,
        title: '본사 관리자 로그인'
      }
    },
    /*
     * 403 권한 없음 화면
     * 공통 Layout 밖에 두어야 권한 검사 중 무한 리다이렉트가 발생하지 않습니다.
     */
    {
      path: '/head/not-authorized',
      name: 'head-not-authorized',
      component: () => import('../views/head/HeadNotAuthorized.vue'),
      meta: {
        publicHeadPage: true,
        title: '접근 권한 없음'
      }
    },

    // ====================================================
    // 본사 관리자 공통 Layout (Dashboard 및 하위 메뉴)
    // ====================================================
    {
      path: '/head',
      component: () => import('../components/head/HeadLayout.vue'),
      meta: { requiresHeadAuth: true },
      children: [
        /*
         * /head 접속 시 대시보드 이동
         */
        {
          path: '',
          redirect: { name: 'head-dashboard' }
        },

        // ================================================
        // 실제 구현 완료 화면 (P0, P1)
        // ================================================
        {
          path: 'dashboard',
          name: 'head-dashboard',
          component: () => import('../views/head/HeadDashboard.vue'),
          meta: {
            title: '대시보드',
            description: '전체 지점과 상품, 매출 및 운영 상태를 확인하세요.',
            phase: 'P1'
          }
        },
        {
          path: 'products',
          name: 'head-products',
          component: () => import('../views/head/HeadProduct.vue'),
          meta: {
            title: '본사 메뉴 관리',
            description: '본사 공통 상품과 가격 및 고객 화면 노출 상태를 관리합니다.',
            phase: 'P0'
          }
        },
        {
          path: 'flavors',
          name: 'head-flavors',
          component: () => import('../views/head/HeadFlavor.vue'),
          meta: {
            title: '아이스크림 관리',
            description: '아이스크림 맛을 등록하고 관리합니다.',
            phase: 'P0'
          }
        },
        {
          path: 'categories',
          name: 'head-categories',
          component: () => import('../views/head/HeadCategory.vue'),
          meta: {
            title: '카테고리 관리',
            description: '상품 카테고리와 노출 순서를 관리합니다.',
            phase: 'P0'
          }
        },
        {
          path: 'product-options',
          name: 'head-product-options',
          component: () => import('../views/head/HeadProductOption.vue'),
          meta: {
            title: '상품 옵션 관리',
            description: '상품별 선택 옵션과 추가 금액을 관리합니다.',
            phase: 'P0'
          }
        },
        {
          path: 'stores',
          name: 'head-stores',
          component: () => import('../views/head/HeadStore.vue'),
          meta: {
            title: '지점 관리',
            description: '전체 지점 정보와 지점 관리자 계정을 관리합니다.',
            phase: 'P0'
          }
        },
        {
          path: 'store-products',
          name: 'head-store-products',
          component: () => import('../views/head/HeadStoreProduct.vue'),
          meta: {
            title: '지점 판매 메뉴',
            description: '지점별 판매 상품과 가격 및 품절 상태를 관리합니다.',
            phase: 'P0'
          }
        },

        // ================================================
        // 이후 실제 화면으로 교체할 임시 기능 (P1, P2)
        // ================================================
        {
          path: 'discounts',
          name: 'head-discounts',
          component: loadHeadTemporaryPage,
          meta: { title: '메뉴 할인 관리', description: '상품에 자동 적용되는 할인 정책을 관리합니다.', phase: 'P1' }
        },
        {
          path: 'banners',
          name: 'head-banners',
          component: () => import('../views/head/HeadBanner.vue'),
          meta: { title: '배너 관리', description: '고객 키오스크에 노출되는 광고 배너를 관리합니다.', phase: 'P1' }
        },
        {
          path: 'statistics',
          name: 'head-statistics',
          component: () => import('../views/head/HeadStatistics.vue'),
          meta: { title: '통계 및 리포트', description: '결제 매출과 지점 및 상품별 판매 실적을 분석합니다.', phase: 'P1' }
        },
       {
          path: 'security',
          name: 'head-security',

          component: () =>
            import(
              '../views/head/HeadSecurity.vue'
            ),

          meta: {
            title: '보안 및 권한',

            description:
              '본사 관리자 계정과 접근 권한을 관리합니다.',

            phase: 'P1',

            roles: [
              'SUPER_ADMIN'
            ],

            implemented: true
          }
        },
        {
          path: 'settings',
          name: 'head-settings',
          component: () => import('../views/head/HeadSettings.vue'),
          meta: { title: '설정', description: '본사 시스템의 공통 설정을 관리합니다.', phase: 'P1' }
        },
        {
          path: 'logs',
          name: 'head-logs',
          component: () => import('../views/head/HeadActionLog.vue'),
          meta: { title: '작업 내역', description: '관리자 작업 내역을 조회합니다.', phase: 'P1' }
        },
        {
          path: 'coupons',
          name: 'head-coupons',
          component: () => import('../views/head/HeadCoupon.vue'),
          meta: { title: '쿠폰 관리', description: '쿠폰 발급과 사용 조건을 관리합니다.', phase: 'P2' }
        },
        {
          path: 'events',
          name: 'head-events',
          component: () => import('../views/head/HeadEvent.vue'),
          meta: { title: '이벤트 관리', description: '이벤트 대상과 진행 기간을 관리합니다.', phase: 'P2' }
        },
        {
          path: 'inventory-requests',
          name: 'head-inventory-requests',
          component: () => import('../views/head/HeadRestockRequest.vue'),
          meta: { title: '재고 신청 관리', description: '지점의 재고 신청 내역을 확인하고 처리합니다.', phase: 'P2', implemented: true }
        },
        {
          path: 'inventory',
          name: 'head-inventory',
          component: () => import('../views/head/HeadInventory.vue'),
          meta: {
            title: '재고 현황',
            description:
              '전체 지점 재고와 자동 보충 설정을 관리합니다.',
            phase: 'P2',
            implemented: true
          }
        },
        {
          path: 'deliveries',
          name: 'head-deliveries',
          component: () => import('../views/head/HeadDelivery.vue'),
          meta: { title: '배송 관리', description: '재고 출고와 배송 진행 상태를 관리합니다.', phase: 'P2' }
        },
        {
          path: 'policies',
          name: 'head-policies',
          component: () => import('../views/head/HeadPolicy.vue'),
          meta: { title: '약관 및 방침 관리', description: '이용약관 및 개인정보 처리방침을 관리합니다.', phase: 'P2' }
        },
        {
          path: 'storeFlavor',
          name: 'head-storeFlavor',
          component: () => import('../views/head/HeadStoreFlavor.vue')
        },

        /*
         * 존재하지 않는 /head 하위 주소 접근 시
         * 본사 대시보드로 리다이렉트
         */
        {
          path: ':pathMatch(.*)*',
          redirect: { name: 'head-dashboard' }
        }
      ]
    },

    // ====================================================
    // 전체 존재하지 않는 주소 (404 Fallback)
    // ====================================================
    {
      path: '/:pathMatch(.*)*',
      redirect: { name: 'KioskHome' } // 잘못된 주소 접근 시 가장 기본인 키오스크 홈으로 이동
    }
  ]
})
router.beforeEach(async (to) => {
  const headAuthStore = useHeadAuthStore(pinia)

  // ============================
  // 본사 로그인 페이지
  // ============================
  if (to.name === 'head-login') {
    if (
      headAuthStore.isAuthenticated &&
      hasHeadAccessRole(headAuthStore.role)
    ) {
      // 캐시 삭제만으로는 localStorage의 예전 JWT가 지워지지 않습니다.
      // 남아 있는 토큰을 서버에서 검증한 뒤에만 대시보드로 이동합니다.
      const restored = await headAuthStore.restoreSession()

      if (restored) {
        return { name: 'head-dashboard' }
      }

      headAuthStore.clearAuthentication()
    }

    return true
  }

  // ============================
  // 지점 로그인 페이지
  // ============================
  if (to.name === 'branch-login') {
    return true
  }

  // ============================
  // 인증 필요 여부 확인
  // ============================
  const requiresHeadAuth = to.matched.some(
    (routeRecord) => routeRecord.meta.requiresHeadAuth
  )

  const requiresBranchAuth = to.matched.some(
    (routeRecord) => routeRecord.meta.requiresBranchAuth
  )

  // ============================
  // 지점 인증
  // ============================
  if (requiresBranchAuth) {
    const branchUser = localStorage.getItem('branchUser')

    if (!branchUser) {
      return {
        name: 'branch-login',
        query: { redirect: to.fullPath }
      }
    }

    return true
  }

  // ============================
  // 본사 인증이 필요 없는 화면
  // ============================
  if (!requiresHeadAuth) {
    return true
  }

  // ============================
  // 본사 JWT 검사 (쿠키 전환으로 프론트엔드 검사 생략)
  // ============================
  // 백엔드 API 호출 시 401 에러로 처리됨

  // ============================
  // 세션 복구
  // ============================
  if (!headAuthStore.headUser) {
    const restored = await headAuthStore.restoreSession()

    if (!restored) {
      return {
        name: 'head-login',
        query: { redirect: to.fullPath }
      }
    }
  }

  // ============================
  // 본사 역할 검사
  // ============================
  if (!hasHeadAccessRole(headAuthStore.role)) {
    headAuthStore.clearAuthentication()

    return {
      name: 'head-login'
    }
  }

  // ============================
  // 특정 권한 검사
  // ============================
  const allowedRoles = to.meta.roles

  if (
    Array.isArray(allowedRoles) &&
    allowedRoles.length > 0
  ) {
    const currentRole = normalizeRole(
      headAuthStore.role
    )

    const normalizedAllowedRoles =
      allowedRoles.map(normalizeRole)

    if (
      !normalizedAllowedRoles.includes(
        currentRole
      )
    ) {
      return {
        name: 'head-not-authorized'
      }
    }
  }

  return true
})

export default router
