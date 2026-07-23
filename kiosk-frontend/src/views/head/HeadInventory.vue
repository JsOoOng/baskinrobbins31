<template>
  <AppMessageToast
    :message="message"
    :type="messageType"
    @close="clearMessage"
  />

  <section class="inventory-page">
    <header class="page-header">
      <div>
        <p class="page-eyebrow">
          INVENTORY MANAGEMENT
        </p>

        <h1>전체 재고 현황</h1>

        <p class="page-description">
          전체 지점의 재고 수량과 자동 보충 설정을 관리합니다.
        </p>
      </div>

      <button
        type="button"
        class="refresh-button"
        :disabled="loading"
        @click="loadInventories(false)"
      >
        {{ loading ? '조회 중...' : '새로고침' }}
      </button>
    </header>

    <!-- 요약 카드 -->
    <div class="summary-grid">
      <article class="summary-card">
        <span class="summary-label">
          전체 재고 항목
        </span>

        <strong class="summary-value">
          {{ formatNumber(inventories.length) }}
        </strong>

        <span class="summary-unit">
          건
        </span>
      </article>

      <article class="summary-card">
        <span class="summary-label">
          부족 재고
        </span>

        <strong class="summary-value danger">
          {{ formatNumber(lowStockCount) }}
        </strong>

        <span class="summary-unit">
          건
        </span>
      </article>

      <article class="summary-card">
        <span class="summary-label">
          자동 보충 사용
        </span>

        <strong class="summary-value success">
          {{ formatNumber(enabledCount) }}
        </strong>

        <span class="summary-unit">
          건
        </span>
      </article>

      <article class="summary-card">
        <span class="summary-label">
          자동 보충 중지
        </span>

        <strong class="summary-value muted">
          {{ formatNumber(disabledCount) }}
        </strong>

        <span class="summary-unit">
          건
        </span>
      </article>
    </div>

    <!-- 검색 및 필터 -->
    <section class="filter-panel">
      <div class="filter-field search-field">
        <label for="inventory-keyword">
          검색
        </label>

        <input
          id="inventory-keyword"
          v-model.trim="keyword"
          type="text"
          placeholder="지점명 또는 상품명 검색"
        />
      </div>

      <div class="filter-field">
        <label for="store-filter">
          지점
        </label>

        <select
          id="store-filter"
          v-model="selectedStoreId"
        >
          <option value="">
            전체 지점
          </option>

          <option
            v-for="store in storeOptions"
            :key="store.storeId"
            :value="String(store.storeId)"
          >
            {{ store.storeName }}
          </option>
        </select>
      </div>

      <div class="filter-field">
        <label for="mode-filter">
          자동 보충 방식
        </label>

        <select
          id="mode-filter"
          v-model="selectedRestockMode"
        >
          <option value="">
            전체 방식
          </option>

          <option value="THRESHOLD">
            임계 재고
          </option>

          <option value="DAILY">
            정기 보충
          </option>

          <option value="BOTH">
            임계 + 정기
          </option>
        </select>
      </div>

      <div class="filter-field">
        <label for="stock-filter">
          재고 상태
        </label>

        <select
          id="stock-filter"
          v-model="selectedStockStatus"
        >
          <option value="">
            전체 상태
          </option>

          <option value="LOW">
            부족
          </option>

          <option value="NORMAL">
            정상
          </option>

          <option value="FULL">
            목표 수량 충족
          </option>
        </select>
      </div>

      <button
        type="button"
        class="reset-button"
        @click="resetFilters"
      >
        필터 초기화
      </button>
    </section>

    <!-- 재고 목록 -->
    <section class="table-panel">
      <div class="table-header">
        <div>
          <h2>재고 목록</h2>

          <p>
            검색 결과 {{ formatNumber(filteredInventories.length) }}건
          </p>
        </div>
      </div>

      <div
        v-if="loading"
        class="empty-state"
      >
        재고 정보를 불러오는 중입니다.
      </div>

      <div
        v-else-if="paginatedInventories.length === 0"
        class="empty-state"
      >
        조건에 맞는 재고 정보가 없습니다.
      </div>

      <div
        v-else
        class="table-scroll"
      >
        <table class="inventory-table">
          <thead>
            <tr>
              <th>번호</th>
              <th>지점</th>
              <th>재고 품목</th>
              <th>단위</th>
              <th>현재 재고</th>
              <th>최소 재고</th>
              <th>목표 재고</th>
              <th>재고 상태</th>
              <th>보충 방식</th>
              <th>자동 보충</th>
              <th>최근 변경</th>
              <th>관리</th>
            </tr>
          </thead>

          <tbody>
            <tr
              v-for="inventory in paginatedInventories"
              :key="inventory.storeInventoryId"
            >
              <td>
                {{ inventory.storeInventoryId }}
              </td>

              <td>
                <strong class="store-name">
                  {{ inventory.storeName }}
                </strong>
              </td>

              <td>
                <div class="item-cell">
                  <strong>
                    {{ inventory.productName || '상품명 없음' }}
                  </strong>

                  <span>
                    상품 번호 {{ inventory.productId }}
                    · 재고 번호 {{ inventory.itemId }}
                  </span>
                </div>
              </td>

              <td>
                {{ inventory.unit }}
              </td>

              <td>
                <strong
                  :class="{
                    'stock-danger':
                      getStockStatus(inventory) === 'LOW'
                  }"
                >
                  {{ formatNumber(inventory.currentStock) }}
                </strong>
              </td>

              <td>
                {{ formatNumber(inventory.minStock) }}
              </td>

              <td>
                {{ formatNumber(inventory.targetStock) }}
              </td>

              <td>
                <span
                  class="status-badge"
                  :class="
                    `status-${getStockStatus(inventory).toLowerCase()}`
                  "
                >
                  {{ getStockStatusLabel(inventory) }}
                </span>
              </td>

              <td>
                <span class="mode-label">
                  {{ getRestockModeLabel(inventory.restockMode) }}
                </span>
              </td>

              <td>
                <button
                  type="button"
                  class="enabled-toggle-button"
                  :class="{
                    active: inventory.autoRestockEnabled
                  }"
                  :disabled="
                    togglingInventoryId ===
                    inventory.storeInventoryId
                  "
                  :title="
                    inventory.autoRestockEnabled
                      ? '클릭하여 자동 보충 중지'
                      : '클릭하여 자동 보충 사용'
                  "
                  @click="toggleAutoRestock(inventory)"
                >
                  {{
                    togglingInventoryId ===
                    inventory.storeInventoryId
                      ? '처리 중'
                      : inventory.autoRestockEnabled
                        ? '사용'
                        : '중지'
                  }}
                </button>
              </td>

              <td>
                {{ formatDate(inventory.lastUpdated) }}
              </td>

              <td>
                <button
                  type="button"
                  class="setting-button"
                  @click="openSettingModal(inventory)"
                >
                  설정
                </button>
                <button
                  v-if="getActiveAlert(inventory.storeInventoryId)"
                  type="button"
                  class="send-alert-button"
                  :disabled="sendingAlertId === getActiveAlert(inventory.storeInventoryId).alertId"
                  @click="sendAlert(getActiveAlert(inventory.storeInventoryId).alertId)"
                  style="margin-left: 5px; background: #e45151; color: white; border: none; padding: 6px 12px; border-radius: 6px; cursor: pointer; font-size: 13px; font-weight: 600;"
                >
                  {{ sendingAlertId === getActiveAlert(inventory.storeInventoryId).alertId ? '전송 중...' : '알림 보내기' }}
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- 페이지네이션 -->
      <nav
        v-if="totalPages > 1"
        class="pagination"
        aria-label="재고 목록 페이지"
      >
        <button
          type="button"
          :disabled="currentPage === 1"
          @click="changePage(1)"
        >
          처음
        </button>

        <button
          type="button"
          :disabled="pageGroupStart === 1"
          @click="movePreviousPageGroup"
        >
          ‹
        </button>

        <button
          v-for="page in visiblePages"
          :key="page"
          type="button"
          class="page-number"
          :class="{ active: currentPage === page }"
          @click="changePage(page)"
        >
          {{ page }}
        </button>

        <button
          type="button"
          :disabled="pageGroupEnd >= totalPages"
          @click="moveNextPageGroup"
        >
          ›
        </button>

        <button
          type="button"
          :disabled="currentPage === totalPages"
          @click="changePage(totalPages)"
        >
          마지막
        </button>
      </nav>
    </section>
  </section>

  <!-- 자동 보충 설정 모달 -->
  <Teleport to="body">
    <div
      v-if="settingModalOpen"
      class="modal-backdrop"
      @click.self="closeSettingModal"
    >
      <section
        class="setting-modal"
        role="dialog"
        aria-modal="true"
        aria-labelledby="inventory-setting-title"
      >
        <header class="modal-header">
          <div>
            <p class="modal-eyebrow">
              AUTO RESTOCK
            </p>

            <h2 id="inventory-setting-title">
              자동 재고 보충 설정
            </h2>
          </div>

          <button
            type="button"
            class="modal-close"
            aria-label="모달 닫기"
            @click="closeSettingModal"
          >
            ×
          </button>
        </header>

        <div
          v-if="selectedInventory"
          class="selected-summary"
        >
          <strong>
            {{ selectedInventory.storeName }}
          </strong>

          <span>
            {{ selectedInventory.productName || '상품명 없음' }}
          </span>

          <span>
            현재 재고
            {{ formatNumber(selectedInventory.currentStock) }}
            {{ selectedInventory.unit }}
          </span>
        </div>

        <form
          class="setting-form"
          @submit.prevent="saveRestockSetting"
        >
          <label class="toggle-row">
            <div>
              <strong>자동 보충 사용</strong>

              <span>
                비활성화하면 자동 재고 보충이 실행되지 않습니다.
              </span>
            </div>

            <input
              v-model="settingForm.autoRestockEnabled"
              type="checkbox"
            />
          </label>

          <div class="form-field">
            <label for="restock-mode">
              자동 보충 방식
            </label>

            <select
              id="restock-mode"
              v-model="settingForm.restockMode"
              :disabled="!settingForm.autoRestockEnabled"
            >
              <option value="THRESHOLD">
                최소 재고 이하일 때 보충
              </option>

              <option value="DAILY">
                정해진 시간에 정기 보충
              </option>

              <option value="BOTH">
                최소 재고 + 정기 보충
              </option>
            </select>
          </div>

          <div class="number-grid">
            <div class="form-field">
              <label for="min-stock">
                최소 재고
              </label>

              <input
                id="min-stock"
                v-model.number="settingForm.minStock"
                type="number"
                min="0"
              />
            </div>

            <div class="form-field">
              <label for="target-stock">
                목표 재고
              </label>

              <input
                id="target-stock"
                v-model.number="settingForm.targetStock"
                type="number"
                min="1"
              />
            </div>
          </div>

          <p class="setting-help">
            목표 재고는 최소 재고보다 커야 합니다.
          </p>

          <footer class="modal-actions">
            <button
              type="button"
              class="cancel-button"
              :disabled="saving"
              @click="closeSettingModal"
            >
              취소
            </button>

            <button
              type="submit"
              class="save-button"
              :disabled="saving"
            >
              {{ saving ? '저장 중...' : '설정 저장' }}
            </button>
          </footer>
        </form>
      </section>
    </div>
  </Teleport>
