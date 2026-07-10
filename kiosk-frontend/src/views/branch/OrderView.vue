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
                <td>{{ order.orderStatus }}</td>
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
    padding:20px;
}

.left{
    width:40%;
}

.right{
    width:60%;
}

table{
    width:100%;
    border-collapse:collapse;
}

th,td{
    border:1px solid #ddd;
    padding:10px;
    text-align:center;
}

tbody tr{
    cursor:pointer;
}

tbody tr:hover{
    background:#f5f5f5;
}
</style>