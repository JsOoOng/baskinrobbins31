<!--
  [화면 흐름 안내] HeadStatistics
  역할: 본사 관리에서 사용자가 보는 화면이다.
  진입: /head/statistics -> 이 Vue 파일 렌더링
  데이터: 사용자 동작 -> @/api/head/headStoreApi, @/api/head/headStatisticsApi -> 응답/상태 반영
  다음 이동: 현재 상태를 갱신하거나 부모 화면에 이벤트를 전달
-->
<script setup>
import {
  computed,
  onMounted,
  reactive,
  ref,
  watch
} from 'vue'

import {
  extractStoreData,
  getHeadStores
} from '@/api/head/headStoreApi'

import AppMessageToast
  from '@/components/common/AppMessageToast.vue'

import {
  extractStatisticsData,
  extractStatisticsErrorMessage,
  getHeadFlavorSalesRanking,
  getHeadProductSalesRanking,
  getHeadSalesTrend,
  getHeadStatisticsSummary,
  getHeadStoreSalesRanking
} from '@/api/head/headStatisticsApi'

/*
 * 기본 상태
 */
const loading = ref(false)
const message = ref('')

const stores = ref([])
const trend = ref([])
const graphDisplayCount = ref('ALL')
const graphAnimationKey = ref(0)
/*
 * 그래프 표시 개수 선택값에 따라 최신 데이터 N개만 잘라냅니다.
 * ALL은 조회된 전체 기간을 그대로 사용하고 차트 템플릿이 이 값을 렌더링합니다.
 */
const displayedTrend = computed(() => {
  if (graphDisplayCount.value === 'ALL') return trend.value
  return trend.value.slice(-Number(graphDisplayCount.value))
})
const storeRanking = ref([])
const productRanking = ref([])
const flavorRanking = ref([])

const summary = reactive({
  totalSales: 0,
  orderCount: 0,
  salesQuantity: 0,
  averageOrderAmount: 0
})

/*
 * 인기 분석 탭
 *
 * PRODUCT: 상품 순위
 * FLAVOR: 맛 순위
 * DONUT: 맛 비율
 */
const popularityView = ref('PRODUCT')
const popularityPageSize = ref(5)

/*
 * 상품 순위 페이지
 */
const productRankingPage = ref(1)

/*
 * 맛 순위 페이지
 */
const flavorRankingPage = ref(1)

/*
 * 도넛 오른쪽 순위 목록 전용 페이지
 *
 * 왼쪽 도넛 그래프와 독립적으로 동작합니다.
 */
const donutLegendPage = ref(1)

/*
 * 인기 분석 페이지당 개수가 바뀌면 모든 순위 페이지를 첫 페이지로 돌리고,
 * key를 변경해 막대·도넛 그래프 애니메이션을 처음부터 다시 실행합니다.
 */
watch(popularityPageSize, () => {
  productRankingPage.value = 1
  flavorRankingPage.value = 1
  donutLegendPage.value = 1
  graphAnimationKey.value += 1
})
/*
 * 매출 그래프 표시 개수 변경 시 Vue key를 갱신해 막대 요소를 재생성합니다.
 */
watch(graphDisplayCount, () => {
  graphAnimationKey.value += 1
})

/*
 * 도넛 색상
 *
 * 0~9: 상위 10개 맛
 * 10: 11위 이하를 합친 기타
 */
const donutColors = [
  '#725ee7',
  '#ef4f91',
  '#42b983',
  '#f3a43b',
  '#4d8cf4',
  '#9b6be8',
  '#ef6b69',
  '#37a6a6',
  '#e28b38',
  '#607dce',
  '#9aa1af'
]

const clearMessage = () => {
  message.value = ''
}

/*
 * 상품 순위 계산
 */
const productRankingTotalPages =
  computed(() => {
    return Math.max(
      1,
      Math.ceil(
        productRanking.value.length /
        popularityPageSize.value
      )
    )
  })

const pagedProductRanking =
  computed(() => {
    const startIndex =
      (
        productRankingPage.value - 1
      ) * popularityPageSize.value

    return productRanking.value.slice(
      startIndex,
      startIndex +
        popularityPageSize.value
    )
  })

const getProductRankingNumber = (
  index
) => {
  return (
    (
      productRankingPage.value - 1
    ) * popularityPageSize.value
  ) + index + 1
}

/*
 * 맛 순위 계산
 */
const flavorRankingTotalPages =
  computed(() => {
    return Math.max(
      1,
      Math.ceil(
        flavorRanking.value.length /
        popularityPageSize.value
      )
    )
  })

const pagedFlavorRanking =
  computed(() => {
    const startIndex =
      (
        flavorRankingPage.value - 1
      ) * popularityPageSize.value

    return flavorRanking.value.slice(
      startIndex,
      startIndex +
        popularityPageSize.value
    )
  })

const getFlavorRankingNumber = (
  index
) => {
  return (
    (
      flavorRankingPage.value - 1
    ) * popularityPageSize.value
  ) + index + 1
}

/*
 * 맛 판매 비율 계산
 */
const totalFlavorSalesQuantity =
  computed(() => {
    return flavorRanking.value.reduce(
      (total, flavor) => {
        return (
          total +
          Number(
            flavor.salesQuantity ?? 0
          )
        )
      },
      0
    )
  })

/*
 * 도넛에서 개별 조각으로 표시할 상위 10개 맛
 */
const donutTopFlavors =
  computed(() => {
    return flavorRanking.value.slice(
      0,
      popularityPageSize.value
    )
  })

/*
 * 11위 이하 판매량 합계
 */
const otherFlavorSalesQuantity =
  computed(() => {
    return flavorRanking.value
      .slice(popularityPageSize.value)
      .reduce(
        (total, flavor) => {
          return (
            total +
            Number(
              flavor.salesQuantity ?? 0
            )
          )
        },
        0
      )
  })

/*
 * 왼쪽 도넛 그래프 데이터
 *
 * 상위 10개 맛 + 기타
 */