</template>

<script setup>
import {
  computed,
  onBeforeUnmount,
  onMounted,
  reactive,
  ref,
  watch
} from 'vue'

import AppMessageToast from '@/components/common/AppMessageToast.vue'

import {
  getHeadInventories,
  updateHeadInventoryRestockSetting
} from '@/api/headquarter/headInventoryApi'

import {
  getActiveInventoryShortageAlerts,
  sendInventoryShortageAlertToStore
} from '@/api/headquarter/headInventoryAlertApi'

const inventories = ref([])
const loading = ref(false)
const saving = ref(false)
const togglingInventoryId = ref(null)

const activeAlerts = ref([])
const sendingAlertId = ref(null)

/*
 * 자동 갱신 설정
 *
 * 5000 = 5초
 */
 const AUTO_REFRESH_INTERVAL = 5000

const backgroundRefreshing = ref(false)

let autoRefreshTimer = null

const message = ref('')
const messageType = ref('success')

const keyword = ref('')
const selectedStoreId = ref('')
const selectedRestockMode = ref('')
const selectedStockStatus = ref('')

const currentPage = ref(1)
const pageSize = 10
const pageGroupSize = 5

const settingModalOpen = ref(false)
const selectedInventory = ref(null)

const settingForm = reactive({
  autoRestockEnabled: true,
  restockMode: 'DAILY',
  minStock: 10,
  targetStock: 50
})

