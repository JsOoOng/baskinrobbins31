<template>
  <div class="toss-success-container">
    <div class="loading-card">
      <div class="spinner"></div>
      <h2>결제 승인 진행 중...</h2>
      <p>창을 닫지 마시고 잠시만 기다려주세요.</p>
    </div>
  </div>
</template>

<script setup>
import { onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import axios from '@/api/axios';
import { useBasketStore } from '@/stores/customer/basket';

const route = useRoute();
const router = useRouter();
const basketStore = useBasketStore();

onMounted(async () => {
  const { paymentKey, orderId, amount, pointUsed } = route.query;

  if (!paymentKey || !orderId || !amount) {
    alert('비정상적인 접근입니다.');
    router.push('/');
    return;
  }

  try {
    // 백엔드로 승인 요청
    await axios.post('/api/orders/toss/confirm', {
      paymentKey,
      orderId, // ex) kiosk_order_35
      amount: Number(amount),
      pointUsed: Number(pointUsed || 0)
    });

    // 성공 시 장바구니 초기화 후 완료 화면 이동
    await basketStore.fetchBasket();
    
    // orderId 파싱 (kiosk_order_35 -> 35)
    const realOrderId = orderId.replace('kiosk_order_', '');
    router.push(`/order-complete?orderId=${realOrderId}`);
    
  } catch (error) {
    console.error('결제 승인 에러:', error);
    const errorMsg = error.response?.data?.error || '결제 승인 중 오류가 발생했습니다.';
    alert(`결제 실패: ${errorMsg}`);
    
    // 장바구니로 복귀
    router.push('/menu');
  }
});
</script>

<style scoped>
.toss-success-container {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: calc(100vh - 100px);
  background-color: #f8f9fa;
}

.loading-card {
  background: white;
  padding: 60px 40px;
  border-radius: 20px;
  text-align: center;
  box-shadow: 0 10px 40px rgba(0,0,0,0.1);
  width: 500px;
  max-width: 90%;
}

.spinner {
  width: 60px;
  height: 60px;
  border: 6px solid #ffecf0;
  border-top: 6px solid #ff7c98;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 20px auto;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

h2 {
  color: #ff6b6b;
  margin-bottom: 15px;
}
</style>