const donutFlavorData =
  computed(() => {
    const result = [
      ...donutTopFlavors.value
    ]

    if (
      otherFlavorSalesQuantity.value > 0
    ) {
      result.push({
        flavorId: 'OTHER',
        flavorName: '기타',
        salesQuantity:
          otherFlavorSalesQuantity.value
      })
    }

    return result
  })

const getFlavorPercentage = (
  flavor
) => {
  if (
    totalFlavorSalesQuantity.value <= 0
  ) {
    return 0
  }

  return Number(
    (
      Number(
        flavor.salesQuantity ?? 0
      ) /
      totalFlavorSalesQuantity.value *
      100
    ).toFixed(1)
  )
}

const otherFlavorPercentage =
  computed(() => {
    if (
      totalFlavorSalesQuantity.value <= 0
    ) {
      return 0
    }

    return Number(
      (
        otherFlavorSalesQuantity.value /
        totalFlavorSalesQuantity.value *
        100
      ).toFixed(1)
    )
  })

const getFlavorColor = (index) => {
  if (index >= 10) {
    return donutColors[10]
  }

  return donutColors[index]
}

/*
 * 왼쪽 도넛은 오른쪽 순위 페이지와 관계없이
 * 항상 상위 10개 + 기타를 표시합니다.
 */
const donutBackground =
  computed(() => {
    if (
      donutFlavorData.value.length === 0 ||
      totalFlavorSalesQuantity.value === 0
    ) {
      return '#edf0f4'
    }

    let currentPercentage = 0

    const sections =
      donutFlavorData.value.map(
        (flavor, index) => {
          /*
           * 그래프 구간에는 반올림 전 비율을 사용합니다.
           * 반올림 오차로 생기는 빈 공간을 방지합니다.
           */
          const percentage =
            Number(
              flavor.salesQuantity ?? 0
            ) /
            totalFlavorSalesQuantity.value *
            100

          const start =
            currentPercentage

          const end =
            currentPercentage +
            percentage

          currentPercentage = end

          return `${
            getFlavorColor(index)
          } ${start}% ${end}%`
        }
      )

    return `conic-gradient(${sections.join(', ')})`
  })

/*
 * 도넛 오른쪽 순위 목록 페이징
 */
const donutLegendTotalPages =
  computed(() => {
    return Math.max(
      1,
      Math.ceil(
        flavorRanking.value.length /
        popularityPageSize.value
      )
    )
  })

const pagedDonutLegend =
  computed(() => {
    const startIndex =
      (
        donutLegendPage.value - 1
      ) * popularityPageSize.value

    return flavorRanking.value
      .slice(
        startIndex,
        startIndex +
          popularityPageSize.value
      )
      .map(
        (flavor, index) => {
          const ranking =
            startIndex + index + 1

          return {
            ...flavor,
            ranking,
            /*
             * 11위 이하 항목은 도넛의 기타 색상으로 표시합니다.
             */
            colorIndex:
              ranking <= 10
                ? ranking - 1
                : 10
          }
        }
      )
  })

/*
 * 한 번에 표시할 페이지 버튼 수
 *
 * 예:
 * 1~5
 * 6~10
 * 11~15
 */
 const PAGE_GROUP_SIZE = 5

const createPageGroup = (
  currentPage,
  totalPages
) => {
  /*
   * 현재 페이지가 포함된 5개 단위 페이지 그룹
   */
  const visiblePages = computed(() => {
    const groupStart =
      Math.floor(
        (
          currentPage.value - 1
        ) / PAGE_GROUP_SIZE
      ) * PAGE_GROUP_SIZE + 1

    const groupEnd =
      Math.min(
        groupStart +
          PAGE_GROUP_SIZE -
          1,

        totalPages.value
      )

    return Array.from(
      {
        length:
          groupEnd -
          groupStart +
          1
      },

      (_, index) =>
        groupStart + index
    )
  })

  /*
   * 이전 페이지 그룹 존재 여부
   */
  const hasPreviousGroup =
    computed(() => {
      const firstPage =
        visiblePages.value[0] ?? 1

      return firstPage > 1
    })

  /*
   * 다음 페이지 그룹 존재 여부
   */
  const hasNextGroup =
    computed(() => {
      const pages =
        visiblePages.value

      const lastPage =
        pages[
          pages.length - 1
        ] ?? 1

      return (
        lastPage <
        totalPages.value
      )
    })

  /*
   * 이전 그룹으로 이동
   *
   * 6~10
   * →
   * 5페이지로 이동하면서 1~5 출력
   */
  const goPreviousGroup = () => {
    if (!hasPreviousGroup.value) {
      return
    }

    const firstPage =
      visiblePages.value[0]

    currentPage.value =
      firstPage - 1
  }

  /*
   * 다음 그룹으로 이동
   *
   * 1~5
   * →
   * 6페이지로 이동하면서 6~10 출력
   */
  const goNextGroup = () => {
    if (!hasNextGroup.value) {
      return
    }

    const pages =
      visiblePages.value

    const lastPage =
      pages[
        pages.length - 1
      ]

    currentPage.value =
      lastPage + 1
  }

  return {
    visiblePages,
    hasPreviousGroup,
    hasNextGroup,
    goPreviousGroup,
    goNextGroup
  }
}

const {
  visiblePages:
    productVisiblePages,

  hasPreviousGroup:
    productHasPreviousGroup,

  hasNextGroup:
    productHasNextGroup,

  goPreviousGroup:
    goPreviousProductGroup,

  goNextGroup:
    goNextProductGroup
} = createPageGroup(
  productRankingPage,
  productRankingTotalPages
)

const {
  visiblePages:
    flavorVisiblePages,

  hasPreviousGroup:
    flavorHasPreviousGroup,

  hasNextGroup:
    flavorHasNextGroup,

  goPreviousGroup:
    goPreviousFlavorGroup,

  goNextGroup:
    goNextFlavorGroup
} = createPageGroup(
  flavorRankingPage,
  flavorRankingTotalPages
)

const {
  visiblePages:
    donutVisiblePages,

  hasPreviousGroup:
    donutHasPreviousGroup,

  hasNextGroup:
    donutHasNextGroup,

  goPreviousGroup:
    goPreviousDonutGroup,

  goNextGroup:
    goNextDonutGroup
} = createPageGroup(
  donutLegendPage,
  donutLegendTotalPages
)