const showMessage = (
  text,
  type = 'success'
) => {
  message.value = text
  messageType.value = type
}

const clearMessage = () => {
  message.value = ''
}

const extractErrorMessage = (
  error,
  fallbackMessage
) => {
  return (
    error?.response?.data?.message ||
    error?.response?.data?.error ||
    fallbackMessage
  )
}

const extractList = (response) => {
  const body = response?.data

  if (Array.isArray(body)) {
    return body
  }

  if (Array.isArray(body?.data)) {
    return body.data
  }

  return []
}

const extractItem = (response) => {
  return response?.data?.data ?? response?.data
}

/*
 * 재고 목록 조회
 *
 * silent = false
 * → 사용자가 직접 새로고침
 * → 로딩 화면과 오류 메시지 표시
 *
 * silent = true
 * → 자동 갱신
 * → 화면 깜빡임 없이 데이터만 교체
 */
 const loadInventories = async (
  silent = false
) => {
  const isSilent =
    silent === true

  /*
   * 자동 갱신 요청이 겹치는 것을 방지
   */
  if (
    isSilent &&
    backgroundRefreshing.value
  ) {
    return
  }

  if (isSilent) {
    backgroundRefreshing.value = true
  } else {
    loading.value = true
    clearMessage()
  }

  try {
    const [inventoriesResponse, alertsResponse] = await Promise.all([
      getHeadInventories(),
      getActiveInventoryShortageAlerts()
    ])

    inventories.value =
      extractList(inventoriesResponse)

    activeAlerts.value =
      extractList(alertsResponse)
  } catch (error) {
    /*
     * 자동 갱신 실패 시에는
     * 기존 목록을 지우거나 토스트를 띄우지 않습니다.
     */
    if (isSilent) {
      console.error(
        '[재고 자동 갱신 실패]',
        error
      )

      return
    }

    inventories.value = []

    showMessage(
      extractErrorMessage(
        error,
        '재고 목록을 불러오지 못했습니다.'
      ),
      'error'
    )
  } finally {
    if (isSilent) {
      backgroundRefreshing.value = false
    } else {
      loading.value = false
    }
  }
}

