<!--
  [화면 흐름 안내] HeadRestockRequest
  역할: 본사 관리에서 사용자가 보는 화면이다.
  진입: /head/inventory-requests -> 이 Vue 파일 렌더링
  데이터: 사용자 동작 -> @/api/head/headRestockApi -> 응답/상태 반영
  다음 이동: 현재 상태를 갱신하거나 부모 화면에 이벤트를 전달
-->
<template>
  <AppMessageToast
    :message="message"
    :type="messageType"
    @close="clearMessage"
  />

  <section class="restock-page">
    <header class="page-header">
      <div>
        <p class="page-eyebrow">
          RESTOCK REQUEST MANAGEMENT
        </p>
        <h1>재고 신청 관리</h1>
        <p class="page-description">
          지점의 재고 신청 내역을 확인하고 승인 및 배송 상태를 처리합니다.
        </p>
      </div>
      <button
        type="button"
        class="refresh-button"
        :disabled="loading"
        @click="loadRestocks"
      >
        {{ loading ? '조회 중...' : '새로고침' }}
      </button>
    </header>

    <div class="summary-grid">
      <article class="summary-card" @click="filterStatus = ''" :class="{ active: filterStatus === '' }">
        <span class="summary-label">전체 신청 내역</span>
        <strong class="summary-value">{{ formatNumber(restocks.length) }}</strong>
        <span class="summary-unit">건</span>
      </article>

      <article class="summary-card" @click="filterStatus = 'WAITING'" :class="{ active: filterStatus === 'WAITING' }">
        <span class="summary-label">승인 대기</span>
        <strong class="summary-value warning">{{ formatNumber(waitingCount) }}</strong>
        <span class="summary-unit">건</span>
      </article>

      <article class="summary-card" @click="filterStatus = 'APPROVED'" :class="{ active: filterStatus === 'APPROVED' }">
        <span class="summary-label">승인 완료 (배송 준비)</span>
        <strong class="summary-value info">{{ formatNumber(approvedCount) }}</strong>
        <span class="summary-unit">건</span>
      </article>

      <article class="summary-card" @click="filterStatus = 'SHIPPING'" :class="{ active: filterStatus === 'SHIPPING' }">
        <span class="summary-label">배송 중</span>
        <strong class="summary-value primary">{{ formatNumber(shippingCount) }}</strong>
        <span class="summary-unit">건</span>
      </article>
    </div>

    <!-- 재고 신청 목록 -->
    <section class="table-panel">
      <div class="table-header">
        <div>
          <h2>신청 목록</h2>
          <p>검색 결과 {{ formatNumber(filteredRestocks.length) }}건</p>
        </div>
      </div>

      <div v-if="loading" class="empty-state">
        신청 내역을 불러오는 중입니다.
      </div>
      <div v-else-if="filteredRestocks.length === 0" class="empty-state">
        조건에 맞는 신청 내역이 없습니다.
      </div>
      <div v-else class="table-scroll">
        <table class="inventory-table">
          <thead>
            <tr>
              <th>지점명</th>
              <th>재고 품목</th>
              <th>단위</th>
              <th>신청 수량</th>
              <th>상태</th>
              <th>신청일시</th>
              <th>관리</th>
              <th>처리자</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in paginatedRestocks" :key="item.requestId">
              <td><strong>{{ item.storeName || '-' }}</strong></td>
              <td>
                <div class="item-cell">
                  <strong>{{ item.itemName }}</strong>
                </div>
              </td>
              <td>{{ item.unit === '개' ? 'EA' : (item.unit || 'EA') }}</td>
              <td><strong>{{ formatNumber(item.requestQuantity) }}</strong></td>
              <td>
                <span class="status-badge" :class="getStatusClass(item.status)">
                  {{ getStatusLabel(item.status) }}
                </span>
              </td>
              <td>{{ formatDate(item.requestedAt) }}</td>
              <td>
                <div class="action-buttons">
                  <button
                    v-if="item.status === 'WAITING'"
                    type="button"
                    class="save-button"
                    :disabled="processingId === item.requestId"
                    @click="handleApprove(item)"
                  >
                    승인
                  </button>
                  <button
                    v-if="item.status === 'WAITING'"
                    type="button"
                    class="save-button"
                    style="background: #ef4444;"
                    :disabled="processingId === item.requestId"
                    @click="handleReject(item)"
                  >
                    반려
                  </button>
                  <button
                    v-if="item.status === 'APPROVED'"
                    type="button"
                    class="save-button"
                    style="background: #3b82f6"
                    :disabled="processingId === item.requestId"
                    @click="handleShipping(item)"
                  >
                    배송시작
                  </button>
                  <button
                    v-if="item.status === 'SHIPPING'"
                    type="button"
                    class="save-button"
                    style="background: #10b981"
                    :disabled="processingId === item.requestId"
                    @click="handleComplete(item)"
                  >
                    배송완료
                  </button>
                  <button
                    v-if="findShortageAlert(item)"
                    type="button"
                    class="notify-button"
                    :disabled="sendingAlertId === findShortageAlert(item).alertId"
                    @click="handleSendAlert(item)"
                  >
                    {{ sendingAlertId === findShortageAlert(item).alertId ? '알림 전송 중' : '지점 알림' }}
                  </button>
                </div>
              </td>
              <td>{{ item.adminName || '-' }}</td>
            </tr>
          </tbody>
        </table>
      </div>
      <HeadTablePagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total-items="filteredRestocks.length"
      />
    </section>
  </section>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import AppMessageToast from '@/components/common/AppMessageToast.vue'
