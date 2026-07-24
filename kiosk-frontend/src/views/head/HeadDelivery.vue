<!--
  [화면 흐름 안내] HeadDelivery
  역할: 본사 관리에서 사용자가 보는 화면이다.
  진입: /head/deliveries -> 이 Vue 파일 렌더링
  데이터: 사용자 동작 -> @/api/axios -> 응답/상태 반영
  다음 이동: 현재 상태를 갱신하거나 부모 화면에 이벤트를 전달
-->
<script setup>
import { ref, onMounted } from 'vue'
import api from '@/api/axios'
import { restockRejectionReasons } from '@/constants/restockRejectionReasons'


const deliveries = ref([])
const loading = ref(false)
const cancelTarget = ref(null)
const cancelReasonOption = ref('')
const customCancelReason = ref('')

const cancelReasons = restockRejectionReasons



// 배송 목록 조회
const getDeliveries = async () => {

    try {
        loading.value = true

        const response =
            await api.get('/head/deliveries')

        deliveries.value =
            response.data

    } catch(error) {

        console.error(error)

    } finally {
        loading.value = false
    }

}



// 상태 변경
const changeStatus = async (
    deliveryId,
    status
) => {


    try {

        await api.put(
            `/head/delivery/${deliveryId}/status`,
            null,
            {
                params:{
                    status: status
                }
            }
        )


        await getDeliveries()


    } catch(error){

        console.error(error)

        alert(
            "배송 상태 변경 실패"
        )

    }

}



onMounted(() => {

    getDeliveries()

})

/*
 * 백엔드 LocalDateTime 값을 배송 목록에서 읽기 쉬운 한국 시간 문자열로 표시합니다.
 */
function formatDate(date){
    if(!date) return '-'
    return new Date(date).toLocaleString('ko-KR')
}

/* 쉬운주석: 선택한 배송 정보를 취소 모달에 넣고 이전 입력값을 비운다. */
const openCancelModal = (delivery) => {
    cancelTarget.value = delivery
    cancelReasonOption.value = ''
    customCancelReason.value = ''
}

/* 쉬운주석: 닫기 또는 처리 완료 시 모달과 입력값을 모두 초기화한다. */
const closeCancelModal = () => {
    cancelTarget.value = null
    cancelReasonOption.value = ''
    customCancelReason.value = ''
}

/*
 * 쉬운주석: '기타'는 직접 입력값을, 나머지는 선택한 기본 사유를 서버로 보낸다.
 * 서버는 배송 취소와 재고 신청 반려를 함께 처리하고 지점에 실시간 알림을 보낸다.
 */
const cancelDelivery = async () => {
    const reason = cancelReasonOption.value === '기타(직접 입력)'
        ? customCancelReason.value.trim()
        : cancelReasonOption.value

    if (!reason) {
        alert('배송 취소 사유를 선택하거나 입력해주세요.')
        return
    }

    try {
        const response = await api.put(
            `/head/delivery/${cancelTarget.value.deliveryId}/cancel`,
            { reason }
        )
        /*
         * 쉬운주석: 본사 공통 예외 응답은 HTTP 200일 수 있으므로
         * success=false이면 성공처럼 모달을 닫지 않고 오류로 처리한다.
         */
        if (response.data?.success === false) {
            throw new Error(response.data.message || '배송 취소에 실패했습니다.')
        }
        closeCancelModal()
        await getDeliveries()
    } catch (error) {
        console.error(error)
        alert(error.response?.data?.message || error.response?.data?.error || error.message || '배송 취소에 실패했습니다.')
    }
}
/*
 * DeliveryStatus enum을 화면용 한글 상태로 변환합니다.
 * READY → STARTED → IN_PROGRESS → COMPLETED 흐름을 표에 표시합니다.
 */
function deliveryStatusText(status){
    const map = {
        READY:'배송 준비',
        STARTED:'배송 시작',
        IN_PROGRESS:'배송 중',
        COMPLETED:'배송 완료',
        CANCELED:'배송 취소'
    }
    return map[status] ?? status
}

/*
 * 배송과 연결된 재고 신청 상태를 한글로 변환합니다.
 * 배송 상태와 신청 승인 상태를 서로 다른 열에서 구분하기 위한 함수입니다.
 */
function restockStatusText(status){
    const map = {
        WAITING:'승인 대기',
        APPROVED:'승인 완료',
        SHIPPING:'배송 중',
        COMPLETED:'입고 완료',
        REJECTED:'반려',
        CANCELED:'취소'
    }
    return map[status] ?? status
}

</script>



<template>

