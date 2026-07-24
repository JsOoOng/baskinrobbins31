<!--
  [화면 흐름 안내] HeadDashboard
  역할: 본사 관리에서 사용자가 보는 화면이다.
  진입: /head/dashboard -> 이 Vue 파일 렌더링
  데이터: 사용자 동작 -> props·Pinia·상위 화면 상태 -> 응답/상태 반영
  다음 이동: 현재 상태를 갱신하거나 부모 화면에 이벤트를 전달
-->
<script setup>
import { computed, ref, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'

import P2ComingSoonModal from '../../components/head/P2ComingSoonModal.vue'
import HeadTablePagination from '../../components/head/HeadTablePagination.vue'
import { getHeadDashboardSummary } from '../../api/head/headDashboardApi'
import {
  approveHeadRestock,
  rejectHeadRestock,
  extractRestockErrorMessage
} from '../../api/head/headRestockApi'
import {
  getActiveInventoryShortageAlerts,
  sendInventoryShortageAlertToStore
} from '../../api/headquarter/headInventoryAlertApi'
import { useHeadAuthStore } from '../../stores/head/headAuthStore'
import { askRestockRejectionReason } from '../../constants/restockRejectionReasons'

const router = useRouter()

/*
 * 현재 대시보드 수치는 화면 디자인 확인용 예시입니다.
 * Dashboard API가 완성되면 응답값으로 교체합니다.
 */
const comparisonPeriod = ref('전주 대비')
const searchKeyword = ref('')
const inventoryStatusFilter = ref('ALL')
const inventorySortKey = ref('requestNumber')
const inventorySortDirection = ref('desc')
const inventoryCurrentPage = ref(1)
const inventoryPageSize = ref(10)

const p2Modal = ref({
  open: false,
  title: '',
  description: ''
})

const statistics = ref([
  { key: 'stores', label: '전체 지점 수', value: '-', subText: '불러오는 중...', trend: 'same', icon: '⌂' },
  { key: 'activeStores', label: '운영 중인 지점 수', value: '-', subText: '불러오는 중...', trend: 'same', icon: '✓' },
  { key: 'products', label: '전체 상품 수', value: '-', subText: '불러오는 중...', trend: 'same', icon: '▣' },
  { key: 'pendingInventory', label: '처리 대기 재고 신청', value: '-', subText: '불러오는 중...', trend: 'same', icon: '◷' },
  { key: 'events', label: '진행 중인 이벤트 수', value: '-', subText: '불러오는 중...', trend: 'same', icon: '★' },
  { key: 'banners', label: '노출 중인 배너 수', value: '-', subText: '불러오는 중...', trend: 'same', icon: '▤' },
  { key: 'sales', label: '오늘 전체 매출', value: '-', subText: '불러오는 중...', trend: 'same', icon: '₩' },
  { key: 'orders', label: '오늘 전체 주문 수', value: '-', subText: '불러오는 중...', trend: 'same', icon: '▧' }
])

const inventoryRequests = ref([])
const shortageAlerts = ref([])
const sendingAlertId = ref(null)
const processingRequestId = ref(null)
const headAuthStore = useHeadAuthStore()
const storeSummary = ref([])
const recentActions = ref([])

const inventoryStatusOptions = [
  'ALL',
  'waiting',
  'approved',
  'shipping',
  'completed',
  'rejected',
  'canceled'
]

/*
 * 쉬운주석: 같은 표 제목을 누르면 오름차순과 내림차순이 번갈아 적용된다.
 * 숫자·날짜·한글은 각 데이터에 맞는 비교 방법을 아래 정렬 함수에서 사용한다.
 */
const toggleInventorySort = (key) => {
  if (inventorySortKey.value === key) {
    inventorySortDirection.value =
      inventorySortDirection.value === 'asc' ? 'desc' : 'asc'
  } else {
    inventorySortKey.value = key
    inventorySortDirection.value = key === 'requestDate' ? 'desc' : 'asc'
  }
}

/* 쉬운주석: 상태 제목은 전체부터 각 처리 상태까지 한 번씩 순서대로 보여준다. */
const cycleInventoryStatus = () => {
  const currentIndex = inventoryStatusOptions.indexOf(inventoryStatusFilter.value)
  inventoryStatusFilter.value =
    inventoryStatusOptions[(currentIndex + 1) % inventoryStatusOptions.length]
}

const inventorySortMark = (key) => {
  if (inventorySortKey.value !== key) return '↕'
  return inventorySortDirection.value === 'asc' ? '↑' : '↓'
}

const inventoryStatusLabel = () => ({
  ALL: '전체',
  waiting: '승인 대기',
  approved: '승인 완료',
  shipping: '배송 중',
  completed: '배송 완료',
  rejected: '반려',
  canceled: '취소'
}[inventoryStatusFilter.value])

// 숫자 필드가 없거나 null이어도 대시보드 전체 렌더링이 중단되지 않게 0으로 표시한다.
const dashboardNumber = (value) => Number(value ?? 0)

const periodCardLabels = {
  '전일 대비': ['오늘 전체 매출', '오늘 전체 주문 수'],
  '전주 대비': ['최근 7일 전체 매출', '최근 7일 전체 주문 수'],
  '전월 대비': ['최근 30일 전체 매출', '최근 30일 전체 주문 수'],
  '전년 동기 대비': ['최근 365일 전체 매출', '최근 365일 전체 주문 수']
}

const comparisonTrend = (change) => {
  const value = dashboardNumber(change)
  if (value > 0) return 'up'
  if (value < 0) return 'down'
  return 'same'
}

const comparisonText = (change, unit) => {
  const value = dashboardNumber(change)
  if (value > 0) return `${comparisonPeriod.value} ${value}${unit} 증가`
  if (value < 0) return `${comparisonPeriod.value} ${Math.abs(value)}${unit} 감소`
  return `${comparisonPeriod.value} 동일`
}

const rateText = (change, rate) => {
  const value = dashboardNumber(change)
  if (value > 0) return `${comparisonPeriod.value} 약 ${Math.abs(rate).toFixed(1)}% 증가`
  if (value < 0) return `${comparisonPeriod.value} 약 ${Math.abs(rate).toFixed(1)}% 감소`
  return `${comparisonPeriod.value} 동일`
}

const changeAmountText = (change, unit) => {
  const value = dashboardNumber(change)
  if (value > 0) return `${value.toLocaleString()}${unit} 증가`
  if (value < 0) return `${Math.abs(value).toLocaleString()}${unit} 감소`
  return `변동 ${dashboardNumber(0).toLocaleString()}${unit}`
}

/*
 * 본사 대시보드 진입 시 요약 통계·재고 신청·부족 알림을 조회해
 * 카드, 진행률, 최근 처리 목록에 각각 분배합니다.
 */
const fetchDashboardData = async () => {
  try {
    const [data, alertResponse] = await Promise.all([
      getHeadDashboardSummary(comparisonPeriod.value),
      getActiveInventoryShortageAlerts()
    ])

    // 구버전 API의 { data: {...} } 응답과 현재의 직접 DTO 응답을 모두 처리한다.
    const summary = data?.data ?? data ?? {}
    const totalStoresChange = dashboardNumber(summary.totalStoresChange)
    const activeStoresChange = dashboardNumber(summary.activeStoresChange)
    const todaySalesChange = dashboardNumber(summary.todaySalesChange)
    const todayOrdersChange = dashboardNumber(summary.todayOrdersChange)
    const [salesLabel, ordersLabel] = periodCardLabels[comparisonPeriod.value]

    statistics.value = [
      { key: 'stores', label: '전체 지점 수', value: dashboardNumber(summary.totalStores).toString(), subText: comparisonText(totalStoresChange, '곳'), trend: comparisonTrend(totalStoresChange), icon: '⌂' },
      { key: 'activeStores', label: '운영 중인 지점 수', value: dashboardNumber(summary.activeStores).toString(), subText: comparisonText(activeStoresChange, '곳'), trend: comparisonTrend(activeStoresChange), icon: '✓' },
      { key: 'products', label: '전체 상품 수', value: dashboardNumber(summary.totalProducts).toString(), subText: '등록된 전체 상품', trend: 'same', icon: '▣' },
      { key: 'pendingInventory', label: '처리 대기 재고 신청', value: dashboardNumber(summary.pendingInventory).toString(), subText: '처리 현황', trend: 'same', icon: '◷' },
      { key: 'events', label: '진행 중인 이벤트 수', value: dashboardNumber(summary.activeEvents).toString(), subText: '진행 중인 이벤트', trend: 'same', icon: '★' },
      { key: 'banners', label: '노출 중인 배너 수', value: dashboardNumber(summary.activeBanners).toString(), subText: '활성화 배너', trend: 'same', icon: '▤' },
      {
        key: 'sales',
        label: salesLabel,
        value: dashboardNumber(summary.todaySales).toLocaleString() + '원',
        subText: rateText(todaySalesChange, dashboardNumber(summary.todaySalesChangeRate)),
        detailText: changeAmountText(todaySalesChange, '원'),
        trend: comparisonTrend(todaySalesChange),
        icon: '₩'
      },
      {
        key: 'orders',
        label: ordersLabel,
        value: dashboardNumber(summary.todayOrders).toLocaleString() + '건',
        subText: rateText(todayOrdersChange, dashboardNumber(summary.todayOrdersChangeRate)),
        detailText: changeAmountText(todayOrdersChange, '건'),
        trend: comparisonTrend(todayOrdersChange),
        icon: '▧'
      }
    ]
    
    storeSummary.value = summary.storeSummary || []
    inventoryRequests.value = summary.inventoryRequests || []
    shortageAlerts.value = Array.isArray(alertResponse.data) ? alertResponse.data : []
    recentActions.value = summary.recentActions || []
  } catch (error) {
    console.error('대시보드 통계 조회 실패:', error)
  }
}

/*
 * 대시보드의 재고 신청 행과 서버의 활성 부족 알림을
 * 지점명·품목명으로 연결해 실제 전송 대상 alertId를 찾습니다.
 */
const findShortageAlert = (request) => shortageAlerts.value.find((alert) =>
  alert.storeName === request.storeName && alert.itemName === request.productName
)

/*
 * 대시보드 알림 버튼
 * 행 선택 → 활성 alertId 확인 → 본사 관리자 ID와 함께 전송 API 호출
 * → 성공 후 대시보드 데이터를 다시 조회해 버튼 상태를 동기화합니다.
 */
const sendShortageAlert = async (request) => {
  const shortageAlert = findShortageAlert(request)
  const adminId = headAuthStore.headUser?.employeeId
  if (!shortageAlert || !adminId) {
    alert('전송 가능한 부족 알림 또는 관리자 정보가 없습니다.')
    return
  }
  if (!confirm(`${request.storeName}에 ${request.productName} 재고 부족 알림을 보내시겠습니까?`)) return
  sendingAlertId.value = shortageAlert.alertId
  try {
    await sendInventoryShortageAlertToStore(shortageAlert.alertId, adminId)
    alert('지점에 재고 부족 알림을 전송했습니다.')
    await fetchDashboardData()
  } catch (error) {
    alert(error.response?.data?.message || '지점 알림 전송에 실패했습니다.')
  } finally {
    sendingAlertId.value = null
  }
}

/*
 * 대시보드의 승인·반려도 재고 신청 관리 화면과 같은 API를 사용한다.
 * 처리 후 대시보드를 다시 조회해 세 화면의 상태를 즉시 맞춘다.
 */
const processInventoryRequest = async (request, action) => {
  const adminId = headAuthStore.headUser?.employeeId
  if (!adminId) {
    alert('처리할 본사 관리자 정보가 없습니다. 다시 로그인해 주세요.')
    return
  }

  const actionLabel = action === 'approve' ? '승인' : '반려'
  const rejectionReason = action === 'reject' ? askRestockRejectionReason() : null
  if (action === 'reject' && !rejectionReason) return
  if (action === 'approve' && !confirm(`${request.productName} 재고 신청을 승인하시겠습니까?`)) return

  processingRequestId.value = request.requestNumber
  try {
    const process = action === 'approve' ? approveHeadRestock : rejectHeadRestock
    await process(request.requestNumber, { adminId, rejectionReason })
    await fetchDashboardData()
  } catch (error) {
    alert(extractRestockErrorMessage(error, `${actionLabel} 처리에 실패했습니다.`))
  } finally {
    processingRequestId.value = null
  }
}

onMounted(() => {
  fetchDashboardData()
})

const filteredInventoryRequests = computed(() => {
  const keyword = searchKeyword.value.trim().toLowerCase()
  const filtered = inventoryRequests.value.filter((request) => {
    const matchesKeyword = !keyword || [
      request.requestNumber,
      request.storeName,
      request.productName,
      request.status
    ].some((value) => String(value).toLowerCase().includes(keyword))
    const matchesStatus =
      inventoryStatusFilter.value === 'ALL' ||
      request.statusType === inventoryStatusFilter.value
    return matchesKeyword && matchesStatus
  })

  return [...filtered].sort((a, b) => {
    const key = inventorySortKey.value
    let result

    if (['requestNumber', 'quantity'].includes(key)) {
      result = dashboardNumber(a[key]) - dashboardNumber(b[key])
    } else if (key === 'requestDate') {
      result = new Date(a.requestDate) - new Date(b.requestDate)
    } else {
      result = String(a[key] ?? '').localeCompare(String(b[key] ?? ''), 'ko')
    }

    return inventorySortDirection.value === 'asc' ? result : -result
  })
})

/*
 * 쉬운주석: 필터가 끝난 전체 목록에서 현재 페이지에 해당하는 10건만 잘라 표에 보여준다.
 */
const paginatedInventoryRequests = computed(() => {
  const start = (inventoryCurrentPage.value - 1) * inventoryPageSize.value
  return filteredInventoryRequests.value.slice(start, start + inventoryPageSize.value)
})

watch(
  [searchKeyword, inventoryStatusFilter, inventorySortKey, inventorySortDirection, inventoryPageSize],
  () => { inventoryCurrentPage.value = 1 }
)

/* 통계 증감 방향을 대시보드 카드의 화살표 문자로 변환합니다. */
const getTrendIcon = (trend) => {
  if (trend === 'up') return '↗'
  if (trend === 'down') return '↘'
  if (trend === 'success') return '✓'
  if (trend === 'warning') return '!'
  return '-'
}

/* 지점별 수치를 최대값 대비 백분율로 바꿔 진행 막대 너비를 계산합니다. */
const getProgressWidth = (item) => {
  if (!item.total) {
    return '0%'
  }

  return `${Math.min((item.value / item.total) * 100, 100)}%`
}

const openP2 = (title, description) => {
  p2Modal.value = {
    open: true,
    title,
    description
  }
}

/* 준비 중 기능 안내 모달의 열림 상태와 내용을 초기화합니다. */
const closeP2 = () => {
  p2Modal.value.open = false
}

/*
 * 요약 카드의 key에 따라 관련 본사 관리 화면으로 이동합니다.
 * 아직 구현되지 않은 카드는 이동 대신 준비 중 안내 모달을 엽니다.
 */
const handleStatisticClick = (statistic) => {
  if (statistic.phase === 'P2') {
    openP2(
      statistic.label,
      '재고 신청 조회와 승인·반려 기능을 제공할 예정입니다.'
    )

    return
  }

  const paths = {
    stores: '/head/stores',
    activeStores: '/head/stores',
    products: '/head/products',
    events: '/head/events',
    banners: '/head/banners',
    sales: '/head/statistics',
    orders: '/head/statistics',
    pendingInventory: '/head/inventory-requests'
  }

  const path = paths[statistic.key]

  if (path) {
    router.push(path)
  }
}

/* 대시보드 바로가기 버튼에서 Vue Router 경로로 이동합니다. */
const goTo = (path) => {
  router.push(path)
}
</script>

<template>
  <section class="dashboard-page">
    <div class="dashboard-controls">
      <label class="comparison-select">
        <span>비교 기준</span>

        <select v-model="comparisonPeriod" @change="fetchDashboardData">
          <option>전일 대비</option>
          <option>전주 대비</option>
          <option>전월 대비</option>
          <option>전년 동기 대비</option>
        </select>
      </label>
    </div>

    <!-- 통계 카드 8개 -->
    <div class="statistics-grid">
      <button
        v-for="statistic in statistics"
        :key="statistic.key"
        type="button"
        class="statistic-card"
        @click="handleStatisticClick(statistic)"
      >
        <div class="statistic-card-top">
          <span class="statistic-label">
            {{ statistic.label }}
          </span>

          <span
            class="statistic-icon"
            :class="`icon-${statistic.trend}`"
          >
            {{ statistic.icon }}
          </span>
        </div>

        <strong class="statistic-value">
          {{ statistic.value }}
        </strong>

        <div
          class="trend-text"
          :class="`trend-${statistic.trend}`"
        >
          <span class="trend-icon">
            {{ getTrendIcon(statistic.trend) }}
          </span>

          <span class="trend-copy">
            <span>{{ statistic.subText }}</span>
            <small v-if="statistic.detailText" class="trend-detail">
              {{ statistic.detailText }}
            </small>
          </span>

          <small v-if="statistic.phase">
            {{ statistic.phase }}
          </small>
        </div>
      </button>
    </div>

    <!-- 재고 신청 현황 -->
    <section class="dashboard-panel inventory-panel">
      <div class="panel-header">
        <div>
          <div class="panel-title-line">
            <h2>재고 신청 현황</h2>
          </div>

          <p>
            최근 지점의 신청 내역을 확인하고 승인·반려 처리합니다.
          </p>
        </div>

        <div class="panel-tools">
          <div class="search-box">
            <span>⌕</span>

            <input
              v-model="searchKeyword"
              type="search"
              placeholder="지점명 또는 신청번호 검색"
            />
          </div>

        </div>
      </div>

      <div class="table-scroll">
        <table class="request-table">
          <thead>
            <tr>
              <th><button class="table-sort-button" type="button" @click="toggleInventorySort('requestNumber')">신청 번호 {{ inventorySortMark('requestNumber') }}</button></th>
              <th><button class="table-sort-button" type="button" @click="toggleInventorySort('storeName')">지점명 {{ inventorySortMark('storeName') }}</button></th>
              <th><button class="table-sort-button" type="button" @click="toggleInventorySort('productName')">신청 메뉴 {{ inventorySortMark('productName') }}</button></th>
              <th><button class="table-sort-button" type="button" @click="toggleInventorySort('quantity')">수량 {{ inventorySortMark('quantity') }}</button></th>
              <th><button class="table-sort-button" type="button" @click="toggleInventorySort('requestDate')">신청일 {{ inventorySortMark('requestDate') }}</button></th>
              <th><button class="table-sort-button" type="button" @click="cycleInventoryStatus">상태: {{ inventoryStatusLabel() }}</button></th>
              <th>처리</th>
            </tr>
          </thead>

          <tbody>
            <tr
              v-for="request in paginatedInventoryRequests"
              :key="request.requestNumber"
            >
              <td class="request-number">
                #{{ request.requestNumber }}
              </td>

              <td>
                <div class="store-name">
                  <span
                    class="store-dot"
                    :class="`store-dot-${request.statusType}`"
                  />

                  {{ request.storeName }}
                </div>
              </td>

              <td>
                {{ request.productName }}
              </td>

              <td>
                {{ request.quantity }}
              </td>

              <td class="muted-cell">
                {{ request.requestDate }}
              </td>

              <td>
                <span
                  class="status-badge"
                  :class="`status-${request.statusType}`"
                >
                  {{ request.status }}
                </span>
              </td>

              <td>
                <div class="action-buttons">
                  <template v-if="request.statusType === 'waiting'">
                    <button
                      type="button"
                      class="approve-button"
                      :disabled="processingRequestId === request.requestNumber"
                      @click="processInventoryRequest(request, 'approve')"
                    >
                      승인
                    </button>

                    <button
                      type="button"
                      class="reject-button"
                      :disabled="processingRequestId === request.requestNumber"
                      @click="processInventoryRequest(request, 'reject')"
                    >
                      반려
                    </button>
                  </template>

                  <button
                    v-if="findShortageAlert(request)"
                    type="button"
                    class="alert-button"
                    :disabled="sendingAlertId === findShortageAlert(request).alertId"
                    @click="sendShortageAlert(request)"
                  >
                    {{ sendingAlertId === findShortageAlert(request).alertId ? '전송 중' : '지점 알림' }}
                  </button>

                  <button
                    v-else
                    type="button"
                    class="detail-button"
                    @click="goTo('/head/inventory-requests')"
                  >
                    상세 보기
                  </button>
                </div>
              </td>
            </tr>

            <tr v-if="filteredInventoryRequests.length === 0">
              <td
                colspan="7"
                class="empty-table"
              >
                검색 결과가 없습니다.
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <HeadTablePagination
        v-model:current-page="inventoryCurrentPage"
        v-model:page-size="inventoryPageSize"
        :total-items="filteredInventoryRequests.length"
      />
    </section>

    <!-- 하단 정보 -->
    <div class="bottom-grid">
      <!-- 지점 운영 현황 -->
      <section class="dashboard-panel summary-panel">
        <div class="panel-header compact-header">
          <div>
            <h2>지점 운영 현황</h2>

            <p>
              전체 지점의 현재 운영 상태입니다.
            </p>
          </div>

          <button
            type="button"
            class="text-button"
            @click="goTo('/head/stores')"
          >
            지점 관리 →
          </button>
        </div>

        <div class="store-summary-list">
          <div
            v-for="item in storeSummary"
            :key="item.label"
            class="summary-item"
          >
            <div class="summary-item-header">
              <span>
                {{ item.label }}
              </span>

              <strong>
                {{ item.value }}곳
              </strong>
            </div>

            <div class="progress-track">
              <span
                class="progress-value"
                :class="`progress-${item.type}`"
                :style="{
                  width: getProgressWidth(item)
                }"
              />
            </div>
          </div>
        </div>
      </section>

      <!-- 최근 관리자 작업 -->
      <section class="dashboard-panel action-panel">
        <div class="panel-header compact-header">
          <div>
            <h2>최근 관리자 작업</h2>

            <p>
              최근 본사 관리자 작업 기록입니다.
            </p>
          </div>
          <button
            type="button"
            class="text-button"
            @click="goTo('/head/logs')"
          >
            전체보기 →
          </button>
        </div>

        <div class="recent-action-list">
          <div
            v-for="action in recentActions"
            :key="`${action.action}-${action.time}`"
            class="recent-action-item"
          >
            <span class="action-type-icon">
              {{ action.type.charAt(0) }}
            </span>

            <div class="action-information">
              <strong>
                {{ action.action }}
              </strong>

              <p>
                {{ action.administrator }}
                ·
                {{ action.time }}
              </p>
            </div>
          </div>
        </div>
      </section>
    </div>

    <!-- 빠른 관리 -->
    <section class="dashboard-panel quick-panel">
      <div class="panel-header compact-header">
        <div>
          <h2>빠른 관리</h2>

          <p>
            자주 사용하는 본사 관리 화면으로 이동합니다.
          </p>
        </div>
      </div>

      <div class="quick-action-grid">
        <button
          type="button"
          @click="goTo('/head/categories')"
        >
          <span>◫</span>
          카테고리 관리
        </button>

        <button
          type="button"
          @click="goTo('/head/products')"
        >
          <span>▣</span>
          본사 메뉴 관리
        </button>

        <button
          type="button"
          @click="goTo('/head/banners')"
        >
          <span>▤</span>
          배너 관리
        </button>

        <button
          type="button"
          @click="goTo('/head/statistics')"
        >
          <span>↗</span>
          매출 통계
        </button>

        <button
          type="button"
          @click="goTo('/head/events')
          "
        >
          <span>★</span>
          이벤트 관리
        </button>
      </div>
    </section>

    <P2ComingSoonModal
      :open="p2Modal.open"
      :title="p2Modal.title"
      :description="p2Modal.description"
      @close="closeP2"
    />
  </section>
