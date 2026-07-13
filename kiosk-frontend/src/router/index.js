import {
  createRouter,
  createWebHistory
} from 'vue-router'

import pinia from '../stores/pinia'

import {
  useHeadAuthStore
} from '../stores/head/headAuthStore'

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
const loadHeadTemporaryPage = () =>
  import('../views/head/HeadTemporaryPage.vue')

const router = createRouter({
  history: createWebHistory(
    import.meta.env.BASE_URL
  ),

  routes: [
    // ====================================================
    // 기본 접속
    // ====================================================

    {
      path: '/',
      redirect: {
        name: 'branch-login'
      }
    },

    // ====================================================
    // 지점 관리자
    // ====================================================

    {
      path: '/branch/login',
      name: 'branch-login',

      component: () =>
        import(
          '../views/branch/BranchLogin.vue'
        ),

      meta: {
        title: '지점 관리자 로그인'
      }
    },

    {
      path: '/branch/main',
      name: 'branch-main',

      component: () =>
        import(
          '../views/branch/BranchMain.vue'
        ),

      meta: {
        title: '지점 관리자 메인'
      }
    },

    {
      path: '/branch/order',
      name: 'branch-order',

      component: () =>
        import(
          '../views/branch/OrderView.vue'
        ),

      meta: {
        title: '주문 관리'
      }
    },

    {
      path: '/branch/menu',
      name: 'branch-menu',

      component: () =>
        import(
          '../views/branch/BranchMenu.vue'
        ),

      meta: {
        title: '지점 메뉴 관리'
      }
    },

    // ====================================================
    // 본사 관리자 로그인
    // ====================================================

    {
      path: '/head/login',
      name: 'head-login',

      component: () =>
        import(
          '../views/head/HeadLogin.vue'
        ),

      meta: {
        publicHeadPage: true,
        title: '본사 관리자 로그인'
      }
    },

    /*
     * 403 권한 없음 화면
     *
     * 공통 Layout 밖에 두어야 권한 검사 중
     * 무한 리다이렉트가 발생하지 않습니다.
     */
    {
      path: '/head/not-authorized',
      name: 'head-not-authorized',

      component: () =>
        import(
          '../views/head/HeadNotAuthorized.vue'
        ),

      meta: {
        publicHeadPage: true,
        title: '접근 권한 없음'
      }
    },

    // ====================================================
    // 본사 관리자 공통 Layout
    // ====================================================

    {
      path: '/head',

      component: () =>
        import(
          '../components/head/HeadLayout.vue'
        ),

      meta: {
        requiresHeadAuth: true
      },

      children: [
        /*
         * /head 접속 시 대시보드 이동
         */
        {
          path: '',

          redirect: {
            name: 'head-dashboard'
          }
        },

        // ================================================
        // 오늘 실제 구현 완료 화면
        // ================================================

        /*
         * 본사 대시보드
         */
        {
          path: 'dashboard',
          name: 'head-dashboard',

          component: () =>
            import(
              '../views/head/HeadDashboard.vue'
            ),

          meta: {
            title: '대시보드',

            description:
              '전체 지점과 상품, 매출 및 운영 상태를 확인하세요.',

            phase: 'P1'
          }
        },

        /*
         * 본사 메뉴 관리
         */
        {
          path: 'products',
          name: 'head-products',

          component: () =>
            import(
              '../views/head/HeadProduct.vue'
            ),

          meta: {
            title: '본사 메뉴 관리',

            description:
              '본사 공통 상품과 가격 및 고객 화면 노출 상태를 관리합니다.',

            phase: 'P0'
          }
        },

        /*
         * 카테고리 관리
         */
        {
          path: 'categories',
          name: 'head-categories',

          component: () =>
            import(
              '../views/head/HeadCategory.vue'
            ),

          meta: {
            title: '카테고리 관리',

            description:
              '상품 카테고리와 노출 순서를 관리합니다.',

            phase: 'P0'
          }
        },
        
        {
          path: 'product-options',
          name: 'head-product-options',
        
          component: () =>
            import(
              '../views/head/HeadProductOption.vue'
            ),
        
          meta: {
            title: '상품 옵션 관리',
        
            description:
              '상품별 선택 옵션과 추가 금액을 관리합니다.',
        
            phase: 'P0'
          }
        },

        /*
         * 지점 관리
         */
        {
          path: 'stores',
          name: 'head-stores',
        
          component: () =>
            import(
              '../views/head/HeadStore.vue'
            ),
        
          meta: {
            title: '지점 관리',
            description:
              '전체 지점 정보와 지점 관리자 계정을 관리합니다.',
        
            phase: 'P0'
          }
        },

        /*
         * 지점 판매 메뉴 관리
         */
        {
          path: 'store-products',
          name: 'head-store-products',
        
          component: () =>
            import(
              '../views/head/HeadStoreProduct.vue'
            ),
        
          meta: {
            title: '지점 판매 메뉴',
        
            description:
              '지점별 판매 상품과 가격 및 품절 상태를 관리합니다.',
        
            phase: 'P0'
          }
        },

        // ================================================
        // 이후 실제 화면으로 교체할 P1 기능
        // ================================================

        /*
         * 메뉴 할인 관리
         */
        {
          path: 'discounts',
          name: 'head-discounts',

          component: loadHeadTemporaryPage,

          meta: {
            title: '메뉴 할인 관리',

            description:
              '상품에 자동 적용되는 할인 정책을 관리합니다.',

            phase: 'P1'
          }
        },

        /*
         * 배너 관리
         */
        {
          path: 'banners',
          name: 'head-banners',

          component: loadHeadTemporaryPage,

          meta: {
            title: '배너 관리',

            description:
              '고객 화면에 노출되는 광고 배너를 관리합니다.',

            phase: 'P1'
          }
        },

        /*
         * 통계 및 리포트
         */
        {
          path: 'statistics',
          name: 'head-statistics',

          component: loadHeadTemporaryPage,

          meta: {
            title: '통계 및 리포트',

            description:
              '기간별 매출과 지점별 실적을 비교합니다.',

            phase: 'P1'
          }
        },

        /*
         * 보안 및 권한
         *
         * SUPER_ADMIN만 접근할 수 있습니다.
         */
        {
          path: 'security',
          name: 'head-security',

          component: loadHeadTemporaryPage,

          meta: {
            title: '보안 및 권한',

            description:
              '관리자 계정과 접근 권한을 관리합니다.',

            phase: 'P1',

            roles: [
              'SUPER_ADMIN'
            ]
          }
        },

        /*
         * 설정
         */
        {
          path: 'settings',
          name: 'head-settings',

          component: loadHeadTemporaryPage,

          meta: {
            title: '설정',

            description:
              '본사 시스템의 공통 설정을 관리합니다.',

            phase: 'P1'
          }
        },

        // ================================================
        // P2 예정 기능
        // ================================================

        /*
         * 쿠폰 관리
         */
        {
          path: 'coupons',
          name: 'head-coupons',

          component: loadHeadTemporaryPage,

          meta: {
            title: '쿠폰 관리',

            description:
              '쿠폰 발급과 사용 조건을 관리합니다.',

            phase: 'P2'
          }
        },

        /*
         * 이벤트 관리
         */
        {
          path: 'events',
          name: 'head-events',

          component: loadHeadTemporaryPage,

          meta: {
            title: '이벤트 관리',

            description:
              '이벤트 대상과 진행 기간을 관리합니다.',

            phase: 'P2'
          }
        },

        /*
         * 재고 신청 관리
         */
        {
          path: 'inventory-requests',
          name: 'head-inventory-requests',

          component: loadHeadTemporaryPage,

          meta: {
            title: '재고 신청 관리',

            description:
              '지점의 재고 신청 내역을 확인하고 처리합니다.',

            phase: 'P2'
          }
        },

        /*
         * 재고 현황
         */
        {
          path: 'inventory',
          name: 'head-inventory',

          component: loadHeadTemporaryPage,

          meta: {
            title: '재고 현황',

            description:
              '본사와 지점별 재고 수량을 확인합니다.',

            phase: 'P2'
          }
        },

        /*
         * 배송 관리
         */
        {
          path: 'deliveries',
          name: 'head-deliveries',

          component: loadHeadTemporaryPage,

          meta: {
            title: '배송 관리',

            description:
              '재고 출고와 배송 진행 상태를 관리합니다.',

            phase: 'P2'
          }
        },

        /*
         * 존재하지 않는 /head 하위 주소
         *
         * 본사 영역 안에서 잘못된 주소로 접근하면
         * 지점 로그인으로 가지 않고 본사 대시보드로 이동합니다.
         */
        {
          path: ':pathMatch(.*)*',

          redirect: {
            name: 'head-dashboard'
          }
        }
      ]
    },

    // ====================================================
    // 전체 존재하지 않는 주소
    // ====================================================

    {
      path: '/:pathMatch(.*)*',

      redirect: {
        name: 'branch-login'
      }
    }
  ]
})