const getActiveAlert = (storeInventoryId) => {
  return activeAlerts.value.find(
    alert => alert.storeInventoryId === storeInventoryId && alert.alertStatus === 'DETECTED'
  )
}

const sendAlert = async (alertId) => {
  if (!alertId || sendingAlertId.value) return

  const headUser = JSON.parse(localStorage.getItem('headUser') || '{}')
  const adminId = headUser.adminId || 1 

  sendingAlertId.value = alertId

  try {
    await sendInventoryShortageAlertToStore(alertId, adminId)
    showMessage('지점으로 재고 부족 알림을 전송했습니다.', 'success')
    loadInventories(true) 
  } catch (error) {
    showMessage(extractErrorMessage(error, '알림 전송에 실패했습니다.'), 'error')
  } finally {
    sendingAlertId.value = null
  }
}

/*
 * 자동 갱신 실행 가능 여부 확인
 */
 const runAutoRefresh = () => {
  /*
   * 다른 브라우저 탭을 보고 있으면
   * 불필요한 요청을 보내지 않습니다.
   */
  if (
    document.visibilityState !==
    'visible'
  ) {
    return
  }

  /*
   * 목록 조회·설정 저장·상태 변경 중에는
   * 자동 갱신을 잠시 건너뜁니다.
   */
  if (
    loading.value ||
    saving.value ||
    backgroundRefreshing.value ||
    togglingInventoryId.value !== null ||
    settingModalOpen.value
  ) {
    return
  }

  loadInventories(true)
}

/*
 * 자동 갱신 시작
 */
const startAutoRefresh = () => {
  stopAutoRefresh()

  autoRefreshTimer =
    window.setInterval(
      runAutoRefresh,
      AUTO_REFRESH_INTERVAL
    )
}

/*
 * 자동 갱신 종료
 */
const stopAutoRefresh = () => {
  if (autoRefreshTimer === null) {
    return
  }

  window.clearInterval(
    autoRefreshTimer
  )

  autoRefreshTimer = null
}

/*
 * 다른 탭에서 돌아왔을 때
 * 즉시 최신 재고 조회
 */
const handleVisibilityChange = () => {
  if (
    document.visibilityState ===
    'visible'
  ) {
    runAutoRefresh()
  }
}

const storeOptions = computed(() => {
  const storeMap = new Map()

  inventories.value.forEach((inventory) => {
    if (!storeMap.has(inventory.storeId)) {
      storeMap.set(
        inventory.storeId,
        {
          storeId: inventory.storeId,
          storeName: inventory.storeName
        }
      )
    }
  })

  return Array
    .from(storeMap.values())
    .sort((a, b) =>
      a.storeId - b.storeId
    )
})

const getStockStatus = (inventory) => {
  const currentStock =
    Number(inventory.currentStock ?? 0)

  const minStock =
    Number(inventory.minStock ?? 0)

  const targetStock =
    Number(inventory.targetStock ?? 0)

  if (currentStock <= minStock) {
    return 'LOW'
  }

  if (
    targetStock > 0 &&
    currentStock >= targetStock
  ) {
    return 'FULL'
  }

  return 'NORMAL'
}