</template>

<style scoped>
.dashboard-page {
  display: grid;
  gap: 20px;
}

.dashboard-controls {
  display: flex;
  justify-content: flex-end;
  padding: 8px 16px;
  background: #f8f6ff;
  border: 1px solid #e2defe;
  border-radius: 12px;
}

.api-ready-badge,
.phase-label {
  padding: 4px 7px;
  border-radius: 6px;
  font-size: 9px;
  font-weight: 900;
}

.demo-badge {
  color: #6854df;
  background: #eae6ff;
}

.comparison-select {
  display: flex;
  gap: 8px;
  align-items: center;

  font-size: 11px;
  font-weight: 700;
}

.comparison-select select {
  height: 34px;
  padding: 0 9px;

  border: 1px solid #dcd8f5;
  border-radius: 8px;
  outline: none;

  color: #5f5878;
  background: #ffffff;
}

.statistics-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 14px;
}

.statistic-card {
  min-width: 0;
  min-height: 138px;
  padding: 18px;

  border: 1px solid #e4e7ed;
  border-radius: 15px;
  cursor: pointer;

  font-family: inherit;
  text-align: left;

  background: #ffffff;

  box-shadow:
    0 5px 18px
    rgba(43, 49, 68, 0.04);

  transition:
    transform 0.18s ease,
    border-color 0.18s ease,
    box-shadow 0.18s ease;
}

