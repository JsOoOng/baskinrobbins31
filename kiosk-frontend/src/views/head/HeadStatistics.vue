<script setup>
import {
  computed,
  onMounted,
  reactive,
  ref
} from 'vue'

import {
  extractStoreData,
  getHeadStores
} from '@/api/head/headStoreApi'

import {
  extractStatisticsData,
  extractStatisticsErrorMessage,
  getHeadProductSalesRanking,
  getHeadSalesTrend,
  getHeadStatisticsSummary,
  getHeadStoreSalesRanking
} from '@/api/head/headStatisticsApi'

const loading = ref(false)
const message = ref('')

const stores = ref([])
const trend = ref([])
const storeRanking = ref([])
const productRanking = ref([])

const summary = reactive({
  totalSales: 0,
  orderCount: 0,
  salesQuantity: 0,
  averageOrderAmount: 0
})

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
  const values = trend.value.map(
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

  limit: 10
})

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

const loadStatistics = async () => {
  if (!validateFilters()) {
    return
  }

  loading.value = true
  message.value = ''

  try {
    const requestFilters =
      createRequestFilters()

    const [
      summaryResponse,
      trendResponse,
      storeResponse,
      productResponse
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
        ? trendData.map(normalizeTrend)
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

  } catch (error) {
    message.value =
      extractStatisticsErrorMessage(
        error
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
  await loadStores()
  await loadStatistics()
})
</script>

<template>
  <section class="statistics-page">
    <div
      v-if="message"
      class="error-message"
    >
      <strong>!</strong>

      <p>{{ message }}</p>

      <button
        type="button"
        @click="message = ''"
      >
        ×
      </button>
    </div>

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
        v-else-if="trend.length > 0"
        class="chart-scroll"
      >
        <div class="chart-bars">
          <article
            v-for="item in trend"
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
                    `${getBarHeight(item)}%`
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

              <tr v-if="storeRanking.length === 0">
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

      <!-- 상품 순위 -->
      <section class="ranking-panel">
        <header class="panel-header">
          <div>
            <h2>인기 상품 순위</h2>

            <p>
              판매 수량 기준 상위 10개 상품
            </p>
          </div>
        </header>

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
                ) in productRanking"
                :key="product.productId"
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
                  productRanking.length === 0
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

.error-message {
  display: flex;
  gap: 10px;
  align-items: center;
  padding: 13px 15px;
  border: 1px solid #ffd0d7;
  border-radius: 11px;
  color: #d64359;
  background: #fff2f4;
}

.error-message strong {
  display: grid;
  width: 22px;
  height: 22px;
  place-items: center;
  border-radius: 50%;
  color: #ffffff;
  background: #eb566b;
}

.error-message p {
  flex: 1;
  margin: 0;
  font-size: 11px;
}

.error-message button {
  border: 0;
  cursor: pointer;
  font-size: 20px;
  background: transparent;
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
}
</style>