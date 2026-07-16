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
      path: '/branch/main',
      name: 'branch-main',
      component: () => import('../views/branch/BranchMain.vue'),
      meta: { title: '지점 관리자 메인' }
    },
    {
      path: '/branch/order',
      name: 'branch-order',
      component: () => import('../views/branch/OrderView.vue'),
      meta: { title: '주문 관리' }
    },
    {
      path: '/branch/menu',
      name: 'branch-menu',
      component: () => import('../views/branch/BranchMenu.vue'),
      meta: { title: '지점 메뉴 관리' }
    },
    {
      path: '/branch/inventory',
      name: 'branch-inventory',
      component: () => import('../views/branch/BranchInventory.vue')
    },
    {
      path: '/branch/statistics',
      name: 'branch-statistics',
      component: () => import('../views/branch/statistics/BranchStatistics.vue')
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
          component: loadHeadTemporaryPage,
          meta: { title: '보안 및 권한', description: '관리자 계정과 접근 권한을 관리합니다.', phase: 'P1', roles: ['SUPER_ADMIN'] }
        },
        {
          path: 'settings',
          name: 'head-settings',
          component: loadHeadTemporaryPage,
          meta: { title: '설정', description: '본사 시스템의 공통 설정을 관리합니다.', phase: 'P1' }
        },
        {
          path: 'coupons',
          name: 'head-coupons',
          component: loadHeadTemporaryPage,
          meta: { title: '쿠폰 관리', description: '쿠폰 발급과 사용 조건을 관리합니다.', phase: 'P2' }
        },
        {
          path: 'events',
          name: 'head-events',
          component: loadHeadTemporaryPage,
          meta: { title: '이벤트 관리', description: '이벤트 대상과 진행 기간을 관리합니다.', phase: 'P2' }
        },
        {
          path: 'inventory-requests',
          name: 'head-inventory-requests',
          component: loadHeadTemporaryPage,
          meta: { title: '재고 신청 관리', description: '지점의 재고 신청 내역을 확인하고 처리합니다.', phase: 'P2' }
        },
        {
          path: 'inventory',
          name: 'head-inventory',
          component: loadHeadTemporaryPage,
          meta: { title: '재고 현황', description: '본사와 지점별 재고 수량을 확인합니다.', phase: 'P2' }
        },
        {
          path: 'deliveries',
          name: 'head-deliveries',
          component: loadHeadTemporaryPage,
          meta: { title: '배송 관리', description: '재고 출고와 배송 진행 상태를 관리합니다.', phase: 'P2' }
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

// ====================================================
// 라우터 전역 가드 (본사 권한 체크)
// ====================================================
/*
 * 모든 화면 이동 전에 실행되는 인증 검사
 */
router.beforeEach(async (to) => {
  const headAuthStore = useHeadAuthStore(pinia)

  /*
   * 본사 로그인 화면에 접근한 경우
   * 이미 정상적인 본사 계정으로 로그인했다면 대시보드로 이동합니다.
   */
  if (to.name === 'head-login') {
    if (
      headAuthStore.token &&
      headAuthStore.headUser &&
      hasHeadAccessRole(headAuthStore.role)
    ) {
      return { name: 'head-dashboard' }
    }
    return true
  }

  /*
   * 부모 또는 현재 Route에 requiresHeadAuth가 있는지 확인
   */
  const requiresHeadAuth = to.matched.some(
    (routeRecord) => routeRecord.meta.requiresHeadAuth
  )

  /*
   * 본사 인증이 필요하지 않은 화면 (키오스크, 지점 등)은 통과
   */
  if (!requiresHeadAuth) {
    return true
  }

  /*
   * JWT 토큰이 없다면 본사 로그인 화면 이동
   */
  if (!headAuthStore.token) {
    return {
      name: 'head-login',
      query: { redirect: to.fullPath }
    }
  }

  /*
   * 토큰은 있지만 localStorage에 사용자 정보가 없다면 복구 시도
   */
  if (!headAuthStore.headUser) {
    const restored = await headAuthStore.restoreSession()

    if (!restored) {
      return {
        name: 'head-login',
        query: { redirect: to.fullPath }
      }
    }
  }

  /*
   * 본사 관리자 역할인지 확인
   */
  if (!hasHeadAccessRole(headAuthStore.role)) {
    headAuthStore.clearAuthentication()
    return { name: 'head-login' }
  }

  /*
   * 특정 역할 전용 화면 검사 (예: SUPER_ADMIN 전용)
   */
  const allowedRoles = to.meta.roles

  if (Array.isArray(allowedRoles) && allowedRoles.length > 0) {
    const currentRole = normalizeRole(headAuthStore.role)
    const normalizedAllowedRoles = allowedRoles.map(normalizeRole)

    if (!normalizedAllowedRoles.includes(currentRole)) {
      return { name: 'head-not-authorized' }
    }
  }

  return true
})

export default router