<template>
  <div class="order-confirm-container">
    <h2>STEP 01. 주문 내역을 확인해주세요</h2>
    
    <div class="order-list">
        <div v-for="(item, index) in basketStore.cartItems" :key="index" class="order-item">
            <div class="item-info">
            <h4>{{ item.productName }}</h4>
            
            <div v-if="item.flavors && item.flavors.length > 0" class="flavor-list" style="font-size: 0.9rem; color: #666;">
                선택한 맛: 
                <span v-for="(f, fIndex) in item.flavors" :key="fIndex">
                {{ f.flavorName || '맛ID:' + f.flavorId }}
                {{ f.quantity > 1 ? '(' + f.quantity + ')' : '' }}
                {{ fIndex < item.flavors.length - 1 ? ', ' : '' }}
                </span>
            </div>
            
            </div>
            <div class="item-actions">
            <span>{{ item.quantity }}개</span>
            <button @click="removeItem(index)">❌</button>
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
import { useRouter, useRoute } from 'vue-router';
import { useBasketStore } from '@/stores/customer/basket';

import axios from '@/api/axios';

const router = useRouter();
const route = useRoute();
const basketStore = useBasketStore();

onMounted(() => {
  if (basketStore.cartItems.length === 0) {
    alert('장바구니가 비어있어 메인으로 돌아갑니다.');
    router.push('/');
  }
});

const handlePayment = async (method) => {
  const orderId = route.query.orderId;
  
  if (!orderId) {
    alert('주문 번호를 찾을 수 없습니다. 처음부터 다시 시도해주세요.');
    router.push('/');
    return;
  }

  try {
    // 🌟 2단계: 방금 만든 주문서로 결제 및 재고 차감 진행 (POST /api/orders/{orderId}/pay)
    await axios.post(`/api/orders/${orderId}/pay`, { paymentMethod: method });

    alert(`${method === 'CARD' ? '신용카드' : '현금'} 결제가 완료되었습니다! 영수증 번호를 확인해주세요.`);
    
    // 3단계: 장바구니 초기화 및 메인 이동
    await basketStore.fetchBasket(); // 서버에서 세션이 비워졌으니 화면 갱신
    router.push('/'); 
  } catch (error) {
    console.error(error);
    const errorMsg = error.response?.data?.error || '결제 처리 중 오류가 발생했습니다.';
    alert(errorMsg);
  }
};

// 💡 삭제 로직도 index를 받도록 추가해 줘 (아까 에러 났던 부분 방지)
const removeItem = async (index) => {
  if (!confirm('이 메뉴를 장바구니에서 삭제하시겠습니까?')) return;
  try {
    await axios.delete(`/api/customer/basket/${index}`);
    await basketStore.fetchBasket();
    if (basketStore.cartItems.length === 0) {
        alert('장바구니가 비어있어 메인으로 돌아갑니다.');
        router.push('/');
    }
  } catch (error) {
    alert('삭제 실패');
  }
};
</script>