import HeadTablePagination from '@/components/head/HeadTablePagination.vue'
import { useHeadAuthStore } from '@/stores/head/headAuthStore'
import { askRestockRejectionReason } from '@/constants/restockRejectionReasons'
import {
  getHeadRestocks,
  approveHeadRestock,
  startHeadRestockShipping,
  completeHeadRestock,
  rejectHeadRestock,
  extractRestockErrorMessage
} from '@/api/head/headRestockApi'
import {
  getActiveInventoryShortageAlerts,
  sendInventoryShortageAlertToStore
} from '@/api/headquarter/headInventoryAlertApi'

const headAuthStore = useHeadAuthStore()

const restocks = ref([])

const loading = ref(false)
const processingId = ref(null)
const sendingAlertId = ref(null)
const shortageAlerts = ref([])
const filterStatus = ref('')
const currentPage = ref(1)
const pageSize = ref(10)

const message = ref('')
const messageType = ref('success')

/* 신청 처리 결과를 성공·오류 유형과 함께 토스트에 표시합니다. */
const showMessage = (text, type = 'success') => {
  message.value = text
  messageType.value = type
}

/* 토스트 닫기 이벤트에서 현재 안내 문구를 초기화합니다. */
const clearMessage = () => {
  message.value = ''
}

/* 신청 수량과 전체 건수를 천 단위 구분 형식으로 표시합니다. */
const formatNumber = (num) => {
  return Number(num || 0).toLocaleString()
}

/* 신청 일시를 한국 지역 날짜·시간 문자열로 변환합니다. */
const formatDate = (dateString) => {
  if (!dateString) return '-'
  const date = new Date(dateString)
  const yyyy = date.getFullYear()
  const mm = String(date.getMonth() + 1).padStart(2, '0')
  const dd = String(date.getDate()).padStart(2, '0')
  const hh = String(date.getHours()).padStart(2, '0')
  const min = String(date.getMinutes()).padStart(2, '0')
  return `${yyyy}-${mm}-${dd} ${hh}:${min}`
}

/* RestockStatus enum을 표와 버튼에서 사용할 한글 상태명으로 바꿉니다. */
const getStatusLabel = (status) => {
  const labels = {
    WAITING: '승인 대기',
    APPROVED: '승인 완료',
    SHIPPING: '배송 중',
    COMPLETED: '배송 완료',
    REJECTED: '반려'
  }
  return labels[status] || status
}

