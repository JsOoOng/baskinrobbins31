<!--
  [화면 흐름 안내] OrderCompleteView
  역할: 고객 키오스크에서 사용자가 보는 화면이다.
  진입: /order-complete -> 이 Vue 파일 렌더링
  데이터: 사용자 동작 -> @/api/axios -> 응답/상태 반영
  다음 이동: /kiosk
-->
<template>
  <div class="order-complete-container">
    <div class="complete-card">
      <div class="icon-success">✔️</div>
      <h2>{{ $t('주문이 완료되었습니다!') }}</h2>
      <p class="desc">{{ $t('아래의 주문번호가 호출되면 상품을 받아가세요.') }}</p>
      
      <div class="order-number-box">
        <span class="label">{{ $t('주문 번호') }}</span>
        <span class="number">{{ orderNumber }}</span>
      </div>
      
      <p class="timer-desc">{{ $t('잠시 후 처음 화면으로 돌아갑니다 ({countdown}초)', { countdown: countdown }) }}</p>
      
      <button class="btn-home" @click="goHome">{{ $t('처음으로 돌아가기') }}</button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useI18n } from 'vue-i18n';
import axios from '@/api/axios';

const route = useRoute();
const router = useRouter();
const { t } = useI18n({ useScope: 'global' });

const orderNumber = ref(t('조회중...'));
const countdown = ref(7);
let timerInterval = null;

const fetchOrderNumber = async () => {
  const orderId = route.query.orderId;
  if (!orderId) {
    orderNumber.value = t('오류');
    return;
  }
  
  try {
    const res = await axios.get(`/api/orders/${orderId}`);
    if (res.data && res.data.orderId) {
      orderNumber.value = res.data.orderNumber;
    } else {
      orderNumber.value = t('오류'); // fallback
    }
  } catch (err) {
    console.error('주문 정보 조회 실패:', err);
    orderNumber.value = t('오류'); // fallback
  }
};

const goHome = () => {
  clearInterval(timerInterval);
  router.push('/kiosk');
};

onMounted(() => {
  fetchOrderNumber();
  
  timerInterval = setInterval(() => {
    countdown.value--;
    if (countdown.value <= 0) {
      goHome();
    }
  }, 1000);
});

onUnmounted(() => {
  clearInterval(timerInterval);
});
</script>

<style scoped>
.order-complete-container {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: calc(100vh - 100px);
  background-color: #f8f9fa;
}

.complete-card {
  background: white;
  padding: 60px 40px;
  border-radius: 20px;
  text-align: center;
  box-shadow: 0 10px 40px rgba(0,0,0,0.1);
  width: 500px;
  max-width: 90%;
}

.icon-success {
  font-size: 5rem;
  margin-bottom: 20px;
}

h2 {
  color: #ff6b6b;
  font-size: 2rem;
  margin-bottom: 10px;
}

.desc {
  color: #666;
  font-size: 1.2rem;
  margin-bottom: 40px;
}

.order-number-box {
  background: #fff0f3;
  border: 4px solid #ff7c98;
  border-radius: 20px;
  padding: 30px;
  margin-bottom: 40px;
}

.order-number-box .label {
  display: block;
  font-size: 1.2rem;
  color: #ff7c98;
  font-weight: bold;
  margin-bottom: 10px;
}

.order-number-box .number {
  display: block;
  font-size: 5rem;
  font-weight: 900;
  color: #ff6b6b;
}

.timer-desc {
  color: #999;
  margin-bottom: 20px;
}

.btn-home {
  background: #333;
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
  background: #555;
}
</style>