const getStockStatusLabel = (
  inventory
) => {
  const status =
    getStockStatus(inventory)

  const labels = {
    LOW: '부족',
    NORMAL: '정상',
    FULL: '목표 충족'
  }

  return labels[status] ?? '-'
}

const getRestockModeLabel = (mode) => {
  const labels = {
    THRESHOLD: '임계 재고',
    DAILY: '정기 보충',
    BOTH: '임계 + 정기'
  }

  return labels[mode] ?? '-'
}

const filteredInventories = computed(() => {
  const normalizedKeyword =
    keyword.value
      .trim()
      .toLowerCase()

  return inventories.value.filter(
    (inventory) => {
      const matchesKeyword =
        !normalizedKeyword ||
        inventory.storeName
          ?.toLowerCase()
          .includes(normalizedKeyword) ||
        inventory.productName
          ?.toLowerCase()
          .includes(normalizedKeyword)

      const matchesStore =
        !selectedStoreId.value ||
        String(inventory.storeId) ===
          selectedStoreId.value

      const matchesMode =
        !selectedRestockMode.value ||
        inventory.restockMode ===
          selectedRestockMode.value

      const matchesStatus =
        !selectedStockStatus.value ||
        getStockStatus(inventory) ===
          selectedStockStatus.value

      return (
        matchesKeyword &&
        matchesStore &&
        matchesMode &&
        matchesStatus
      )
    }
  )
})

const lowStockCount = computed(() => {
  return inventories.value.filter(
    (inventory) =>
      getStockStatus(inventory) === 'LOW'
  ).length
})

const enabledCount = computed(() => {
  return inventories.value.filter(
    (inventory) =>
      inventory.autoRestockEnabled
  ).length
})

const disabledCount = computed(() => {
  return inventories.value.length -
    enabledCount.value
})

const totalPages = computed(() => {
  return Math.max(
    1,
    Math.ceil(
      filteredInventories.value.length /
        pageSize
    )
  )
})

const paginatedInventories =
  computed(() => {
    const start =
      (currentPage.value - 1) *
      pageSize

    return filteredInventories.value.slice(
      start,
      start + pageSize
    )
  })

const pageGroupStart = computed(() => {
  return (
    Math.floor(
      (currentPage.value - 1) /
        pageGroupSize
    ) *
      pageGroupSize +
    1
  )
})

const pageGroupEnd = computed(() => {
  return Math.min(
    pageGroupStart.value +
      pageGroupSize -
      1,
    totalPages.value
  )
})

const visiblePages = computed(() => {
  const pages = []

  for (
    let page = pageGroupStart.value;
    page <= pageGroupEnd.value;
    page++
  ) {
    pages.push(page)
  }

  return pages
})

const changePage = (page) => {
  if (
    page < 1 ||
    page > totalPages.value
  ) {
    return
  }

  currentPage.value = page
}

const movePreviousPageGroup = () => {
  changePage(
    Math.max(
      1,
      pageGroupStart.value -
        pageGroupSize
    )
  )
}

const moveNextPageGroup = () => {
  changePage(
    Math.min(
      totalPages.value,
      pageGroupStart.value +
        pageGroupSize
    )
  )
}

const resetFilters = () => {
  keyword.value = ''
  selectedStoreId.value = ''
  selectedRestockMode.value = ''
  selectedStockStatus.value = ''
  currentPage.value = 1
}

const openSettingModal = (
  inventory
) => {
  selectedInventory.value =
    inventory

  settingForm.autoRestockEnabled =
    Boolean(
      inventory.autoRestockEnabled
    )

  settingForm.restockMode =
    inventory.restockMode ??
    'DAILY'

  settingForm.minStock =
    Number(inventory.minStock ?? 0)

  settingForm.targetStock =
    Number(inventory.targetStock ?? 1)

  settingModalOpen.value = true
}

const closeSettingModal = () => {
  if (saving.value) {
    return
  }

  settingModalOpen.value = false
  selectedInventory.value = null
}