/* 신청 상태별 배지 색상을 적용할 CSS 클래스명을 만듭니다. */
const getStatusClass = (status) => {
  return `status-${status.toLowerCase()}`
}

/*
 * 본사 재고 신청 목록과 활성 부족 알림을 함께 조회합니다.
 * 신청 데이터는 최신 요청이 먼저 보이도록 신청 시각 내림차순으로 정렬합니다.
 */
const loadRestocks = async () => {
  loading.value = true
  clearMessage()
  try {
    const [data, alertResponse] = await Promise.all([
      getHeadRestocks(),
      getActiveInventoryShortageAlerts()
    ])
    // Sort by requestedAt descending
    restocks.value = data.sort((a, b) => new Date(b.requestedAt) - new Date(a.requestedAt))
    shortageAlerts.value = Array.isArray(alertResponse.data) ? alertResponse.data : []
  } catch (error) {
    showMessage(extractRestockErrorMessage(error, '신청 내역을 불러오지 못했습니다.'), 'error')
  } finally {
    loading.value = false
  }
}

/*
 * WAITING 신청 승인 처리
 * 관리자 ID와 함께 승인 API 호출 → 상태 변경 알림 발행 → 목록 재조회 순서입니다.
 */
const handleApprove = async (item) => {
  if (!confirm(`${item.itemName} 신청을 승인하시겠습니까?`)) return
  
  const adminId = headAuthStore.headUser?.employeeId
  if (!adminId) {
    showMessage('처리 권한이 없습니다. 다시 로그인해주세요.', 'error')
    return
  }
  
  processingId.value = item.requestId
  try {
    await approveHeadRestock(item.requestId, { adminId })
    showMessage('승인 처리되었습니다.')
    await loadRestocks()
  } catch (error) {
    showMessage(extractRestockErrorMessage(error, '승인 처리에 실패했습니다.'), 'error')
  } finally {
    processingId.value = null
  }
}

/*
 * WAITING 신청 반려 처리
 * 반려 사유를 입력받아 API로 전달하고 지점 상태 알림과 목록을 갱신합니다.
 */
const handleReject = async (item) => {
  const rejectionReason = askRestockRejectionReason()
  if (!rejectionReason) return
  
  const adminId = headAuthStore.headUser?.employeeId
  if (!adminId) {
    showMessage('처리 권한이 없습니다. 다시 로그인해주세요.', 'error')
    return
  }
  
  processingId.value = item.requestId
  try {
    await rejectHeadRestock(item.requestId, { adminId, rejectionReason })
    showMessage('반려 처리되었습니다.')
    await loadRestocks()
  } catch (error) {
    showMessage(extractRestockErrorMessage(error, '반려 처리에 실패했습니다.'), 'error')
  } finally {
    processingId.value = null
  }
}

/*
 * 승인된 신청을 배송 단계로 이동합니다.
 * APPROVED → SHIPPING 상태 변경 API 호출 후 표의 최신 상태를 다시 조회합니다.
 */
const handleShipping = async (item) => {
  if (!confirm('배송을 시작하시겠습니까?')) return
  
  const adminId = headAuthStore.headUser?.employeeId
  if (!adminId) {
    showMessage('처리 권한이 없습니다. 다시 로그인해주세요.', 'error')
    return
  }

  processingId.value = item.requestId
  try {
    await startHeadRestockShipping(item.requestId, { adminId })
    showMessage('배송 처리가 시작되었습니다.')
    await loadRestocks()
  } catch (error) {
    showMessage(extractRestockErrorMessage(error, '배송 처리에 실패했습니다.'), 'error')
  } finally {
    processingId.value = null
  }
}

/*
 * 쉬운주석: 배송 중인 신청을 완료하면 신청 상태와 연결된 실제 재고가 함께 증가한다.
 * 화면의 관리자 번호를 완료 API에 보내고, 성공한 뒤 최신 목록을 다시 불러온다.
 */
