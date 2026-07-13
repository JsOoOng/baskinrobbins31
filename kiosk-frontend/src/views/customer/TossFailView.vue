<template>
  <div class="toss-fail-container">
    <div class="fail-card">
      <div class="icon-fail">❌</div>
      <h2>결제에 실패했습니다</h2>
      <p class="desc">{{ errorMessage }}</p>
      
      <button class="btn-home" @click="goBack">장바구니로 돌아가기</button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import axios from '@/api/axios';

const route = useRoute();
const router = useRouter();
const errorMessage = ref('알 수 없는 오류');

onMounted(async () => {
  if (route.query.message) {
    errorMessage.value = route.query.message;
  }
  
  // 주문 취소 처리 (orderId가 쿼리로 왔다고 가정)
  const orderId = route.query.orderId;
  if (orderId) {
    try {
      const realOrderId = orderId.replace('kiosk_order_', '');
      await axios.post(`/api/orders/${realOrderId}/cancel`);
    } catch (e) {
      console.error('주문 취소 실패', e);
    }
  }
});

const goBack = () => {
  router.push('/menu');
};
</script>

<style scoped>
.toss-fail-container {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: calc(100vh - 100px);
  background-color: #f8f9fa;
}

.fail-card {
  background: white;
  padding: 60px 40px;
  border-radius: 20px;
  text-align: center;
  box-shadow: 0 10px 40px rgba(0,0,0,0.1);
  width: 500px;
  max-width: 90%;
}

.icon-fail {
  font-size: 5rem;
  margin-bottom: 20px;
}

h2 {
  color: #ff6b6b;
  font-size: 2rem;
  margin-bottom: 15px;
}

.desc {
  color: #666;
  font-size: 1.2rem;
  margin-bottom: 40px;
}

.btn-home {
  background: #ff7c98;
  color: white;
  border: none;
  padding: 15px 40px;
  font-size: 1.2rem;
  border-radius: 10px;
  cursor: pointer;
  font-weight: bold;
  transition: 0.2s;
  width: 100%;
}

.btn-home:hover {
  background: #e66885;
}
</style>