const validateSettingForm = () => {
  if (
    settingForm.minStock === null ||
    settingForm.minStock < 0
  ) {
    showMessage(
      '최소 재고는 0 이상이어야 합니다.',
      'error'
    )

    return false
  }

  if (
    settingForm.targetStock === null ||
    settingForm.targetStock < 1
  ) {
    showMessage(
      '목표 재고는 1 이상이어야 합니다.',
      'error'
    )

    return false
  }

  if (
    settingForm.targetStock <=
    settingForm.minStock
  ) {
    showMessage(
      '목표 재고는 최소 재고보다 커야 합니다.',
      'error'
    )

    return false
  }

  return true
}
const toggleAutoRestock = async (
inventory
) => {
const nextEnabled =
  !inventory.autoRestockEnabled

const minStock =
  Number(inventory.minStock ?? 0)

const targetStock =
  Number(inventory.targetStock ?? 0)

/*
 * 자동 보충을 켜려고 하는데
 * 최소·목표 재고 설정이 정상적이지 않은 경우
 * 설정 모달을 먼저 엽니다.
 */
if (
  nextEnabled &&
  (
    targetStock < 1 ||
    targetStock <= minStock
  )
) {
  openSettingModal(inventory)

  settingForm.autoRestockEnabled =
    true

  settingForm.minStock =
    minStock >= 0
      ? minStock
      : 10

  settingForm.targetStock =
    targetStock > minStock
      ? targetStock
      : 50

  showMessage(
    '자동 보충을 사용하려면 최소 재고와 목표 재고를 설정해주세요.',
    'info'
  )

  return
}

togglingInventoryId.value =
  inventory.storeInventoryId

clearMessage()

const request = {
  autoRestockEnabled: nextEnabled,

  restockMode:
    inventory.restockMode ??
    'DAILY',

  minStock,

  targetStock
}

try {
  const response =
    await updateHeadInventoryRestockSetting(
      inventory.storeInventoryId,
      request
    )

  const updatedInventory =
    extractItem(response)

  const targetIndex =
    inventories.value.findIndex(
      (item) =>
        item.storeInventoryId ===
        inventory.storeInventoryId
    )

  if (
    targetIndex >= 0 &&
    updatedInventory
  ) {
    inventories.value.splice(
      targetIndex,
      1,
      updatedInventory
    )
  }

  showMessage(
    nextEnabled
      ? '자동 재고 보충을 사용하도록 변경했습니다.'
      : '자동 재고 보충을 중지했습니다.'
  )
} catch (error) {
  showMessage(
    extractErrorMessage(
      error,
      '자동 재고 보충 상태를 변경하지 못했습니다.'
    ),
    'error'
  )
} finally {
  togglingInventoryId.value = null
}
}
const saveRestockSetting = async () => {
  if (
    !selectedInventory.value ||
    !validateSettingForm()
  ) {
    return
  }

  saving.value = true
  clearMessage()

  const request = {
    autoRestockEnabled:
      settingForm.autoRestockEnabled,

    restockMode:
      settingForm.restockMode,

    minStock:
      Number(settingForm.minStock),

    targetStock:
      Number(settingForm.targetStock)
  }

  try {
    const response =
      await updateHeadInventoryRestockSetting(
        selectedInventory.value
          .storeInventoryId,
        request
      )

    const updatedInventory =
      extractItem(response)

    const targetIndex =
      inventories.value.findIndex(
        (inventory) =>
          inventory.storeInventoryId ===
          selectedInventory.value
            .storeInventoryId
      )

    if (
      targetIndex >= 0 &&
      updatedInventory
    ) {
      inventories.value.splice(
        targetIndex,
        1,
        updatedInventory
      )
    }

    settingModalOpen.value = false
    selectedInventory.value = null

    showMessage(
      '자동 재고 보충 설정이 저장되었습니다.'
    )
  } catch (error) {
    showMessage(
      extractErrorMessage(
        error,
        '자동 재고 보충 설정을 저장하지 못했습니다.'
      ),
      'error'
    )
  } finally {
    saving.value = false
  }
}

const formatNumber = (value) => {
  return Number(value ?? 0)
    .toLocaleString('ko-KR')
}

const formatDate = (value) => {
  if (!value) {
    return '-'
  }

  const date = new Date(value)

  if (Number.isNaN(date.getTime())) {
    return '-'
  }

  return new Intl.DateTimeFormat(
    'ko-KR',
    {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    }
  ).format(date)
}

watch(
  [
    keyword,
    selectedStoreId,
    selectedRestockMode,
    selectedStockStatus
  ],
  () => {
    currentPage.value = 1
  }
)

watch(totalPages, (pageCount) => {
  if (currentPage.value > pageCount) {
    currentPage.value = pageCount
  }
})

onMounted(async () => {
  /*
   * 최초 목록 조회
   */
  await loadInventories(false)

  /*
   * 5초 자동 갱신 시작
   */
  startAutoRefresh()

  /*
   * 브라우저 탭 복귀 감지
   */
  document.addEventListener(
    'visibilitychange',
    handleVisibilityChange
  )
})

