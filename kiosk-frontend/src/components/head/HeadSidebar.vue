<script setup>
import { computed } from 'vue'
import {
  useRoute,
  useRouter
} from 'vue-router'
import { storeToRefs } from 'pinia'

import {
  useHeadAuthStore
} from '@/stores/head/headAuthStore'

defineProps({
  open: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits([
  'close',
  'open-p2'
])

const route = useRoute()
const router = useRouter()

const headAuthStore =
  useHeadAuthStore()

const {
  isSuperAdmin
} = storeToRefs(headAuthStore)

/*
 * path 문자열 대신 routeName을 사용합니다.
 *
 * Router 경로가 일부 변경되어도
 * name이 같으면 정상 이동합니다.
 */
const menuGroups = computed(() => {
  return [
    {
      title: '',
      items: [
        {
          label: '대시보드',
          icon: '▦',
          routeName: 'head-dashboard',
          phase: 'P1',
          implemented: true
        }
      ]
    },

    {
      title: '상품 관리',
      items: [
        {
          label: '본사 메뉴 관리',
          icon: '▣',
          routeName: 'head-products',
          phase: 'P0',
          implemented: true
        },
        {
          label: '카테고리 관리',
          icon: '◫',
          routeName: 'head-categories',
          phase: 'P0',
          implemented: true
        },
        {
          label: '상품 옵션 관리',
          icon: '◇',
          routeName: 'head-product-options',
          phase: 'P0',
          implemented: true
        }
      ]
    },

    {
      title: '프로모션 관리',
      items: [
        {
          label: '메뉴 할인 관리',
          icon: '%',
          routeName: 'head-discounts',
          phase: 'P1',
          implemented: true
        },
        {
          label: '쿠폰 관리',
          icon: '◇',
          routeName: 'head-coupons',
          phase: 'P2',
          implemented: true
        },
        {
          label: '이벤트 관리',
          icon: '★',
          routeName: 'head-events',
          phase: 'P2',
          implemented: true,
          description:
            '이벤트 기간과 대상 상품·카테고리를 관리하는 기능입니다.'
        },
        {
          label: '배너 관리',
          icon: '▤',
          routeName: 'head-banners',
          phase: 'P1',
          implemented: true
        }
      ]
    },

    {
      title: '지점 운영',
      items: [
        {
          label: '지점 관리',
          icon: '⌂',
          routeName: 'head-stores',
          phase: 'P0',
          implemented: true
        },
        {
          label: '지점 판매 메뉴',
          icon: '▧',
          routeName: 'head-store-products',
          phase: 'P0',
          implemented: true
        },
        {
          label: '재고 신청 관리',
          icon: '□',
          phase: 'P2',
          implemented: false,
          description:
            '지점의 재고 신청 내역을 승인하고 처리하는 기능입니다.'
        },
        {
          label: '재고 현황',
          icon: '▥',
          phase: 'P2',
          implemented: false,
          description:
            '본사와 지점별 재고 현황을 조회하는 기능입니다.'
        },
        {
          label: '배송 관리',
          icon: '▱',
          phase: 'P2',
          implemented: false,
          description:
            '출고, 배송 중, 배송 완료 상태를 관리하는 기능입니다.'
        }
      ]
    },

    {
      title: '분석',
      items: [
        {
          label: '통계 및 리포트',
          icon: '↗',
          routeName: 'head-statistics',
          phase: 'P1',
          implemented: true
        }
      ]
    },

    {
      title: '시스템',
      items: [
        ...(isSuperAdmin.value
          ? [
              {
                label: '보안 및 권한',
                icon: '◆',
                routeName: 'head-security',
                phase: 'P1',
                implemented: true,
                roles: [
                  'SUPER_ADMIN'
                ]
              }
            ]
          : []),

        {
          label: '설정',
          icon: '⚙',
          routeName: 'head-settings',
          phase: 'P1',
          implemented: true
        }
      ]
    }
  ]
})

/*
 * 현재 활성화된 메뉴 확인
 */
const isActiveMenu = (item) => {
  if (!item.routeName) {
    return false
  }

  return route.name === item.routeName
}

/*
 * 메뉴 클릭 처리
 */
const handleMenuClick = async (item) => {
  /*
   * P2 기능은 화면 이동하지 않고
   * 공통 안내 모달을 표시합니다.
   */
  if (!item.implemented) {
    emit('open-p2', {
      title: item.label,
      description: item.description
    })

    emit('close')
    return
  }

  if (!item.routeName) {
    console.error(
      `[사이드바] ${item.label}의 Route 이름이 없습니다.`
    )

    return
  }

  /*
   * 이미 현재 화면이면 중복 이동하지 않습니다.
   */
  if (route.name === item.routeName) {
    emit('close')
    return
  }

  try {
    await router.push({
      name: item.routeName
    })

    emit('close')

  } catch (error) {
    /*
     * 동적 import 또는 Vue 파일 문법 오류가 발생하면
     * 이제 브라우저에서 확인할 수 있습니다.
     */
    console.error(
      `[사이드바 이동 실패] ${item.label}`,
      error
    )

    window.alert(
      `${item.label} 화면을 불러오지 못했습니다.\nVite 터미널의 오류 내용을 확인해주세요.`
    )
  }
}
</script>

<template>
  <aside
    class="head-sidebar"
    :class="{
      'head-sidebar-open': open
    }"
  >
    <!-- 브랜드 -->
    <div class="sidebar-brand">
      <div class="brand-logo">
        31
      </div>

      <div class="brand-text">
        <strong>
          Baskin Robbins
        </strong>

        <span>
          본사 관리자
        </span>
      </div>

      <button
        type="button"
        class="mobile-close-button"
        aria-label="메뉴 닫기"
        @click="emit('close')"
      >
        ×
      </button>
    </div>

    <!-- 메뉴 -->
    <nav class="sidebar-navigation">
      <section
        v-for="group in menuGroups"
        :key="group.title || 'main'"
        class="menu-group"
      >
        <p
          v-if="group.title"
          class="menu-group-title"
        >
          {{ group.title }}
        </p>

        <button
            v-for="item in group.items"
            :key="item.label"
            type="button"
            class="menu-item"
            :class="{
                'menu-item-active':
                isActiveMenu(item)
            }"
            @click="handleMenuClick(item)"
            >
            <span class="menu-icon">
                {{ item.icon }}
            </span>

            <span class="menu-label">
                {{ item.label }}
            </span>

            <span
                v-if="item.phase === 'P2'"
                class="phase-badge phase-p2"
            >
                P2
            </span>

            <span
                v-else-if="item.phase === 'P1'"
                class="phase-badge phase-p1"
            >
                P1
            </span>
        </button>
      </section>
    </nav>

    <!-- 하단 정보 -->
    <div class="sidebar-footer">
      <div class="system-status">
        <span class="status-dot" />

        <div>
          <strong>
            시스템 정상
          </strong>

          <p>
            Spring Boot 연결
          </p>
        </div>
      </div>

      <p class="sidebar-version">
        Kiosk Admin v1.0
      </p>
    </div>
  </aside>