.statistic-card:hover {
  transform: translateY(-2px);
  border-color: #cdc6f8;

  box-shadow:
    0 11px 25px
    rgba(72, 61, 135, 0.09);
}

.statistic-card-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.statistic-label {
  color: #777f8f;
  font-size: 12px;
  font-weight: 650;
}

.statistic-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;

  width: 34px;
  height: 34px;

  border-radius: 10px;

  font-size: 15px;
  font-weight: 900;
}

.icon-up,
.icon-success {
  color: #15a76f;
  background: #dcfaec;
}

.icon-down {
  color: #e35168;
  background: #ffe5e9;
}

.icon-warning {
  color: #e69016;
  background: #fff0c9;
}

.icon-same {
  color: #6b5ce6;
  background: #ece9ff;
}

.statistic-value {
  display: block;
  overflow: hidden;

  margin-top: 13px;

  color: #242936;
  font-size: 25px;
  font-weight: 850;
  letter-spacing: -0.8px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.trend-text {
  display: flex;
  gap: 5px;
  align-items: center;

  margin-top: 10px;

  font-size: 10px;
  font-weight: 700;
}

.trend-up,
.trend-success {
  color: #15a76f;
}

.trend-down {
  color: #df4f65;
}

.trend-warning {
  color: #e28b10;
}

.trend-same {
  color: #858c9a;
}

.trend-icon {
  font-size: 14px;
}

.trend-copy {
  display: grid;
  gap: 3px;
  text-align: left;
}

.trend-copy .trend-detail {
  color: #858c9a;
  font-size: 9px;
  font-weight: 600;
}

.trend-text > small {
  margin-left: auto;
  padding: 3px 5px;

  border-radius: 5px;

  color: #e37720;
  font-size: 8px;

  background: #fff0dc;
}

.dashboard-panel {
  overflow: hidden;

  border: 1px solid #e2e6ed;
  border-radius: 15px;

  background: #ffffff;

  box-shadow:
    0 5px 18px
    rgba(43, 49, 68, 0.035);
}

.panel-header {
  display: flex;
  gap: 18px;
  align-items: center;
  justify-content: space-between;

  padding: 17px 18px;

  border-bottom: 1px solid #e9ebf0;
}

.panel-header h2 {
  margin: 0;

  color: #2b303d;
  font-size: 15px;
  font-weight: 800;
}

.panel-header p {
  margin: 5px 0 0;

  color: #9299a8;
  font-size: 10px;
}

.panel-title-line {
  display: flex;
  gap: 8px;
  align-items: center;
}

.phase-label {
  color: #e47c24;
  background: #fff0dc;
}

.panel-tools {
  display: flex;
  gap: 8px;
  align-items: center;
}

.search-box {
  display: flex;
  align-items: center;

  height: 36px;
  min-width: 220px;
  padding: 0 11px;

  border: 1px solid #dfe3ea;
  border-radius: 9px;

  color: #969dab;
  background: #ffffff;
}

.search-box input {
  width: 100%;
  margin-left: 7px;

  border: 0;
  outline: 0;

  color: #3d4350;
  font-size: 11px;
  background: transparent;
}

.primary-button,
.secondary-button {
  height: 36px;
  padding: 0 13px;

  border-radius: 9px;
  cursor: pointer;

  font-family: inherit;
  font-size: 11px;
  font-weight: 750;
}

.primary-button {
  border: 1px solid #755fe9;

  color: #ffffff;
  background: #755fe9;
}

.secondary-button {
  border: 1px solid #dfe3ea;

  color: #646c7b;
  background: #ffffff;
}

.inventory-filter-panel {
  display: flex;
  gap: 12px;
  align-items: end;
  padding: 12px 18px;
  border-bottom: 1px solid #e9ebf0;
  background: #fafbfc;
}

.inventory-filter-panel label {
  display: grid;
  gap: 5px;
  color: #646c7b;
  font-size: 10px;
  font-weight: 700;
}

.inventory-filter-panel select {
  height: 36px;
  min-width: 145px;
  padding: 0 10px;
  border: 1px solid #dfe3ea;
  border-radius: 9px;
  color: #3d4350;
  background: #ffffff;
  font: inherit;
}

.table-scroll {
  overflow-x: auto;
}

.request-table {
  width: 100%;
  min-width: 850px;

  border-collapse: collapse;
}

.request-table th {
  padding: 11px 15px;

  color: #7f8797;
  font-size: 10px;
  font-weight: 750;
  text-align: left;

  background: #fafbfc;
}

/*
 * 쉬운주석: 표 제목 전체를 누를 수 있게 만들되 기존 제목 모양은 그대로 유지한다.
 * 화살표는 현재 열의 정렬 방향을 알려주고, 상태 열은 클릭할 때마다 상태 필터가 바뀐다.
 */
.table-sort-button {
  width: 100%;
  padding: 0;
  border: 0;
  color: inherit;
  background: transparent;
  font: inherit;
  text-align: left;
  cursor: pointer;
}

.table-sort-button:hover {
  color: #5b4bd6;
}

.request-table td {
  padding: 12px 15px;

  border-top: 1px solid #eef0f4;

  color: #4b5261;
  font-size: 10px;
}

.request-number {
  color: #343a49 !important;
  font-weight: 800;
}

.store-name {
  display: flex;
  gap: 7px;
  align-items: center;

  font-weight: 700;
}

.store-dot {
  width: 7px;
  height: 7px;

  border-radius: 50%;
}

.store-dot-waiting {
  background: #f1a51d;
}

.store-dot-shipping {
  background: #4d8cf4;
}

.store-dot-complete {
  background: #24b87b;
}

.store-dot-rejected {
  background: #ea586b;
}

.muted-cell {
  color: #8f96a4 !important;
}

.status-badge {
  display: inline-flex;
  padding: 5px 8px;

  border-radius: 7px;

  font-size: 9px;
  font-weight: 800;
}

.status-waiting {
  color: #d8830f;
  background: #fff0c7;
}

.status-shipping {
  color: #3978df;
  background: #e1edff;
}

.status-complete {
  color: #169966;
  background: #dff8ec;
}

.status-rejected {
  color: #df4056;
  background: #ffe4e8;
}

.action-buttons {
  display: flex;
  gap: 5px;
}

.action-buttons button {
  height: 27px;
  padding: 0 9px;

  border: 0;
  border-radius: 6px;
  cursor: pointer;

  font-family: inherit;
  font-size: 9px;
  font-weight: 750;
}

.approve-button {
  color: #179962;
  background: #def8ea;
}

.reject-button {
  color: #df4056;
  background: #ffe8eb;
}

.detail-button {
  color: #626a78;
  background: #f0f2f5;
}

.empty-table {
  height: 100px;

  color: #969dab !important;
  text-align: center;
}

.table-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;

  padding: 12px 15px;

  border-top: 1px solid #e9ebf0;

  color: #858d9d;
  font-size: 10px;
}