/*
 * 날짜 및 조회 조건
 */
const getLocalDateString = (date) => {
  const year = date.getFullYear()

  const month = String(
    date.getMonth() + 1
  ).padStart(2, '0')

  const day = String(
    date.getDate()
  ).padStart(2, '0')

  return `${year}-${month}-${day}`
}

const today = new Date()

const firstDayOfMonth =
  new Date(
    today.getFullYear(),
    today.getMonth(),
    1
  )

const filters = reactive({
  startDate:
    getLocalDateString(firstDayOfMonth),

  endDate:
    getLocalDateString(today),

  storeId: '',

  period: 'DAILY'
})

const periodOptions = [
  {
    value: 'DAILY',
    label: '일별'
  },
  {
    value: 'WEEKLY',
    label: '주별'
  },
  {
    value: 'MONTHLY',
    label: '월별'
  },
  {
    value: 'YEARLY',
    label: '연도별'
  }
]

/*
 * 공통 계산 및 정규화
 */
const selectedStoreName = computed(() => {
  if (!filters.storeId) {
    return '전체 지점'
  }

  const store = stores.value.find(
    (item) =>
      String(item.storeId) ===
      String(filters.storeId)
  )

  return store?.storeName || '선택 지점'
})

const maxTrendSales = computed(() => {
  const values = displayedTrend.value.map(
    (item) =>
      Number(item.totalSales ?? 0)
  )

  return Math.max(
    ...values,
    1
  )
})

const normalizeStore = (store = {}) => ({
  storeId:
    store.storeId ??
    store.id,

  storeName:
    store.storeName ??
    store.name ??
    '지점명 없음'
})

const normalizeSummary = (data = {}) => ({
  totalSales:
    Number(data.totalSales ?? 0),

  orderCount:
    Number(data.orderCount ?? 0),

  salesQuantity:
    Number(data.salesQuantity ?? 0),

  averageOrderAmount:
    Number(data.averageOrderAmount ?? 0)
})

const normalizeTrend = (item = {}) => ({
  periodLabel:
    item.periodLabel ?? '-',

  totalSales:
    Number(item.totalSales ?? 0),

  orderCount:
    Number(item.orderCount ?? 0),

  salesQuantity:
    Number(item.salesQuantity ?? 0)
})

const normalizeFlavor = (
  flavor = {}
) => ({
  flavorId:
    flavor.flavorId ??
    flavor.id ??
    null,

  flavorName:
    flavor.flavorName ??
    flavor.name ??
    '맛 이름 없음',

  salesQuantity:
    Number(
      flavor.salesQuantity ?? 0
    ),

  orderCount:
    Number(
      flavor.orderCount ?? 0
    ),

  salesAmount:
    Number(
      flavor.salesAmount ?? 0
    )
})

const formatMoney = (value) => {
  return `${Number(value ?? 0)
    .toLocaleString('ko-KR')}원`
}

const formatNumber = (value) => {
  return Number(value ?? 0)
    .toLocaleString('ko-KR')
}

const formatCompactMoney = (value) => {
  const amount =
    Number(value ?? 0)

  if (amount >= 100000000) {
    return `${(
      amount / 100000000
    ).toFixed(1)}억`
  }

  if (amount >= 10000) {
    return `${(
      amount / 10000
    ).toFixed(1)}만`
  }

  return amount.toLocaleString('ko-KR')
}

const formatPeriodLabel = (label) => {
  if (!label) {
    return '-'
  }

  if (filters.period === 'YEARLY') {
    return `${label}년`
  }

  if (filters.period === 'MONTHLY') {
    const [year, month] =
      label.split('-')

    return `${year}.${month}`
  }

  const date = new Date(
    `${label}T00:00:00`
  )

  if (Number.isNaN(date.getTime())) {
    return label
  }

  const formatted =
    `${date.getMonth() + 1}/${date.getDate()}`

  return filters.period === 'WEEKLY'
    ? `${formatted} 주`
    : formatted
}

const getBarHeight = (item) => {
  const amount =
    Number(item.totalSales ?? 0)

  if (amount <= 0) {
    return 3
  }

  return Math.max(
    8,
    Math.round(
      amount /
      maxTrendSales.value *
      100
    )
  )
}

const validateFilters = () => {
  if (
    !filters.startDate ||
    !filters.endDate
  ) {
    message.value =
      '조회 시작일과 종료일을 선택해주세요.'

    return false
  }

  if (
    filters.endDate <
    filters.startDate
  ) {
    message.value =
      '종료일은 시작일보다 빠를 수 없습니다.'

    return false
  }

  return true
}

const createRequestFilters = () => ({
  startDate:
    filters.startDate,

  endDate:
    filters.endDate,

  storeId:
    filters.storeId || null,

  period:
    filters.period,

  /*
   * 상품 순위는 상위 10개까지만 사용합니다.
   */
  limit: 10
})

/*
 * 지점 목록 조회
 */
const loadStores = async () => {
  try {
    const responseBody =
      await getHeadStores()

    const responseData =
      extractStoreData(responseBody)

    stores.value =
      Array.isArray(responseData)
        ? responseData.map(normalizeStore)
        : []

  } catch (error) {
    message.value =
      extractStatisticsErrorMessage(
        error,
        '지점 목록을 불러오지 못했습니다.'
      )
  }
}

/*
 * 통계 조회
 */