<section class="delivery-page">
  <header class="page-header">
    <div>
      <p class="page-eyebrow">DELIVERY MANAGEMENT</p>
      <h1>배송 관리</h1>
      <p class="page-description">지점별 재고 신청의 배송 진행 상태를 관리합니다.</p>
    </div>
    <button class="refresh-button" type="button" :disabled="loading" @click="getDeliveries">
      {{ loading ? '불러오는 중...' : '새로고침' }}
    </button>
  </header>

  <section class="table-panel">
    <div class="panel-header">
      <div>
        <h2>배송 목록</h2>
        <p>전체 {{ deliveries.filter(d => d).length }}건</p>
      </div>
    </div>
    <div class="table-scroll">
      <table class="delivery-table">

<thead>

<tr>
<th>지점명</th>
<th>재고품목</th>
<th>단위</th>
<th>신청수량</th>
<th>신청일시</th>
<th>신청상태</th>
<th>배송상태</th>
<th>관리</th>
</tr>

</thead>


<tbody>


    <tr
v-for="(delivery,index) in deliveries.filter(d => d)"
:key="delivery.deliveryId ?? index"
>


<td>
{{ delivery.storeName }}
</td>


<td>
{{ delivery.itemName }}
</td>


<td>
{{ delivery.unit }}
</td>


<td>
{{ delivery.requestQuantity }}
</td>


<td>
{{ formatDate(delivery.requestedAt) }}
</td>


<td>
<span class="status-badge restock-status">
  {{ restockStatusText(delivery.restockStatus) }}
</span>
</td>


<td>
<span class="status-badge" :class="`delivery-${delivery.deliveryStatus?.toLowerCase()}`">
  {{ deliveryStatusText(delivery.deliveryStatus) }}
</span>
</td>


<td>


<!-- READY -->
<button
class="status-button"
v-if="delivery.deliveryStatus === 'READY'"
@click="
changeStatus(
delivery.deliveryId,
'STARTED'
)
"
>
배송 시작
</button>



<!-- STARTED -->
<button
class="status-button"
v-if="delivery.deliveryStatus === 'STARTED'"
@click="
changeStatus(
delivery.deliveryId,
'IN_PROGRESS'
)
"
>
배송 중
</button>



<!-- IN_PROGRESS -->
<button
class="status-button complete"
v-if="delivery.deliveryStatus === 'IN_PROGRESS'"
@click="
changeStatus(
delivery.deliveryId,
'COMPLETED'
)
"
>
배송 완료
</button>



<span
class="completed-label"
v-if="
delivery.deliveryStatus === 'COMPLETED'
"
>
완료
</span>

<span
class="canceled-label"
v-if="delivery.deliveryStatus === 'CANCELED'"
:title="delivery.cancelReason"
>
취소
</span>

<button
v-if="!['COMPLETED', 'CANCELED'].includes(delivery.deliveryStatus)"
class="status-button cancel"
type="button"
@click="openCancelModal(delivery)"
>
배송 취소
</button>

</td>


</tr>

    <tr v-if="!loading && deliveries.filter(d => d).length === 0">
      <td class="empty-cell" colspan="8">등록된 배송 내역이 없습니다.</td>
    </tr>

</tbody>


</table>
    </div>
  </section>

  <!-- 쉬운주석: 배송 취소 사유를 선택하고 기타일 때만 직접 입력하는 모달이다. -->
  <div v-if="cancelTarget" class="cancel-modal-backdrop" @click.self="closeCancelModal">
    <form class="cancel-modal" @submit.prevent="cancelDelivery">
      <h2>배송 취소 사유</h2>
      <p>{{ cancelTarget.storeName }} · {{ cancelTarget.itemName }}</p>

      <label for="cancel-reason">취소 사유 선택</label>
      <select id="cancel-reason" v-model="cancelReasonOption" required>
        <option value="" disabled>사유를 선택해주세요</option>
        <option v-for="reason in cancelReasons" :key="reason" :value="reason">
          {{ reason }}
        </option>
      </select>

      <label v-if="cancelReasonOption === '기타(직접 입력)'" for="custom-cancel-reason">
        기타 사유
      </label>
      <textarea
        v-if="cancelReasonOption === '기타(직접 입력)'"
        id="custom-cancel-reason"
        v-model="customCancelReason"
        maxlength="500"
        rows="4"
        placeholder="배송 취소 사유를 직접 입력해주세요."
        required
      />

      <div class="cancel-modal-actions">
        <button type="button" class="modal-close-button" @click="closeCancelModal">닫기</button>
        <button type="submit" class="modal-cancel-button">배송 취소 확정</button>
      </div>
    </form>
  </div>
</section>

</template>

<style scoped>

.delivery-page {
  min-height: 100%;
  padding: 28px;
  background: #f6f7fb;
}