.pagination {
  display: flex;
  gap: 4px;
}

.pagination button {
  min-width: 29px;
  height: 29px;

  border: 1px solid #e0e4eb;
  border-radius: 7px;
  cursor: pointer;

  color: #6b7281;
  background: #ffffff;
}

.pagination button:disabled {
  cursor: not-allowed;
  opacity: 0.45;
}

.pagination .active-page {
  border-color: #705fe8;

  color: #ffffff;
  background: #705fe8;
}

.alert-button {
  border: 0;
  border-radius: 6px;
  padding: 7px 10px;
  background: #f59e0b;
  color: #fff;
  cursor: pointer;
}

.alert-button:disabled {
  cursor: not-allowed;
  opacity: .6;
}

.bottom-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 17px;
}

.compact-header {
  padding: 16px 18px;
}

.text-button {
  border: 0;
  cursor: pointer;

  color: #6d5ce3;
  font-size: 10px;
  font-weight: 800;
  background: transparent;
}

.store-summary-list {
  display: grid;
  gap: 19px;

  padding: 20px;
}

.summary-item-header {
  display: flex;
  align-items: center;
  justify-content: space-between;

  margin-bottom: 8px;

  color: #626a78;
  font-size: 11px;
}

.summary-item-header strong {
  color: #353b48;
}