const loadStatistics = async (
  clearPreviousMessage = true
) => {
  if (!validateFilters()) {
    return
  }

  loading.value = true

  if (clearPreviousMessage) {
    clearMessage()
  }

  try {
    const requestFilters =
      createRequestFilters()

    /*
     * 기타 수량을 계산하려면 10위 이하 데이터도 필요합니다.
     * 현재 백엔드 최대 limit이 50이므로 50개까지 요청합니다.
     */
    const flavorRequestFilters = {
      ...requestFilters,
      limit: 50
    }

    const [
      summaryResponse,
      trendResponse,
      storeResponse,
      productResponse,
      flavorResponse
    ] = await Promise.all([
      getHeadStatisticsSummary(
        requestFilters
      ),

      getHeadSalesTrend(
        requestFilters
      ),

      getHeadStoreSalesRanking(
        requestFilters
      ),

      getHeadProductSalesRanking(
        requestFilters
      ),

      getHeadFlavorSalesRanking(
        flavorRequestFilters
      )
    ])

    const summaryData =
      normalizeSummary(
        extractStatisticsData(
          summaryResponse
        )
      )

    Object.assign(
      summary,
      summaryData
    )

    const trendData =
      extractStatisticsData(
        trendResponse
      )

    trend.value =
      Array.isArray(trendData)
        ? trendData.map(
            normalizeTrend
          )
        : []

    const storeData =
      extractStatisticsData(
        storeResponse
      )

    storeRanking.value =
      Array.isArray(storeData)
        ? storeData
        : []

    const productData =
      extractStatisticsData(
        productResponse
      )

    productRanking.value =
      Array.isArray(productData)
        ? productData
        : []

    const flavorData =
      extractStatisticsData(
        flavorResponse
      )

    flavorRanking.value =
      Array.isArray(flavorData)
        ? flavorData.map(
            normalizeFlavor
          )
        : []

    /*
     * 조회 조건이 바뀌면 모든 내부 페이지를 처음으로 돌립니다.
     */
    productRankingPage.value = 1
    flavorRankingPage.value = 1
    donutLegendPage.value = 1

  } catch (error) {
    trend.value = []
    storeRanking.value = []
    productRanking.value = []
    flavorRanking.value = []

    productRankingPage.value = 1
    flavorRankingPage.value = 1
    donutLegendPage.value = 1

    Object.assign(
      summary,
      {
        totalSales: 0,
        orderCount: 0,
        salesQuantity: 0,
        averageOrderAmount: 0
      }
    )

    message.value =
      extractStatisticsErrorMessage(
        error,
        '통계 정보를 불러오지 못했습니다.'
      )

  } finally {
    loading.value = false
  }
}

const setPeriod = async (period) => {
  filters.period = period

  await loadStatistics()
}

onMounted(async () => {
  clearMessage()

  await Promise.all([
    loadStores(),
    loadStatistics(false)
  ])
})
</script>