const handleComplete = async (item) => {
  if (!confirm('배송을 완료하고 재고를 입고하시겠습니까?')) return

  const adminId = headAuthStore.headUser?.employeeId
  if (!adminId) {
    showMessage('처리 권한이 없습니다. 다시 로그인해주세요.', 'error')
    return
  }

  processingId.value = item.requestId
  try {
    await completeHeadRestock(item.requestId, { adminId })
    showMessage('배송 완료와 재고 입고가 처리되었습니다.')
    await loadRestocks()
  } catch (error) {
    showMessage(extractRestockErrorMessage(error, '배송 완료 처리에 실패했습니다.'), 'error')
  } finally {
    processingId.value = null
  }
}



const filteredRestocks = computed(() => {
  if (!filterStatus.value) return restocks.value
  return restocks.value.filter((r) => r.status === filterStatus.value)
})
/*
 * 상태 필터가 적용된 재고 신청을 공통 페이징 컴포넌트와 연결합니다.
 * 페이지당 건수가 바뀌거나 결과가 줄면 watch에서 유효한 마지막 페이지로 보정합니다.
 */
const totalPages = computed(() => Math.max(1, Math.ceil(filteredRestocks.value.length / pageSize.value)))
const paginatedRestocks = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  return filteredRestocks.value.slice(start, start + pageSize.value)
})
watch([filterStatus, pageSize], () => { currentPage.value = 1 })
watch(totalPages, (pages) => {
  if (currentPage.value > pages) currentPage.value = pages
})

/*
 * 재고 신청 행과 활성 재고 부족 알림을 지점·품목 기준으로 연결합니다.
 * 반환된 alertId는 지점 알림 전송 API의 식별자로 사용됩니다.
 */
const findShortageAlert = (item) => shortageAlerts.value.find((alert) =>
  alert.storeName === item.storeName && alert.itemName === item.itemName
)

/*
 * 재고 신청 관리의 '알림 보내기' 처리
 * 신청 행 → 활성 부족 알림 조회 → 지점 알림 API → 목록 재조회 순으로 이동합니다.
 */
const handleSendAlert = async (item) => {
  const shortageAlert = findShortageAlert(item)
  const adminId = headAuthStore.headUser?.employeeId
  if (!shortageAlert || !adminId) {
    showMessage('전송 가능한 부족 알림 또는 관리자 정보가 없습니다.', 'error')
    return
  }
  if (!confirm(`${item.storeName}에 ${item.itemName} 재고 부족 알림을 보내시겠습니까?`)) return
  sendingAlertId.value = shortageAlert.alertId
  try {
    await sendInventoryShortageAlertToStore(shortageAlert.alertId, adminId)
    showMessage(`${item.storeName}에 재고 부족 알림을 전송했습니다.`)
    await loadRestocks()
  } catch (error) {
    showMessage(error.response?.data?.message || '지점 알림 전송에 실패했습니다.', 'error')
  } finally {
    sendingAlertId.value = null
  }
}

const waitingCount = computed(() => restocks.value.filter(r => r.status === 'WAITING').length)
const approvedCount = computed(() => restocks.value.filter(r => r.status === 'APPROVED').length)
const shippingCount = computed(() => restocks.value.filter(r => r.status === 'SHIPPING').length)

onMounted(() => {
  loadRestocks()
})
</script>

<style scoped>
.restock-page {
  padding: 30px;
}

.notify-button {
  border: 0;
  border-radius: 6px;
  padding: 7px 10px;
  background: #f59e0b;
  color: #fff;
  cursor: pointer;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 25px;
}

.page-eyebrow {
  margin: 0 0 5px;
  color: #6d5de2;
  font-size: 11px;
  font-weight: 800;
  letter-spacing: 1px;
}

.page-header h1 {
  margin: 0 0 8px;
  color: #1a1e27;
  font-size: 24px;
  font-weight: 800;
  letter-spacing: -0.5px;
}

.page-description {
  margin: 0;
  color: #7b8396;
  font-size: 13px;
  line-height: 1.5;
}