/*
 * 모든 화면 이동 전에 실행되는 인증 검사
 */
router.beforeEach(async (to) => {
  const headAuthStore =
    useHeadAuthStore(pinia)

  /*
   * 본사 로그인 화면에 접근한 경우
   *
   * 이미 정상적인 본사 계정으로 로그인했다면
   * 로그인 화면을 다시 보여주지 않고 대시보드로 이동합니다.
   */
  if (to.name === 'head-login') {
    if (
      headAuthStore.token &&
      headAuthStore.headUser &&
      hasHeadAccessRole(
        headAuthStore.role
      )
    ) {
      return {
        name: 'head-dashboard'
      }
    }

    return true
  }

  /*
   * 부모 또는 현재 Route에
   * requiresHeadAuth가 있는지 확인합니다.
   */
  const requiresHeadAuth =
    to.matched.some(
      (routeRecord) =>
        routeRecord.meta.requiresHeadAuth
    )

  /*
   * 본사 인증이 필요하지 않은 화면
   *
   * 예:
   * 지점 로그인
   * 본사 로그인
   * 접근 권한 없음
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

      query: {
        redirect: to.fullPath
      }
    }
  }

  /*
   * 토큰은 있지만 localStorage에 사용자 정보가 없다면
   * GET /head/auth/me를 이용해 로그인 상태를 복구합니다.
   */
  if (!headAuthStore.headUser) {
    const restored =
      await headAuthStore.restoreSession()

    if (!restored) {
      return {
        name: 'head-login',

        query: {
          redirect: to.fullPath
        }
      }
    }
  }

  /*
   * 본사 관리자 역할인지 확인
   */
  if (
    !hasHeadAccessRole(
      headAuthStore.role
    )
  ) {
    headAuthStore.clearAuthentication()

    return {
      name: 'head-login'
    }
  }

  /*
   * 특정 역할 전용 화면 검사
   *
   * 현재는 보안 및 권한 화면이
   * SUPER_ADMIN 전용입니다.
   */
  const allowedRoles =
    to.meta.roles

  if (
    Array.isArray(allowedRoles) &&
    allowedRoles.length > 0
  ) {
    const currentRole =
      normalizeRole(
        headAuthStore.role
      )

    const normalizedAllowedRoles =
      allowedRoles.map(
        normalizeRole
      )

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