<template>
  <AppMessageToast
    :message="message"
    type="error"
    @close="clearMessage"
  />

  <section class="statistics-page">
    <!-- 조회 조건 -->
    <section class="filter-panel">
      <div>
        <p class="section-label">
          SALES ANALYTICS
        </p>

        <h2>통계 조회 조건</h2>

        <p>
          결제 완료 데이터를 기준으로 매출과 판매량을 집계합니다.
        </p>
      </div>

      <div class="filter-controls">
        <label>
          <span>시작일</span>

          <input
            v-model="filters.startDate"
            type="date"
          />
        </label>

        <label>
          <span>종료일</span>

          <input
            v-model="filters.endDate"
            type="date"
          />
        </label>

        <label>
          <span>지점</span>

          <select v-model="filters.storeId">
            <option value="">
              전체 지점
            </option>

            <option
              v-for="store in stores"
              :key="store.storeId"
              :value="store.storeId"
            >
              {{ store.storeName }}
            </option>
          </select>
        </label>

        <button
          type="button"
          class="search-button"
          :disabled="loading"
          @click="loadStatistics"
        >
          {{
            loading
              ? '조회 중...'
              : '통계 조회'
          }}
        </button>
      </div>
    </section>

    <!-- 요약 카드 -->
    <div class="summary-grid">
      <article>
        <div>
          <p>총 결제 매출</p>

          <strong>
            {{ formatMoney(summary.totalSales) }}
          </strong>

          <small>
            {{ selectedStoreName }}
          </small>
        </div>

        <span class="summary-icon purple">
          ₩
        </span>
      </article>

      <article>
        <div>
          <p>결제 주문 수</p>

          <strong>
            {{ formatNumber(summary.orderCount) }}건
          </strong>

          <small>취소·환불 제외</small>
        </div>

        <span class="summary-icon blue">
          ▤
        </span>
      </article>

      <article>
        <div>
          <p>판매 상품 수량</p>

          <strong>
            {{ formatNumber(summary.salesQuantity) }}개
          </strong>

          <small>주문 상세 수량 합계</small>
        </div>

        <span class="summary-icon green">
          ▣
        </span>
      </article>

      <article>
        <div>
          <p>평균 주문 금액</p>

          <strong>
            {{
              formatMoney(
                summary.averageOrderAmount
              )
            }}
          </strong>

          <small>매출 ÷ 주문 수</small>
        </div>

        <span class="summary-icon pink">
          ∅
        </span>
      </article>
    </div>

    <!-- 매출 추이 -->
    <section class="chart-panel">
      <header class="panel-header">
        <div>
          <h2>기간별 매출 추이</h2>

          <p>
            실제 결제 완료 시간을 기준으로 집계합니다.
          </p>
        </div>
        <label class="graph-count-control">
          그래프 표시
          <select v-model="graphDisplayCount">
            <option value="5">5개</option>
            <option value="10">10개</option>
            <option value="50">50개</option>
            <option value="100">100개</option>
            <option value="500">500개</option>
            <option value="1000">1000개</option>
            <option value="ALL">전체</option>
          </select>
        </label>

        <div class="period-buttons">
          <button
            v-for="option in periodOptions"
            :key="option.value"
            type="button"
            :class="{
              active:
                filters.period === option.value
            }"
            @click="setPeriod(option.value)"
          >
            {{ option.label }}
          </button>
        </div>
      </header>

      <div
        v-if="loading"
        class="loading-area"
      >
        <span class="spinner" />
        <p>통계를 계산하고 있습니다.</p>
      </div>

      <div
        v-else-if="displayedTrend.length > 0"
        class="chart-scroll"
      >
        <div :key="graphAnimationKey" class="chart-bars">
          <article
            v-for="(item, index) in displayedTrend"
            :key="item.periodLabel"
            class="chart-item"
          >
            <strong>
              {{
                formatCompactMoney(
                  item.totalSales
                )
              }}
            </strong>

            <div class="bar-track">
              <div
                class="bar"
                :style="{
                  height:
                    `${getBarHeight(item)}%`,
                  animationDelay:
                    `${(displayedTrend.length - index - 1) * 70}ms`
                }"
              />
            </div>

            <span>
              {{
                formatPeriodLabel(
                  item.periodLabel
                )
              }}
            </span>

            <small>
              {{ item.orderCount }}건
            </small>
          </article>
        </div>
      </div>

      <div
        v-else
        class="empty-area"
      >
        조회 기간에 결제 데이터가 없습니다.
      </div>
    </section>

    <div class="ranking-grid">
      <!-- 지점 순위 -->
      <section class="ranking-panel">
        <header class="panel-header">
          <div>
            <h2>지점별 매출 순위</h2>

            <p>
              최종 결제 금액 기준 상위 10개 지점
            </p>
          </div>
        </header>

        <div class="table-scroll">
          <table>
            <thead>
              <tr>
                <th>순위</th>
                <th>지점</th>
                <th>매출</th>
                <th>주문</th>
                <th>판매량</th>
              </tr>
            </thead>

            <tbody>
              <tr
                v-for="(
                  store,
                  index
                ) in storeRanking"
                :key="store.storeId"
              >
                <td>
                  <span
                    class="rank-badge"
                    :class="{
                      top: index < 3
                    }"
                  >
                    {{ index + 1 }}
                  </span>
                </td>

                <td>
                  <strong>
                    {{ store.storeName }}
                  </strong>

                  <small>
                    지점 #{{ store.storeId }}
                  </small>
                </td>

                <td class="money-cell">
                  {{
                    formatMoney(
                      store.totalSales
                    )
                  }}
                </td>

                <td>
                  {{ formatNumber(store.orderCount) }}건
                </td>

                <td>
                  {{
                    formatNumber(
                      store.salesQuantity
                    )
                  }}개
                </td>
              </tr>

              <tr
                v-if="
                  storeRanking.length === 0
                "
              >
                <td
                  colspan="5"
                  class="empty-cell"
                >
                  지점별 매출 데이터가 없습니다.
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>

      <!-- 인기 분석 -->
      <section class="ranking-panel popularity-panel">
        <header class="panel-header popularity-header">
          <div>
            <h2>인기 분석</h2>

            <p>
              상품과 맛의 판매 실적을 분석합니다.
            </p>
          </div>

          <div class="popularity-tabs">
            <label class="popularity-page-size">
              페이지당
              <select v-model.number="popularityPageSize">
                <option :value="5">5개</option>
                <option :value="10">10개</option>
                <option :value="20">20개</option>
                <option :value="50">50개</option>
              </select>
            </label>
            <button
              type="button"
              :class="{
                active:
                  popularityView === 'PRODUCT'
              }"
              @click="
                popularityView = 'PRODUCT'
              "
            >
              상품 순위
            </button>

            <button
              type="button"
              :class="{
                active:
                  popularityView === 'FLAVOR'
              }"
              @click="
                popularityView = 'FLAVOR'
              "
            >
              맛 순위
            </button>

            <button
              type="button"
              :class="{
                active:
                  popularityView === 'DONUT'
              }"
              @click="
                popularityView = 'DONUT'
              "
            >
              맛 비율
            </button>
          </div>
        </header>

        <!-- 상품 순위 -->
        <template
          v-if="
            popularityView === 'PRODUCT'
          "
        >
          <div class="table-scroll">
            <table>
              <thead>
                <tr>
                  <th>순위</th>
                  <th>상품</th>
                  <th>판매량</th>
                  <th>주문</th>
                  <th>상품 금액</th>
                </tr>
              </thead>

              <tbody>
                <tr
                  v-for="(
                    product,
                    index
                  ) in pagedProductRanking"
                  :key="product.productId"
                >
                  <td>
                    <span
                      class="rank-badge"
                      :class="{
                        top:
                          getProductRankingNumber(
                            index
                          ) <= 3
                      }"
                    >
                      {{
                        getProductRankingNumber(
                          index
                        )
                      }}
                    </span>
                  </td>

                  <td>
                    <strong>
                      {{ product.productName }}
                    </strong>

                    <small>
                      상품 #{{ product.productId }}
                    </small>
                  </td>

                  <td>
                    {{
                      formatNumber(
                        product.salesQuantity
                      )
                    }}개
                  </td>

                  <td>
                    {{
                      formatNumber(
                        product.orderCount
                      )
                    }}건
                  </td>

                  <td class="money-cell">
                    {{
                      formatMoney(
                        product.salesAmount
                      )
                    }}
                  </td>
                </tr>

                <tr
                  v-if="
                    pagedProductRanking.length === 0
                  "
                >
                  <td
                    colspan="5"
                    class="empty-cell"
                  >
                    상품별 판매 데이터가 없습니다.
                  </td>
                </tr>
              </tbody>
            </table>
          </div>

          <footer
            v-if="productRanking.length > 0"
            class="ranking-pagination"
          >
            <span>
              전체
              {{ productRanking.length }}개 상품
            </span>

            <div>
              <!-- 이전 5페이지 그룹 -->
              <button
                type="button"
                :disabled="
                  !productHasPreviousGroup
                "
                @click="
                  goPreviousProductGroup
                "
              >
                ‹
              </button>

              <!-- 최대 5개만 출력 -->
              <button
                v-for="
                  page in productVisiblePages
                "
                :key="page"
                type="button"
                :class="{
                  active:
                    productRankingPage === page
                }"
                @click="
                  productRankingPage = page
                "
              >
                {{ page }}
              </button>

              <!-- 다음 5페이지 그룹 -->
              <button
                type="button"
                :disabled="
                  !productHasNextGroup
                "
                @click="
                  goNextProductGroup
                "
              >
                ›
              </button>
            </div>
          </footer>
        </template>

        <!-- 맛 순위 -->
        <template
          v-else-if="
            popularityView === 'FLAVOR'
          "
        >
          <div class="table-scroll">
            <table>
              <thead>
                <tr>
                  <th>순위</th>
                  <th>맛</th>
                  <th>판매량</th>
                  <th>주문</th>
                  <th>판매 금액</th>
                </tr>
              </thead>

              <tbody>
                <tr
                  v-for="(
                    flavor,
                    index
                  ) in pagedFlavorRanking"
                  :key="
                    flavor.flavorId ??
                    flavor.flavorName
                  "
                >
                  <td>
                    <span
                      class="rank-badge"
                      :class="{
                        top:
                          getFlavorRankingNumber(
                            index
                          ) <= 3
                      }"
                    >
                      {{
                        getFlavorRankingNumber(
                          index
                        )
                      }}
                    </span>
                  </td>

                  <td>
                    <strong>
                      {{ flavor.flavorName }}
                    </strong>

                    <small>
                      맛 #{{ flavor.flavorId }}
                    </small>
                  </td>

                  <td>
                    {{
                      formatNumber(
                        flavor.salesQuantity
                      )
                    }}개
                  </td>

                  <td>
                    {{
                      formatNumber(
                        flavor.orderCount
                      )
                    }}건
                  </td>

                  <td class="money-cell">
                    {{
                      formatMoney(
                        flavor.salesAmount
                      )
                    }}
                  </td>
                </tr>

                <tr
                  v-if="
                    pagedFlavorRanking.length === 0
                  "
                >
                  <td
                    colspan="5"
                    class="empty-cell"
                  >
                    맛별 판매 데이터가 없습니다.
                  </td>
                </tr>
              </tbody>
            </table>
          </div>

          <footer
            v-if="flavorRanking.length > 0"
            class="ranking-pagination"
          >
            <span>
              전체
              {{ flavorRanking.length }}개 맛
            </span>

            <div>
              <button
                type="button"
                :disabled="
                  !flavorHasPreviousGroup
                "
                @click="
                  goPreviousFlavorGroup
                "
              >
                ‹
              </button>

              <button
                v-for="
                  page in flavorVisiblePages
                "
                :key="page"
                type="button"
                :class="{
                  active:
                    flavorRankingPage === page
                }"
                @click="
                  flavorRankingPage = page
                "
              >
                {{ page }}
              </button>

              <button
                type="button"
                :disabled="
                  !flavorHasNextGroup
                "
                @click="
                  goNextFlavorGroup
                "
              >
                ›
              </button>
            </div>
          </footer>
        </template>

        <!-- 맛 판매 비율 -->
        <template v-else>
          <div class="donut-content">
            <!-- 왼쪽: 상위 10개 + 기타 고정 도넛 -->
            <div class="donut-chart-wrapper">
              <div
                :key="graphAnimationKey"
                class="donut-chart"
                :style="{
                  background:
                    donutBackground
                }"
              >
                <div class="donut-center">
                  <strong>
                    {{
                      formatNumber(
                        totalFlavorSalesQuantity
                      )
                    }}
                  </strong>

                  <span>전체 맛 판매량</span>
                </div>
              </div>

              <div class="donut-summary">
                <span>
                  상위 {{ popularityPageSize }}개 맛
                </span>

                <span
                  v-if="
                    otherFlavorSalesQuantity > 0
                  "
                >
                  기타
                  {{
                    formatNumber(
                      otherFlavorSalesQuantity
                    )
                  }}개 ·
                  {{ otherFlavorPercentage }}%
                </span>
              </div>
            </div>

            <!-- 오른쪽: 독립 페이징 순위 목록 -->
            <div class="donut-side">
              <div class="donut-legend">
                <article
                  v-for="
                    flavor in pagedDonutLegend
                  "
                  :key="
                    `legend-${
                      flavor.flavorId ??
                      flavor.flavorName
                    }`
                  "
                >
                  <span
                    class="legend-color"
                    :style="{
                      background:
                        getFlavorColor(
                          flavor.colorIndex
                        )
                    }"
                  />

                  <span class="legend-rank">
                    {{ flavor.ranking }}위
                  </span>

                  <div>
                    <strong>
                      {{ flavor.flavorName }}
                    </strong>

                    <small>
                      {{
                        formatNumber(
                          flavor.salesQuantity
                        )
                      }}개
                    </small>
                  </div>

                  <b>
                    {{
                      getFlavorPercentage(
                        flavor
                      )
                    }}%
                  </b>
                </article>

                <div
                  v-if="
                    pagedDonutLegend.length === 0
                  "
                  class="empty-donut"
                >
                  맛별 판매 데이터가 없습니다.
                </div>
              </div>

              <footer
                v-if="flavorRanking.length > 0"
                class="
                  ranking-pagination
                  donut-pagination
                "
              >
                <span>
                  전체
                  {{ flavorRanking.length }}개 맛
                </span>

                <div>
                  <button
                    type="button"
                    :disabled="
                      !donutHasPreviousGroup
                    "
                    @click="
                      goPreviousDonutGroup
                    "
                  >
                    ‹
                  </button>

                  <button
                    v-for="
                      page in donutVisiblePages
                    "
                    :key="page"
                    type="button"
                    :class="{
                      active:
                        donutLegendPage === page
                    }"
                    @click="
                      donutLegendPage = page
                    "
                  >
                    {{ page }}
                  </button>

                  <button
                    type="button"
                    :disabled="
                      !donutHasNextGroup
                    "
                    @click="
                      goNextDonutGroup
                    "
                  >
                    ›
                  </button>
                </div>
              </footer>
            </div>
          </div>
        </template>
      </section>
    </div>

    <div class="statistics-note">
      <strong>집계 기준</strong>

      <p>
        결제 상태가 PAID이고 주문 상태가 CANCELED가 아닌 주문만 포함합니다.
        환불된 결제는 제외됩니다.
      </p>
    </div>
  </section>