.progress-track {
  overflow: hidden;

  height: 7px;

  border-radius: 20px;
  background: #edf0f4;
}

.progress-value {
  display: block;
  height: 100%;

  border-radius: 20px;
}

.progress-normal {
  background: #30bc81;
}

.progress-waiting {
  background: #f2ac2a;
}

.progress-stopped {
  background: #ee6074;
}

.api-ready-badge {
  color: #6e61d9;
  background: #ece9ff;
}

.recent-action-list {
  display: grid;
}

.recent-action-item {
  display: flex;
  gap: 11px;
  align-items: center;

  padding: 14px 18px;

  border-top: 1px solid #eef0f4;
}

.recent-action-item:first-child {
  border-top: 0;
}

.action-type-icon {
  display: inline-flex;
  flex: 0 0 auto;
  align-items: center;
  justify-content: center;

  width: 34px;
  height: 34px;

  border-radius: 10px;

  color: #6959dd;
  font-size: 11px;
  font-weight: 850;

  background: #eeebff;
}

.action-information {
  min-width: 0;
}

.action-information strong {
  display: block;
  overflow: hidden;

  color: #414754;
  font-size: 11px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.action-information p {
  margin: 5px 0 0;

  color: #9aa1af;
  font-size: 9px;
}

.quick-action-grid {
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: 10px;

  padding: 17px;
}

.quick-action-grid button {
  position: relative;

  display: flex;
  flex-direction: column;
  gap: 9px;
  align-items: center;
  justify-content: center;

  min-height: 86px;

  border: 1px solid #e5e7ed;
  border-radius: 12px;
  cursor: pointer;

  color: #5b6271;
  font-family: inherit;
  font-size: 10px;
  font-weight: 750;

  background: #fafbfc;
}

.quick-action-grid button:hover {
  border-color: #cfc8f7;

  color: #6656da;
  background: #f7f5ff;
}

.quick-action-grid button span {
  font-size: 19px;
}

.quick-action-grid button small {
  position: absolute;
  top: 7px;
  right: 7px;

  padding: 3px 5px;

  border-radius: 5px;

  color: #e07a22;
  font-size: 8px;

  background: #fff0dc;
}

@media (max-width: 1250px) {
  .statistics-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .quick-action-grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}

@media (max-width: 800px) {
  .demo-notice {
    align-items: flex-start;
    flex-wrap: wrap;
  }

  .comparison-select {
    width: 100%;
  }

  .panel-header {
    align-items: flex-start;
    flex-direction: column;
  }

  .panel-tools {
    width: 100%;
    flex-wrap: wrap;
  }

  .search-box {
    flex: 1;
    min-width: 190px;
  }

  .bottom-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 560px) {
  .statistics-grid {
    grid-template-columns: 1fr;
  }

  .quick-action-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .statistic-card {
    min-height: 125px;
  }
}
</style>
