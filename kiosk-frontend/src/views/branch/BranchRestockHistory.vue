<template>
<div class="menu-page">
    <button @click="goBack">
        ← 메인으로 돌아가기
    </button>

    <h2>재고 신청 현황</h2>
    <table>
        <thead>
            <tr>
                <th>상품명</th>
                <th>신청 수량</th>
                <th>상태</th>
                <th>요청 시간</th>
                <th>관리자</th>
                <th>취소</th>
            </tr>
        </thead>
        <tbody>
            <tr v-for="req in restockRequests" :key="req.requestId">
                <td>{{ req.itemName }}</td>
                <td>{{ req.requestQuantity }} {{ req.unit }}</td>
                <td>
                    <span v-if="req.status === 'WAITING'">대기중</span>
                    <span v-else-if="req.status === 'APPROVED'">승인됨</span>
                    <span v-else-if="req.status === 'SHIPPING'">배송중</span>
                    <span v-else-if="req.status === 'COMPLETED'">완료됨</span>
                    <span v-else-if="req.status === 'CANCELED'">취소됨</span>
                    <span v-else-if="req.status === 'REJECTED'">반려됨</span>
                    <span v-else>{{ req.status }}</span>
                </td>
                <td>{{ new Date(req.requestedAt).toLocaleString() }}</td>
                <td>{{ req.adminName || '-' }}</td>
                <td>
                    <button v-if="req.status === 'WAITING'" @click="cancelRestockRequest(req.requestId)" style="background-color: #dc3545; color: white;">
                        취소
                    </button>
                </td>
            </tr>
            <tr v-if="restockRequests.length === 0">
                <td colspan="6" style="text-align: center; padding: 20px;">신청 내역이 없습니다.</td>
            </tr>
        </tbody>
    </table>
</div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import api from '@/api/axios'

const router = useRouter()
let intervalId = null
const user = JSON.parse(localStorage.getItem('branchUser'))

// 재고 신청 내역
const restockRequests = ref([])

// 메인 이동
const goBack = () => {
    router.push('/branch/main')
}

// 재고 신청 내역 조회
const loadRestockRequests = async () => {
    try {
        const response = await api.get(`/branch/status/restock/${user.storeId}`)
        restockRequests.value = response.data
    } catch (e) {
        console.error('재고 신청 내역 조회 실패', e)
    }
}

// 재고 신청 취소
const cancelRestockRequest = async (requestId) => {
    if (!confirm('정말 취소하시겠습니까?')) return
    try {
        await api.post(`/branch/status/restock/${requestId}/cancel`)
        alert('발주 요청이 취소되었습니다.')
        await loadRestockRequests()
    } catch (e) {
        console.error('발주 취소 실패', e)
        alert('취소 실패')
    }
}

onMounted(() => {
    // 최초 조회
    loadRestockRequests()

    // 5초마다 자동 새로고침
    intervalId = setInterval(() => {
        loadRestockRequests()
    }, 5000)
})

onUnmounted(() => {
    clearInterval(intervalId)
})
</script>

<style scoped>
.menu-page {
    height:100vh;
    overflow-y:auto;
    padding:30px;
    box-sizing:border-box;
}

/* 뒤로가기 버튼 */
.menu-page > button {
    margin-bottom:25px;
    padding:10px 18px;
    border:none;
    border-radius:10px;
    background:#222;
    color:white;
    font-size:14px;
    cursor:pointer;
    transition:0.2s;
}

.menu-page > button:hover {
    background:#555;
}

.menu-page > button:first-child {
    margin-top:10px;
    margin-bottom:25px;
}

h2 {
    margin:30px 0 20px;
    color:#333;
}

/* 테이블 영역 */
table {
    width:100%;
    background:white;
    border-collapse:separate;
    border-spacing:0;
    border-radius:15px;
    overflow:hidden;
    box-shadow:0 4px 12px rgba(0,0,0,0.08);
    margin-bottom:30px;
}

thead {
    background:#333;
    color:white;
}

th {
    padding:15px;
    font-size:14px;
}

td {
    padding:15px;
    text-align:center;
    border-bottom:1px solid #eee;
    color:#444;
}

tbody tr {
    transition:0.2s;
}

tbody tr:hover {
    background:#f7f7f7;
}

tbody tr:last-child td {
    border-bottom:none;
}

td button {
    padding:8px 15px;
    border:none;
    border-radius:8px;
    cursor:pointer;
    transition:0.2s;
}

td button:hover {
    opacity:0.8;
}

/* 반응형 */
@media(max-width:900px){
    .menu-page{
        padding:15px;
    }
    table{
        font-size:13px;
    }
    th,td{
        padding:10px;
    }
}
</style>