</template>

<style scoped>
.statistics-page {
  display: grid;
  gap: 18px;
}

.filter-panel {
  display: flex;
  gap: 25px;
  align-items: flex-end;
  justify-content: space-between;
  padding: 19px;
  border: 1px solid #e2e6ed;
  border-radius: 15px;
  background: #ffffff;
}

.section-label {
  margin: 0;
  color: #715ee6;
  font-size: 9px;
  font-weight: 900;
  letter-spacing: 1.2px;
}

.filter-panel h2 {
  margin: 5px 0 3px;
  font-size: 17px;
}

.filter-panel p {
  margin: 0;
  color: #9299a8;
  font-size: 10px;
}

.filter-controls {
  display: flex;
  gap: 9px;
  align-items: flex-end;
}

.filter-controls label {
  display: grid;
  gap: 6px;
}

.filter-controls label span {
  color: #737b89;
  font-size: 9px;
  font-weight: 750;
}

.filter-controls input,
.filter-controls select {
  height: 38px;
  padding: 0 10px;
  border: 1px solid #dfe3ea;
  border-radius: 8px;
  outline: none;
  font-size: 10px;
  background: #ffffff;
}

.search-button {
  height: 38px;
  padding: 0 16px;
  border: 0;
  border-radius: 8px;
  cursor: pointer;
  color: #ffffff;
  font-size: 10px;
  font-weight: 800;
  background: #725ee7;
}