.page-header {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 20px;
  margin-bottom: 22px;
}

.page-eyebrow {
  margin: 0 0 7px;
  color: #8071e8;
  font-size: 11px;
  font-weight: 800;
  letter-spacing: .12em;
}

.page-header h1 {
  margin: 0;
  color: #252a38;
  font-size: 30px;
}

.page-description {
  margin: 9px 0 0;
  color: #71798b;
  font-size: 14px;
}

.refresh-button,
.status-button {
  border: 0;
  border-radius: 8px;
  color: #fff;
  background: #6d5de2;
  font-weight: 700;
  cursor: pointer;
  transition: transform .15s ease, opacity .15s ease;
}

.refresh-button {
  padding: 10px 16px;
}

.refresh-button:disabled {
  opacity: .55;
  cursor: default;
}

.refresh-button:not(:disabled):hover,
.status-button:hover {
  transform: translateY(-1px);
}

.table-panel {
  overflow: hidden;
  border: 1px solid #e2e6ed;
  border-radius: 15px;
  background: #fff;
  box-shadow: 0 8px 24px rgba(37, 42, 56, .04);
}

.panel-header {
  padding: 17px 20px;
  border-bottom: 1px solid #e8ebf0;
}

.panel-header h2 {
  margin: 0;
  color: #303646;
  font-size: 17px;
}

.panel-header p {
  margin: 5px 0 0;
  color: #8a91a1;
  font-size: 12px;
}

.table-scroll {
  overflow-x: auto;
}

.delivery-table {
  width: 100%;
  min-width: 980px;
  border-collapse: collapse;
}

.delivery-table th {
  padding: 13px 14px;
  border-bottom: 1px solid #e8ebf0;
  color: #697083;
  background: #f8f9fc;
  font-size: 12px;
  font-weight: 800;
  text-align: center;
  white-space: nowrap;
}

.delivery-table td {
  padding: 14px;
  border-bottom: 1px solid #edf0f4;
  color: #3f4656;
  font-size: 13px;
  text-align: center;
  white-space: nowrap;
}

.delivery-table tbody tr:last-child td {
  border-bottom: 0;
}

.delivery-table tbody tr:hover {
  background: #fbfbfe;
}

.status-badge,
.completed-label,
.canceled-label {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 66px;
  padding: 6px 9px;
  border-radius: 999px;
  font-size: 11px;
  font-weight: 800;
}

.restock-status {
  color: #6d5de2;
  background: #eeebff;
}

.delivery-ready {
  color: #b66c08;
  background: #fff2d8;
}

.delivery-started {
  color: #3978d4;
  background: #e3f0ff;
}

.delivery-in_progress {
  color: #7652ca;
  background: #eee7ff;
}

.delivery-completed,
.completed-label {
  color: #159b68;
  background: #dff9ed;
}

.delivery-canceled,
.canceled-label {
  color: #dc3545;
  background: #ffe7ea;
}

.status-button {
  min-width: 76px;
  padding: 7px 11px;
  font-size: 12px;
}

.status-button.complete {
  background: #159b68;
}

.status-button.cancel {
  min-width: 64px;
  margin-left: 10px;
  padding-inline: 8px;
  background: #dc3545;
}

.cancel-modal-backdrop {
  position: fixed;
  z-index: 1000;
  inset: 0;
  display: grid;
  place-items: center;
  padding: 20px;
  background: rgba(16, 20, 29, .55);
}

.cancel-modal {
  width: min(480px, 100%);
  padding: 24px;
  border-radius: 14px;
  background: #fff;
  box-shadow: 0 20px 60px rgba(0, 0, 0, .2);
}

.cancel-modal h2 {
  margin: 0 0 6px;
}

.cancel-modal p {
  margin: 0 0 20px;
  color: #71798b;
}

.cancel-modal label {
  display: block;
  margin: 14px 0 7px;
  font-weight: 700;
}

.cancel-modal select,
.cancel-modal textarea {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #d8dce5;
  border-radius: 8px;
  background: #fff;
}

.cancel-modal textarea {
  resize: vertical;
}

.cancel-modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin-top: 22px;
}

.cancel-modal-actions button {
  padding: 9px 14px;
  border: 0;
  border-radius: 8px;
  font-weight: 700;
  cursor: pointer;
}

.modal-close-button {
  color: #454b59;
  background: #eceef3;
}

.modal-cancel-button {
  color: #fff;
  background: #dc3545;
}

.empty-cell {
  height: 160px;
  color: #939aaa !important;
}

@media (max-width: 720px) {
  .delivery-page {
    padding: 18px;
  }

  .page-header {
    align-items: flex-start;
    flex-direction: column;
  }
}

</style>