</template>

<style scoped>
.head-sidebar {
  position: fixed;
  z-index: 1200;
  top: 0;
  bottom: 0;
  left: 0;

  display: flex;
  flex-direction: column;

  width: 270px;

  border-right: 1px solid #e8eaf0;

  background: #ffffff;

  transition:
    transform 0.25s ease;
}

.sidebar-brand {
  position: relative;

  display: flex;
  align-items: center;

  min-height: 76px;
  padding: 14px 20px;

  border-bottom: 1px solid #eceef3;
}

.brand-logo {
  display: inline-flex;
  flex: 0 0 auto;
  align-items: center;
  justify-content: center;

  width: 44px;
  height: 44px;

  border-radius: 14px;

  color: #ffffff;
  font-size: 17px;
  font-weight: 900;

  background:
    linear-gradient(
      140deg,
      #ef3e91,
      #735ee9
    );

  box-shadow:
    0 10px 22px
    rgba(115, 82, 218, 0.2);
}

.brand-text {
  display: grid;
  gap: 3px;

  min-width: 0;
  margin-left: 12px;
}

.brand-text strong {
  overflow: hidden;

  color: #262b38;
  font-size: 14px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.brand-text span {
  color: #969cab;
  font-size: 11px;
}

.mobile-close-button {
  display: none;

  margin-left: auto;

  border: 0;
  cursor: pointer;

  color: #777e8e;
  font-size: 27px;
  background: transparent;
}

.sidebar-navigation {
  flex: 1;
  overflow-y: auto;

  padding: 18px 13px 25px;

  scrollbar-width: thin;
}

.menu-group + .menu-group {
  margin-top: 22px;
}

.menu-group-title {
  margin: 0 0 7px;
  padding: 0 12px;

  color: #a0a6b3;
  font-size: 10px;
  font-weight: 800;
  letter-spacing: 1.1px;
}

.menu-item {
  display: flex;
  gap: 11px;
  align-items: center;

  width: 100%;
  min-height: 44px;
  padding: 9px 12px;

  border: 0;
  border-radius: 11px;
  cursor: pointer;

  color: #626a7a;
  font-family: inherit;
  font-size: 13px;
  font-weight: 650;
  text-align: left;

  background: transparent;

  transition:
    color 0.18s ease,
    background 0.18s ease,
    transform 0.18s ease;
}

.menu-item + .menu-item {
  margin-top: 3px;
}

.menu-item:hover {
  color: #5f50d8;
  background: #f4f2ff;
  transform: translateX(1px);
}

.menu-item-active {
  color: #6554df;
  background:
    linear-gradient(
      90deg,
      #f2efff,
      #fff4fa
    );
}

.menu-item-active::before {
  position: absolute;
}

.menu-icon {
  display: inline-flex;
  flex: 0 0 auto;
  align-items: center;
  justify-content: center;

  width: 22px;
  height: 22px;

  color: inherit;
  font-size: 16px;
  font-weight: 700;
}

.menu-label {
  flex: 1;
  min-width: 0;
}

.phase-badge {
  flex: 0 0 auto;

  padding: 3px 6px;

  border-radius: 6px;

  font-size: 9px;
  font-weight: 900;
}

.phase-p1 {
  color: #6d5de2;
  background: #ece9ff;
}

.phase-p2 {
  color: #e87822;
  background: #fff0dc;
}

.sidebar-footer {
  padding: 15px;

  border-top: 1px solid #eceef3;
}

.system-status {
  display: flex;
  gap: 10px;
  align-items: center;

  padding: 12px;

  border-radius: 12px;

  background: #f7f8fb;
}

.status-dot {
  width: 9px;
  height: 9px;

  border-radius: 50%;

  background: #38b97f;

  box-shadow:
    0 0 0 4px
    rgba(56, 185, 127, 0.13);
}

.system-status strong {
  color: #39404e;
  font-size: 11px;
}

.system-status p {
  margin: 2px 0 0;

  color: #989ead;
  font-size: 10px;
}

.sidebar-version {
  margin: 11px 0 0;

  color: #adb2bd;
  font-size: 10px;
  text-align: center;
}

@media (max-width: 900px) {
  .head-sidebar {
    width: 280px;
    transform: translateX(-100%);

    box-shadow:
      15px 0 45px
      rgba(25, 29, 42, 0.16);
  }

  .head-sidebar-open {
    transform: translateX(0);
  }

  .mobile-close-button {
    display: block;
  }
}
</style>