.search-button:disabled {
  cursor: not-allowed;
  opacity: 0.65;
}

.summary-grid {
  display: grid;
  grid-template-columns:
    repeat(4, minmax(0, 1fr));
  gap: 14px;
}

.summary-grid article {
  display: flex;
  justify-content: space-between;
  min-height: 130px;
  padding: 18px;
  border: 1px solid #e3e6ed;
  border-radius: 15px;
  background: #ffffff;
}

.summary-grid p {
  margin: 0;
  color: #798191;
  font-size: 11px;
  font-weight: 700;
}

.summary-grid strong {
  display: block;
  margin: 13px 0 7px;
  font-size: 21px;
}

.summary-grid small {
  color: #969dab;
  font-size: 9px;
}

.summary-icon {
  display: grid;
  width: 36px;
  height: 36px;
  place-items: center;
  border-radius: 10px;
  font-weight: 900;
}

.purple {
  color: #6654df;
  background: #edeaff;
}

.blue {
  color: #3978d4;
  background: #e3f0ff;
}

.green {
  color: #159b68;
  background: #dff9ed;
}

.pink {
  color: #df4c87;
  background: #ffe4f0;
}

.chart-panel,
.ranking-panel {
  overflow: hidden;
  border: 1px solid #e2e6ed;
  border-radius: 15px;
  background: #ffffff;
}

.panel-header {
  display: flex;
  gap: 15px;
  align-items: center;
  justify-content: space-between;
  padding: 17px 18px;
  border-bottom: 1px solid #e8ebf0;
}

.panel-header h2 {
  margin: 0;
  font-size: 14px;
}

.panel-header p {
  margin: 5px 0 0;
  color: #9299a8;
  font-size: 9px;
}

.period-buttons {
  display: flex;
  padding: 3px;
  border-radius: 9px;
  background: #f0f1f5;
}

.period-buttons button {
  height: 29px;
  padding: 0 11px;
  border: 0;
  border-radius: 7px;
  cursor: pointer;
  color: #747c8a;
  font-size: 9px;
  background: transparent;
}

.period-buttons button.active {
  color: #6756dc;
  font-weight: 800;
  background: #ffffff;
  box-shadow:
    0 2px 7px rgba(46, 50, 69, 0.08);
}

.chart-scroll {
  overflow-x: auto;
  padding: 21px 18px 15px;
}

.chart-bars {
  display: flex;
  gap: 13px;
  align-items: flex-end;
  min-width: max-content;
  height: 260px;
}

.chart-item {
  display: grid;
  grid-template-rows:
    22px 180px 18px 15px;
  align-items: end;
  width: 58px;
  text-align: center;
}

.chart-item > strong {
  overflow: hidden;
  color: #575f6e;
  font-size: 8px;
  text-overflow: ellipsis;
}

.bar-track {
  position: relative;
  overflow: hidden;
  width: 27px;
  height: 170px;
  margin: 0 auto;
  border-radius: 7px 7px 3px 3px;
  background: #f0eefc;
}

.bar {
  position: absolute;
  right: 0;
  bottom: 0;
  left: 0;
  min-height: 4px;
  transform-origin: bottom;
  animation: grow-stat-bar .75s cubic-bezier(.2,.8,.2,1) both;
  border-radius: 7px 7px 3px 3px;
  background:
    linear-gradient(
      180deg,
      #8f7ff0,
      #6754dd
    );
}

.chart-item > span {
  color: #656d7b;
  font-size: 8px;
}

.chart-item > small {
  color: #9ba2b0;
  font-size: 8px;
}

.ranking-grid {
  display: grid;
  grid-template-columns:
    repeat(2, minmax(0, 1fr));
  gap: 14px;
}

.table-scroll {
  overflow-x: auto;
}

table {
  width: 100%;
  min-width: 530px;
  border-collapse: collapse;
}

th,
td {
  padding: 12px 14px;
  text-align: left;
  font-size: 9px;
}

th {
  color: #7e8696;
  background: #fafbfc;
}

td {
  border-top: 1px solid #edf0f4;
  color: #515867;
}

td strong,
td small {
  display: block;
}

td small {
  margin-top: 4px;
  color: #9aa1af;
  font-size: 8px;
}

.rank-badge {
  display: grid;
  width: 25px;
  height: 25px;
  place-items: center;
  border-radius: 7px;
  color: #767e8d;
  font-weight: 800;
  background: #edf0f4;
}

.rank-badge.top {
  color: #6756dc;
  background: #edeaff;
}

.money-cell {
  color: #d84774;
  font-weight: 800;
}

.empty-cell {
  height: 130px;
  text-align: center;
  color: #9097a6;
}

.loading-area,
.empty-area {
  display: flex;
  flex-direction: column;
  gap: 12px;
  align-items: center;
  justify-content: center;
  min-height: 240px;
  color: #9097a6;
  font-size: 10px;
}