onBeforeUnmount(() => {
  /*
   * 재고 화면을 벗어나면
   * 반복 요청을 반드시 종료합니다.
   */
  stopAutoRefresh()

  document.removeEventListener(
    'visibilitychange',
    handleVisibilityChange
  )
})
</script>

<style scoped>
.inventory-page {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.page-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 24px;
}

.page-eyebrow,
.modal-eyebrow {
  margin: 0 0 8px;
  color: #7b8497;
  font-size: 12px;
  font-weight: 800;
  letter-spacing: 0.12em;
}

.page-header h1 {
  margin: 0;
  color: #252a38;
  font-size: 30px;
}

.page-description {
  margin: 10px 0 0;
  color: #71798b;
  font-size: 14px;
}

.refresh-button,
.reset-button,
.setting-button,
.cancel-button,
.save-button {
  border: 0;
  border-radius: 10px;
  cursor: pointer;
  font-weight: 700;
}

.refresh-button {
  padding: 11px 17px;
  background: #30384b;
  color: white;
}

.refresh-button:disabled,
.save-button:disabled,
.cancel-button:disabled {
  cursor: not-allowed;
  opacity: 0.6;
}

.summary-grid {
  display: grid;
  grid-template-columns:
    repeat(4, minmax(0, 1fr));
  gap: 16px;
}

.summary-card {
  position: relative;
  padding: 22px;
  border: 1px solid #e4e7ee;
  border-radius: 16px;
  background: white;
  box-shadow:
    0 8px 24px rgba(30, 35, 48, 0.04);
}

.summary-label {
  display: block;
  margin-bottom: 14px;
  color: #747c8e;
  font-size: 13px;
  font-weight: 700;
}

.summary-value {
  color: #292e3b;
  font-size: 30px;
}

.summary-value.danger {
  color: #d64e4e;
}

.summary-value.success {
  color: #16855f;
}

.summary-value.muted {
  color: #7b8497;
}

.summary-unit {
  margin-left: 5px;
  color: #858c9b;
  font-size: 13px;
}

.filter-panel {
  display: grid;
  grid-template-columns:
    minmax(220px, 1.5fr)
    repeat(3, minmax(150px, 1fr))
    auto;
  align-items: end;
  gap: 14px;
  padding: 20px;
  border: 1px solid #e4e7ee;
  border-radius: 16px;
  background: white;
}

.filter-field,
.form-field {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.filter-field label,
.form-field label {
  color: #565e70;
  font-size: 13px;
  font-weight: 700;
}

.filter-field input,
.filter-field select,
.form-field input,
.form-field select {
  width: 100%;
  box-sizing: border-box;
  min-height: 42px;
  padding: 0 12px;
  border: 1px solid #d9dde7;
  border-radius: 9px;
  background: white;
  color: #303643;
  outline: none;
}

.filter-field input:focus,
.filter-field select:focus,
.form-field input:focus,
.form-field select:focus {
  border-color: #6c768d;
  box-shadow:
    0 0 0 3px rgba(70, 82, 111, 0.1);
}

.reset-button {
  min-height: 42px;
  padding: 0 16px;
  background: #eef0f5;
  color: #535b6e;
}

.table-panel {
  overflow: hidden;
  border: 1px solid #e4e7ee;
  border-radius: 16px;
  background: white;
}

.table-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 22px;
  border-bottom: 1px solid #ebedf2;
}

.table-header h2 {
  margin: 0;
  color: #303542;
  font-size: 18px;
}

.table-header p {
  margin: 6px 0 0;
  color: #858c9b;
  font-size: 13px;
}

.table-scroll {
  overflow-x: auto;
}

.inventory-table {
  width: 100%;
  min-width: 1250px;
  border-collapse: collapse;
}

.inventory-table th,
.inventory-table td {
  padding: 15px 13px;
  border-bottom: 1px solid #eef0f4;
  text-align: center;
  white-space: nowrap;
}

.inventory-table th {
  background: #f8f9fb;
  color: #626a7c;
  font-size: 12px;
  font-weight: 800;
}

.inventory-table td {
  color: #454c5d;
  font-size: 13px;
}

.inventory-table tbody tr:hover {
  background: #fafbfc;
}

.store-name {
  color: #303746;
}

.item-cell {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 4px;
}

.item-cell span {
  color: #9299a7;
  font-size: 11px;
}

