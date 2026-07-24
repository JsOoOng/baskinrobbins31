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


const deliveries = ref([])
const loading = ref(false)



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

/*
 * DeliveryStatus enum을 화면용 한글 상태로 변환합니다.
 * READY → STARTED → IN_PROGRESS → COMPLETED 흐름을 표에 표시합니다.
 */
function deliveryStatusText(status){
    const map = {
        READY:'배송 준비',
        STARTED:'배송 시작',
        IN_PROGRESS:'배송 중',
        COMPLETED:'배송 완료'
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


</td>


</tr>

    <tr v-if="!loading && deliveries.filter(d => d).length === 0">
      <td class="empty-cell" colspan="8">등록된 배송 내역이 없습니다.</td>
    </tr>

</tbody>


</table>
    </div>
  </section>
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
.completed-label {
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

.status-button {
  min-width: 76px;
  padding: 7px 11px;
  font-size: 12px;
}

.status-button.complete {
  background: #159b68;
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