.spinner {
  width: 28px;
  height: 28px;
  border: 3px solid #e7e4fa;
  border-top-color: #715ee6;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

.statistics-note {
  padding: 14px 16px;
  border: 1px solid #e3e0f7;
  border-radius: 11px;
  color: #666e7d;
  background: #f7f5ff;
}

.statistics-note strong {
  color: #6756dc;
  font-size: 10px;
}

.statistics-note p {
  margin: 5px 0 0;
  font-size: 9px;
}

/*
 * 인기 분석
 */
.popularity-header {
  align-items: center;
}

.popularity-tabs {
  display: flex;
  padding: 3px;
  border-radius: 9px;
  background: #f0f1f5;
}

.popularity-tabs button {
  height: 29px;
  padding: 0 10px;
  border: 0;
  border-radius: 7px;
  cursor: pointer;
  color: #7a8290;
  font-size: 8px;
  font-weight: 750;
  background: transparent;
}

.popularity-tabs button.active {
  color: #6857df;
  background: #ffffff;
  box-shadow:
    0 2px 7px
    rgba(46, 50, 69, 0.08);
}

/*
 * 페이지네이션
 */
 .ranking-pagination {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
  justify-content: space-between;

  padding: 11px 14px;

  border-top: 1px solid #e8ebf0;

  color: #8b93a2;
  font-size: 8px;
}

.ranking-pagination > span {
  flex-shrink: 0;

  white-space: nowrap;
}

.ranking-pagination > div {
  display: flex;
  flex-shrink: 0;
  gap: 4px;

  white-space: nowrap;
}

.ranking-pagination button {
  min-width: 27px;
  height: 27px;
  border: 1px solid #e0e4eb;
  border-radius: 7px;
  cursor: pointer;
  color: #6c7482;
  font-size: 9px;
  background: #ffffff;
}

.ranking-pagination button.active {
  border-color: #705fe8;
  color: #ffffff;
  background: #705fe8;
}

.ranking-pagination button:disabled {
  cursor: not-allowed;
  opacity: 0.4;
}

/*
 * 도넛 그래프
 */
.donut-content {
  display: grid;
  grid-template-columns:
    minmax(190px, 0.8fr)
    minmax(250px, 1.2fr);
  gap: 20px;
  align-items: center;
  min-height: 360px;
  padding: 24px;
}

.donut-chart-wrapper {
  display: flex;
  flex-direction: column;
  gap: 14px;
  align-items: center;
  justify-content: center;
}

.donut-chart {
  position: relative;
  animation: reveal-stat-donut .8s cubic-bezier(.2,.8,.2,1) both;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 185px;
  height: 185px;
  border-radius: 50%;
  transition:
    background 0.3s ease;
}

.donut-center {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 105px;
  height: 105px;
  border-radius: 50%;
  background: #ffffff;
  box-shadow:
    0 3px 12px
    rgba(45, 50, 70, 0.08);
}

.donut-center strong {
  color: #292e3b;
  font-size: 20px;
}

.donut-center span {
  margin-top: 4px;
  color: #9199a8;
  font-size: 8px;
}

.donut-summary {
  display: flex;
  flex-wrap: wrap;
  gap: 7px;
  justify-content: center;
}

.donut-summary span {
  padding: 5px 8px;
  border-radius: 20px;
  color: #767e8d;
  font-size: 8px;
  background: #f0f1f5;
}

.donut-side {
  display: grid;
  gap: 10px;
  min-width: 0;
}

.donut-legend {
  display: grid;
  gap: 7px;
}

.donut-legend article {
  display: grid;
  grid-template-columns:
    10px 30px minmax(0, 1fr) auto;
  gap: 9px;
  align-items: center;
  padding: 9px 10px;
  border: 1px solid #ebedf2;
  border-radius: 9px;
  background: #fafbfc;
}

.legend-color {
  width: 9px;
  height: 9px;
  border-radius: 50%;
}

.legend-rank {
  color: #6756dc;
  font-size: 8px;
  font-weight: 850;
}

.donut-legend strong,
.donut-legend small {
  display: block;
}

.donut-legend strong {
  overflow: hidden;
  color: #454b59;
  font-size: 9px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.donut-legend small {
  margin-top: 3px;
  color: #9aa1af;
  font-size: 8px;
}

.donut-legend b {
  color: #6756dc;
  font-size: 10px;
}

.donut-pagination {
  align-items: flex-start;
  flex-direction: column;

  padding-right: 0;
  padding-left: 0;
}

.donut-pagination > div {
  width: 100%;

  justify-content: flex-end;
}

.empty-donut {
  padding: 30px;
  color: #969dab;
  font-size: 9px;
  text-align: center;
}

.popularity-page-size {
  display: flex;
  align-items: center;
  gap: 7px;
  margin-right: 6px;
  color: #6c7482;
  font-size: 10px;
  font-weight: 700;
}

.popularity-page-size select {
  padding: 7px 9px;
  border: 1px solid #dfe3ea;
  border-radius: 7px;
  background: #fff;
  color: #4f5665;
}
.graph-count-control {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #6c7482;
  font-size: 10px;
  font-weight: 700;
}
.graph-count-control select {
  padding: 7px 9px;
  border: 1px solid #dfe3ea;
  border-radius: 7px;
  background: #fff;
}

@keyframes grow-stat-bar {
  from {
    opacity: 0;
    transform: scaleY(0);
  }
  to {
    opacity: 1;
    transform: scaleY(1);
  }
}

@keyframes reveal-stat-donut {
  from {
    opacity: 0;
    transform: scale(.72) rotate(55deg);
  }
  to {
    opacity: 1;
    transform: scale(1) rotate(0);
  }
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

@media (max-width: 1150px) {
  .summary-grid {
    grid-template-columns:
      repeat(2, minmax(0, 1fr));
  }

  .filter-panel {
    align-items: flex-start;
    flex-direction: column;
  }

  .filter-controls {
    width: 100%;
    flex-wrap: wrap;
  }
}

@media (max-width: 850px) {
  .ranking-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 600px) {
  .summary-grid {
    grid-template-columns: 1fr;
  }

  .filter-controls label,
  .filter-controls input,
  .filter-controls select,
  .search-button {
    width: 100%;
  }

  .donut-content {
    grid-template-columns: 1fr;
  }

  .popularity-header {
    align-items: flex-start;
    flex-direction: column;
  }

  .popularity-tabs {
    width: 100%;
  }

  .popularity-tabs button {
    flex: 1;
  }

  .ranking-pagination {
    align-items: flex-start;
    flex-direction: column;
    gap: 8px;
  }
}
</style>
