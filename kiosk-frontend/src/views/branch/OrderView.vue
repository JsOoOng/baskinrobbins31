<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import api from '@/api/axios'

const router = useRouter()


const goBack = () => {

    router.push('/branch/main')

}


// 주문 목록
const orders = ref([])

// 선택한 주문
const selectedOrder = ref(null)

// 선택한 주문 상태
const selectedStatus = ref('')

// 로그인한 사용자 정보
const user = JSON.parse(localStorage.getItem('user'))

// 주문 목록 조회
const loadOrders = async () => {
  try {
    const response = await api.get(`/branch/order/${user.storeId}`)

    orders.value = response.data

    // 선택한 주문이 있으면 상세도 갱신
    if (selectedOrder.value) {
      await selectOrder(selectedOrder.value.orderId)
    }

  } catch (e) {
    console.error('주문 목록 조회 실패', e)
  }
}

// 주문 상세 조회
const selectOrder = async (orderId) => {
  try {
    const response = await api.get(`/branch/order/detail/${orderId}`)

    selectedOrder.value = response.data
    selectedStatus.value = response.data.orderStatus

  } catch (e) {
    console.error('주문 상세 조회 실패', e)
  }
}

// 주문 상태 변경
const changeStatus = async () => {
  if (!selectedOrder.value) return

  try {

    await api.patch(
      `/branch/order/${selectedOrder.value.orderId}/status`,
      {
        status: selectedStatus.value
      }
    )

    alert('주문 상태가 변경되었습니다.')

    // 목록 갱신
    await loadOrders()

    // 상세 갱신
    await selectOrder(selectedOrder.value.orderId)

  } catch (e) {

    console.error('상태 변경 실패', e)
    alert('상태 변경 실패')

  }
}

// 자동 새로고침용 타이머
let intervalId

onMounted(() => {

  // 최초 조회
  loadOrders()

  // 5초마다 자동 새로고침
  intervalId = setInterval(() => {
    loadOrders()
  }, 5000)

})

onUnmounted(() => {
  clearInterval(intervalId)
})


const getStatusClass = (status) => {

    switch(status){

        case 'WAITING':
            return 'status-waiting'


        case 'PREPARING':
            return 'status-preparing'


        case 'COMPLETED':
            return 'status-completed'


        case 'CANCELED':
            return 'status-canceled'


        default:
            return ''

    }

}



const getStatusText = (status) => {

    switch(status){

        case 'WAITING':
            return '대기중'


        case 'PREPARING':
            return '준비중'


        case 'COMPLETED':
            return '완료'


        case 'CANCELED':
            return '취소'


        default:
            return status

    }

}

</script>

<template>

<div class="container">

    <div class="left">

        <button @click="goBack">
            ← 메인으로 돌아가기
        </button>

        <h2>주문 목록</h2>

        <table>

            <thead>

            <tr>
                <th>주문번호</th>
                <th>상태</th>
            </tr>

            </thead>

            <tbody>

            <tr
                v-for="order in orders"
                :key="order.orderId"
                @click="selectOrder(order.orderId)"
            >
                <td>{{ order.orderNumber }}</td>
                <td>
                    <span
                        :class="getStatusClass(order.orderStatus)"
                    >
                        {{ getStatusText(order.orderStatus) }}
                    </span>
                </td>
            </tr>

            </tbody>

        </table>

    </div>

    <div class="right">

        <h2>주문 상세</h2>

        <div v-if="selectedOrder">

            <p>주문번호 : {{ selectedOrder.orderNumber }}</p>

            <p>주문유형 : {{ selectedOrder.orderType }}</p>

            <p>주문상태 : {{ selectedOrder.orderStatus }}</p>

            <p>총금액 : {{ selectedOrder.totalPrice.toLocaleString() }}원</p>

            <hr>

            <h3>주문 상태 변경</h3>

            <select v-model="selectedStatus">
            <option value="WAITING">WAITING</option>
            <option value="PREPARING">PREPARING</option>
            <option value="COMPLETED">COMPLETED</option>
            <option value="CANCELED">CANCELED</option>
            </select>

            <button @click="changeStatus">
            상태 변경
            </button>

            <hr>

            <hr>

            <div
                v-for="item in selectedOrder.items"
                :key="item.productName"
            >

                <h3>{{ item.productName }}</h3>

                <p>수량 : {{ item.quantity }}</p>

                <p>가격 : {{ item.unitPrice.toLocaleString() }}원</p>

                <b>맛</b>

                <ul>

                    <li
                        v-for="flavor in item.flavors"
                        :key="flavor.flavorName"
                    >

                        {{ flavor.flavorName }}
                        ({{ flavor.quantity }})

                    </li>

                </ul>

                <hr>

            </div>

        </div>

        <div v-else>

            주문을 선택하세요.

        </div>

    </div>

</div>

</template>
<style scoped>

.container{

    display:flex;

    gap:30px;

    padding:30px;

    background:#f8f9fa;

    min-height:100vh;

}


/* 왼쪽 주문 목록 */

.left{

    width:40%;

    background:white;

    padding:20px;

    border-radius:15px;

    box-shadow:0 4px 12px rgba(0,0,0,0.08);

}



/* 오른쪽 상세 */

.right{

    width:60%;

    background:white;

    padding:25px;

    border-radius:15px;

    box-shadow:0 4px 12px rgba(0,0,0,0.08);

}




h2{

    margin-bottom:20px;

    color:#333;

}




/* 테이블 */

table{

    width:100%;

    border-collapse:separate;

    border-spacing:0;

    overflow:hidden;

    border-radius:12px;

}



thead{

    background:#333;

    color:white;

}



th{

    padding:14px;

    font-size:14px;

}



td{

    padding:14px;

    border-bottom:1px solid #eee;

    text-align:center;

    color:#444;

}




tbody tr{

    cursor:pointer;

    transition:0.2s;

}



tbody tr:hover{

    background:#f7f7f7;

}



tbody tr:last-child td{

    border-bottom:none;

}




/* 상태 배지 공통 */

.status-waiting,
.status-preparing,
.status-completed,
.status-canceled{


    display:inline-block;

    min-width:80px;

    padding:6px 14px;

    border-radius:20px;

    font-size:13px;

    font-weight:bold;

    text-align:center;

}





/* 대기 */

.status-waiting{


    background:#fff3cd;

    color:#856404;

}



/* 준비중 */

.status-preparing{


    background:#cfe2ff;

    color:#084298;

}



/* 완료 */

.status-completed{


    background:#d1e7dd;

    color:#0f5132;

}



/* 취소 */

.status-canceled{


    background:#f8d7da;

    color:#842029;

}




/* 상세 정보 */

.right p{

    font-size:16px;

    margin:12px 0;

}




hr{

    margin:20px 0;

    border:none;

    border-top:1px solid #eee;

}




select{

    padding:8px 12px;

    border-radius:8px;

    border:1px solid #ccc;

    margin-right:10px;

}



button{

    padding:9px 16px;

    border:none;

    border-radius:8px;

    background:#222;

    color:white;

    cursor:pointer;

    transition:0.2s;

}



button:hover{

    background:#555;

}



</style>