.refresh-button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  height: 38px;
  padding: 0 16px;
  border: 1px solid #dce0e8;
  border-radius: 9px;
  cursor: pointer;
  color: #555d70;
  font-size: 13px;
  font-weight: 700;
  background: #ffffff;
  transition: all 0.2s;
}

.refresh-button:hover:not(:disabled) {
  border-color: #cbd0db;
  background: #f7f8fb;
}

.summary-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 15px;
  margin-bottom: 30px;
}

.summary-card {
  display: flex;
  flex-direction: column;
  padding: 22px 20px;
  border: 1px solid #ebedf3;
  border-radius: 14px;
  background: #ffffff;
  cursor: pointer;
  transition: all 0.2s ease;
}

.summary-card:hover, .summary-card.active {
  border-color: #6d5de2;
  box-shadow: 0 4px 15px rgba(109, 93, 226, 0.1);
  transform: translateY(-2px);
}

.summary-label {
  margin-bottom: 12px;
  color: #676d7d;
  font-size: 12px;
  font-weight: 700;
}

.summary-value {
  color: #1a1e27;
  font-size: 28px;
  font-weight: 900;
  line-height: 1;
}

.summary-value.warning {
  color: #f59e0b;
}

.summary-value.info {
  color: #3b82f6;
}

.summary-value.primary {
  color: #10b981;
}

.summary-unit {
  margin-top: 4px;
  color: #9299ab;
  font-size: 12px;
  font-weight: 600;
}

.table-panel {
  display: flex;
  flex-direction: column;
  border: 1px solid #ebedf3;
  border-radius: 16px;
  background: #ffffff;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.02);
}

.table-header {
  padding: 22px 24px;
  border-bottom: 1px solid #ebedf3;
}

.table-header h2 {
  margin: 0 0 4px;
  color: #2b313f;
  font-size: 16px;
  font-weight: 800;
}

.table-header p {
  margin: 0;
  color: #8b92a5;
  font-size: 12px;
}

.empty-state {
  padding: 80px 20px;
  color: #8b92a5;
  font-size: 14px;
  font-weight: 600;
  text-align: center;
}

.table-scroll {
  overflow-x: auto;
}

.inventory-table {
  width: 100%;
  border-collapse: separate;
  border-spacing: 0;
}

.inventory-table th {
  padding: 14px 24px;
  border-bottom: 1px solid #ebedf3;
  color: #676d7d;
  font-size: 11px;
  font-weight: 700;
  text-align: center;
  white-space: nowrap;
  background: #fbfbfc;
}

.inventory-table td {
  padding: 16px 24px;
  border-bottom: 1px solid #f2f4f8;
  color: #4b5263;
  font-size: 13px;
  vertical-align: middle;
  text-align: center;
  white-space: nowrap;
}

.item-cell {
  display: flex;
  flex-direction: column;
  gap: 4px;
  align-items: center;
}

.item-cell strong {
  color: #262b38;
  font-size: 14px;
}

.status-badge {
  display: inline-flex;
  padding: 4px 8px;
  border-radius: 6px;
  font-size: 11px;
  font-weight: 700;
}

.status-waiting {
  color: #b45309;
  background: #fef3c7;
}

.status-approved {
  color: #1e3a8a;
  background: #dbeafe;
}

.status-shipping {
  color: #065f46;
  background: #d1fae5;
}

.status-completed {
  color: #374151;
  background: #f3f4f6;
}

.status-rejected {
  color: #991b1b;
  background: #fee2e2;
}

.action-buttons {
  display: flex;
  gap: 8px;
  align-items: center;
  justify-content: center;
  flex-wrap: nowrap;
}

.save-button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  height: 32px;
  padding: 0 14px;
  border: 0;
  border-radius: 8px;
  cursor: pointer;
  color: #ffffff;
  font-size: 12px;
  font-weight: 700;
  background: #f59e0b;
  transition: all 0.2s;
  white-space: nowrap;
  flex-shrink: 0;
}

.save-button:hover:not(:disabled) {
  opacity: 0.9;
  transform: translateY(-1px);
}

.save-button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
</style>