.stock-danger {
  color: #d64e4e;
}

.status-badge {
display: inline-flex;
align-items: center;
justify-content: center;
min-width: 64px;
padding: 6px 9px;
border-radius: 999px;
font-size: 11px;
font-weight: 800;
}

.enabled-toggle-button {
display: inline-flex;
align-items: center;
justify-content: center;

min-width: 64px;
padding: 7px 11px;

border: 1px solid #dfe2e9;
border-radius: 999px;
cursor: pointer;

background: #f0f1f4;
color: #747b89;

font-family: inherit;
font-size: 11px;
font-weight: 800;

transition:
  transform 0.15s ease,
  background 0.15s ease,
  border-color 0.15s ease;
}

.enabled-toggle-button:hover {
  border-color: #b8bfcc;
  transform: translateY(-1px);
}

.enabled-toggle-button.active {
  border-color: #cdeade;
  background: #ecf8f3;
  color: #187655;
}

.enabled-toggle-button.active:hover {
  border-color: #80c8aa;
  background: #e0f4eb;
}

.enabled-toggle-button:disabled {
  cursor: not-allowed;
  opacity: 0.55;
  transform: none;
}

.status-low {
  background: #fff0f0;
  color: #cf4242;
}

.status-normal {
  background: #eef6ff;
  color: #356ca8;
}

.status-full {
  background: #ecf8f3;
  color: #187655;
}

.mode-label {
  color: #596174;
  font-weight: 700;
}

.setting-button {
  padding: 7px 12px;
  background: #30384b;
  color: white;
  font-size: 12px;
}

.empty-state {
  padding: 70px 20px;
  color: #858c9b;
  text-align: center;
}

.pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 20px;
}

.pagination button {
  min-width: 36px;
  height: 36px;
  padding: 0 10px;
  border: 1px solid #dfe2e9;
  border-radius: 8px;
  background: white;
  color: #596174;
  cursor: pointer;
}

.pagination button.active {
  border-color: #30384b;
  background: #30384b;
  color: white;
}

.pagination button:disabled {
  cursor: not-allowed;
  opacity: 0.4;
}

.modal-backdrop {
  position: fixed;
  z-index: 9998;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  background: rgba(21, 25, 35, 0.55);
}

.setting-modal {
  width: min(520px, 100%);
  overflow: hidden;
  border-radius: 18px;
  background: white;
  box-shadow:
    0 30px 80px rgba(16, 20, 29, 0.25);
}

.modal-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  padding: 24px;
  border-bottom: 1px solid #e9ebf0;
}

.modal-header h2 {
  margin: 0;
  color: #2d3341;
  font-size: 21px;
}

.modal-close {
  border: 0;
  background: transparent;
  color: #767e8d;
  cursor: pointer;
  font-size: 27px;
  line-height: 1;
}

.selected-summary {
  display: flex;
  flex-direction: column;
  gap: 5px;
  margin: 20px 24px 0;
  padding: 16px;
  border-radius: 12px;
  background: #f6f7fa;
}

.selected-summary strong {
  color: #303746;
}

.selected-summary span {
  color: #747c8d;
  font-size: 13px;
}

.setting-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
  padding: 22px 24px 24px;
}

.toggle-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
}

.toggle-row div {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.toggle-row strong {
  color: #333947;
  font-size: 14px;
}

.toggle-row span {
  color: #858c9b;
  font-size: 12px;
}

.toggle-row input {
  width: 20px;
  height: 20px;
}

.number-grid {
  display: grid;
  grid-template-columns:
    repeat(2, minmax(0, 1fr));
  gap: 14px;
}

.setting-help {
  margin: -8px 0 0;
  color: #858c9b;
  font-size: 12px;
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding-top: 4px;
}

.cancel-button,
.save-button {
  padding: 11px 18px;
}

.cancel-button {
  background: #eceef3;
  color: #555d6f;
}

.save-button {
  background: #30384b;
  color: white;
}

@media (max-width: 1100px) {
  .summary-grid {
    grid-template-columns:
      repeat(2, minmax(0, 1fr));
  }

  .filter-panel {
    grid-template-columns:
      repeat(2, minmax(0, 1fr));
  }

  .reset-button {
    width: 100%;
  }
}

@media (max-width: 700px) {
  .page-header {
    flex-direction: column;
  }

  .refresh-button {
    width: 100%;
  }

  .summary-grid,
  .filter-panel,
  .number-grid {
    grid-template-columns: 1fr;
  }

  .pagination {
    flex-wrap: wrap;
  }
}
</style>