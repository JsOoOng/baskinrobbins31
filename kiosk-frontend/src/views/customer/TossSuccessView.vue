<!--
  [화면 흐름 안내] TossSuccessView
  역할: 고객 키오스크에서 사용자가 보는 화면이다.
  진입: /toss/success -> 이 Vue 파일 렌더링
  데이터: 사용자 동작 -> @/api/axios -> 응답/상태 반영
  다음 이동: /kiosk, /menu
-->
<template>
  <div class="toss-success-container">
    <div class="loading-card">
      <div class="spinner"></div>
      <h2>결제 승인 진행 중...</h2>
      <p>창을 닫지 마시고 잠시만 기다려주세요.</p>
    </div>

    <!-- Alert Modal -->
    <div class="alert-modal" v-if="showAlert">
      <div class="alert-content">
        <p>{{ alertMessage }}</p>
        <button @click="closeAlert">확인</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import axios from '@/api/axios';
import { useBasketStore } from '@/stores/customer/basket';

const route = useRoute();
const router = useRouter();
const basketStore = useBasketStore();

const showAlert = ref(false);
const alertMessage = ref('');
const closeAlertCallback = ref(null);

const displayAlert = (msg, callback = null) => {
  alertMessage.value = msg;
  showAlert.value = true;
  closeAlertCallback.value = callback;
};

const closeAlert = () => {
  showAlert.value = false;
  if (closeAlertCallback.value) {
    closeAlertCallback.value();
    closeAlertCallback.value = null;
  }
};

onMounted(async () => {
  const { paymentKey, orderId, amount, pointUsed } = route.query;

  if (!paymentKey || !orderId || !amount) {
    displayAlert('비정상적인 접근입니다.', () => {
      router.push('/kiosk');
    });
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
    displayAlert(`결제 실패: ${errorMsg}`, () => {
      // 장바구니로 복귀
      router.push('/menu');
    });
  }
});
</script>

<style scoped>
/* Alert Modal */
.alert-modal {
  position: fixed; top: 0; left: 0; width: 100vw; height: 100vh;
  background: rgba(0,0,0,0.5); display: flex; justify-content: center; align-items: center; z-index: 2000;
}
.alert-content {
  background: white; padding: 30px; border-radius: 10px; text-align: center; width: 300px;
}
.alert-content p {
  font-size: 1.1rem; margin-bottom: 20px;
}
.alert-content button {
  background-color: #ff007f; color: white; border: none; padding: 10px 20px; border-radius: 5px; font-size: 1rem; cursor: pointer;
}

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
