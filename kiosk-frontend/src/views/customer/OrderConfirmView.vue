<template>
  <div class="order-confirm-container">
    <h2>STEP 01. 주문 내역을 확인해주세요</h2>
    
    <div class="order-list">
      <div v-for="(item, index) in basketStore.items" :key="index" class="order-item">
        <div class="item-info">
          <h4>{{ item.productName }}</h4>
          <p>옵션 등 상세 내역...</p>
        </div>
        <div class="item-actions">
          <span>{{ item.quantity }}개</span>
          <button @click="removeItem(item)">❌</button>
        </div>
      </div>
    </div>

    <div class="payment-section">
      <h3>결제 방법을 선택해주세요 (총 ₩{{ basketStore.totalPrice.toLocaleString() }})</h3>
      <div class="pay-buttons">
        <button @click="handlePayment('CASH')">현금</button>
        <button @click="handlePayment('CARD')">신용카드</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useBasketStore } from '@/stores/customer/basket';

const router = useRouter();
const basketStore = useBasketStore();

onMounted(() => {
  if (basketStore.items.length === 0) {
    alert('장바구니가 비어있어 메인으로 돌아갑니다.');
    router.push('/');
  }
});

const handlePayment = async (method) => {
  // 실제로는 모달 등에서 매장/포장 여부(orderType)를 선택하게 해야 함
  const orderInfo = {
    storeId: 1,
    kioskId: 1, // 또는 2
    userId: null, // 비회원 가정
    orderType: 'TOGO', 
    dryIceMins: 30
  };

  try {
    await basketStore.submitOrder(orderInfo); // POST /api/orders 호출
    alert(`${method} 결제가 완료되었습니다! 영수증 번호를 확인해주세요.`);
    await basketStore.clearBasket(); // 쿠키 및 스토어 비우기
    router.push('/'); // 첫 화면으로 복귀
  } catch (error) {
    alert('결제 처리 중 오류가 발생했습니다.');
  }
};
</script>