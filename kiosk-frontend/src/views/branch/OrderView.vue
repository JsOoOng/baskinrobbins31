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
const user = JSON.parse(localStorage.getItem('branchUser'))

// 주문 목록 조회
const loadOrders = async () => {
    try {
        const response = await api.get(`/branch/order/${user.storeId}`)

        orders.value = response.data.filter(
            order => order.paymentStatus != null
        )

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

        // 목록 새로고침
        await loadOrders()

        // 상세 새로고침
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

// 주문 상태 색상
const getStatusClass = (status) => {

    switch (status) {

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

// 주문 상태 한글
const getStatusText = (status) => {

    switch (status) {

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

// 결제 상태 한글
const getPaymentStatusText = (status) => {

    switch (status) {

        case 'PAID':
            return '결제완료'

        case 'REFUNDED':
            return '환불완료'

        case 'CANCELED':
            return '결제취소'

        default:
            return status

    }

}

// 결제수단 한글
const getPaymentMethodText = (method) => {

    switch (method) {

        case 'CARD':
            return '카드'

        case 'CASH':
            return '현금'

        case 'E_PAY':
            return '모바일결제'

        case 'COUPON':
            return '쿠폰'

        case 'TOSS':
            return '토스페이'

        default:
            return method

    }

}
</script>


<template>

<div class="container">

    <!-- 왼쪽 : 주문 목록 -->
    <div class="left">

        <button @click="goBack">
            ← 메인으로 돌아가기
        </button>

        <h2>주문 목록</h2>

        <table>

            <thead>
                <tr>
                    <th class="order-number">주문번호</th>
                    <th>주문상태</th>
                    <th>결제상태</th>
                    <th>결제금액</th>
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
                        <span :class="getStatusClass(order.orderStatus)">
                            {{ getStatusText(order.orderStatus) }}
                        </span>
                    </td>

                    <td>
                        {{ getPaymentStatusText(order.paymentStatus) }}
                    </td>

                    <td>
                        {{ order.finalAmount != null 
                            ? order.finalAmount.toLocaleString()
                            : '-' 
                        }}원
                    </td>

                </tr>

            </tbody>

        </table>

    </div>

    <!-- 오른쪽 : 주문 상세 -->
    <div class="right">

        <h2>주문 상세</h2>

        <div v-if="selectedOrder">

            <p><strong>주문번호</strong> : {{ selectedOrder.orderNumber }}</p>

            <p><strong>주문유형</strong> : {{ selectedOrder.orderType }}</p>

            <p><strong>주문상태</strong> : {{ getStatusText(selectedOrder.orderStatus) }}</p>

            <hr>

            <div class="payment-box">

                <h3>결제 정보</h3>

                <div class="payment-row">
                    <span>상품금액</span>
                    <span>{{ selectedOrder.baseAmount.toLocaleString() }}원</span>
                </div>

                <div class="payment-row">
                    <span>쿠폰할인</span>
                    <span>-{{ selectedOrder.couponDiscount.toLocaleString() }}원</span>
                </div>

                <div class="payment-row">
                    <span>포인트 사용</span>
                    <span>{{ selectedOrder.pointUsed.toLocaleString() }}P</span>
                </div>

                <div class="payment-row payment-final">
                    <span>최종 결제금액</span>
                    <span>{{ selectedOrder.finalAmount.toLocaleString() }}원</span>
                </div>

                <hr>

                <div class="payment-row">
                    <span>결제수단</span>
                    <span>{{ getPaymentMethodText(selectedOrder.paymentMethod) }}</span>
                </div>

                <div class="payment-row">
                    <span>결제상태</span>
                    <span>{{ getPaymentStatusText(selectedOrder.paymentStatus) }}</span>
                </div>

                <div class="payment-row">
                    <span>결제시간</span>
                    <span>{{ selectedOrder.paymentDate }}</span>
                </div>

            </div>

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

            <div
                v-for="item in selectedOrder.items"
                :key="item.productName"
            >

                <h3>{{ item.productName }}</h3>

                <p>
                    수량 : {{ item.quantity }}
                </p>

                <p>
                    단가 : {{ item.unitPrice.toLocaleString() }}원
                </p>

                <b class="option-title">
                    옵션
                </b>

                <ul class="option-list">

                    <li
                        v-for="option in item.options"
                        :key="option.optionType + option.optionName"
                    >

                        <span class="option-type">
                            {{ option.optionType }}
                        </span>

                        <span class="option-name">
                            {{ option.optionName }}
                        </span>

                        <span class="option-price">
                            +{{ option.extraPrice.toLocaleString() }}원
                        </span>

                    </li>
                    <li>
                       
                            드라이아이스 :
                            {{ selectedOrder?.dryIceCount ?? 0 }}개
                        
                    </li>

                </ul>

                

                <p>
                    <strong>맛</strong>
                </p>

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

.order-number{

    width:120px;

}

th:first-child,
td:first-child{

    width:90px;

    white-space:nowrap;

}

.status-waiting,
.status-preparing,
.status-completed,
.status-canceled{

    white-space:nowrap;

}

.container{

    display:flex;

    gap:30px;

    padding:30px;

    background:#f8f9fa;

    min-height:100vh;

    box-sizing:border-box;

    align-items:flex-start;

}

.left{

    flex:0 0 45%;

    min-width:450px;

    max-height:calc(100vh - 60px);

    overflow-y:auto;


    background:white;

    padding:25px;

    border-radius:15px;

    box-shadow:0 4px 12px rgba(0,0,0,0.08);

}

.right{

    flex:1;

    min-width:500px;

    max-height:calc(100vh - 60px);

    overflow-y:auto;


    background:white;

    padding:30px;

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

    min-width:400px;

    table-layout:fixed;

    border-collapse:separate;

    border-spacing:0;

    overflow:hidden;

    border-radius:12px;

    table-layout:fixed;

}


thead{

    background:#333;

    color:white;

}


th{

    padding:18px;

    font-size:15px;

}


td{

    padding:18px;
    text-align:center;
    font-size:15px;

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

/* 옵션 */

.option-title{
    margin-top:12px;
    margin-bottom:8px;
    font-size:15px;
    font-weight:bold;
    color:#333;
}

.option-list{
    list-style:none;
    padding:0;
    margin:0 0 15px 0;
}

.option-list li{
    display:flex;
    justify-content:space-between;
    align-items:center;
    padding:8px 12px;
    margin-bottom:8px;
    background:#f8f9fa;
    border-radius:8px;
    font-size:14px;
}

.option-type{
    font-weight:bold;
    color:#666;
}

.option-name{
    flex:1;
    margin-left:12px;
}

.option-price{
    color:#ff6b00;
    font-weight:bold;
}

/* =========================
   결제 정보
========================= */

.payment-box{

    margin:20px 0;

    padding:20px;

    background:#fafafa;

    border:1px solid #e5e5e5;

    border-radius:12px;

}

.payment-box h3{

    margin-top:0;

    margin-bottom:20px;

    color:#333;

}

.payment-row{

    display:flex;

    justify-content:space-between;

    align-items:center;

    padding:10px 0;

    border-bottom:1px dashed #e5e5e5;

}

.payment-row:last-child{

    border-bottom:none;

}

.payment-final{

    margin-top:10px;

    padding-top:15px;

    border-top:2px solid #ddd;

    border-bottom:none;

    font-size:18px;

    font-weight:bold;

    color:#ff6b00;

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