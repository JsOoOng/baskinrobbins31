<template>
  <div class="order-confirm-container">
    <div class="header-section" style="display: flex; justify-content: space-between; align-items: center;">
      <h2>STEP 02. 쿠폰/결제 (주문 내역 확인)</h2>
      <button class="btn-back-cart" @click="goBackToCart">이전 화면으로 🔙</button>
    </div>
    
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
            <span>수량: {{ item.quantity }}개</span>
            <!-- 결제 단계이므로 개별 삭제 버튼은 제거됨 -->
            </div>
        </div>
    </div>

    <div class="payment-section">
      <h3>결제 방법을 선택해주세요 (총 ₩{{ basketStore.totalPrice.toLocaleString() }})</h3>
      <div class="pay-buttons">
        <button @click="handlePayment('CASH')">현금</button>
        <button @click="handlePayment('CARD')">신용카드</button>
        <button class="btn-toss" @click="handleTossPayment">토스 간편결제 💳</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { useBasketStore } from '@/stores/customer/basket';
import { loadTossPayments } from '@tosspayments/payment-sdk';
import axios from '@/api/axios';

const router = useRouter();
const route = useRoute();
const basketStore = useBasketStore();

// 토스페이먼츠 클라이언트 키 (.env 파일에서 로드)
const TOSS_CLIENT_KEY = import.meta.env.VITE_TOSS_CLIENT_KEY;

onMounted(() => {
  if (basketStore.cartItems.length === 0) {
    alert('장바구니가 비어있어 메인으로 돌아갑니다.');
    router.push('/');
  }
});

const goBackToCart = async () => {
  router.push(`/point-discount`);
};

const handlePayment = async (method) => {
  if (basketStore.cartItems.length === 0) {
    alert('장바구니가 비어있습니다.');
    return;
  }

  try {
    // 1. 주문 생성 API 호출 (DB 저장 및 포인트 적립)
    const orderRes = await axios.post('/api/orders', {
      orderType: basketStore.orderType || 'TOGO',
      dryIceCount: basketStore.dryIceCount || 0,
      dryIceMins: basketStore.dryIceMins || 0,
      phoneNumber: basketStore.phoneNumber || null,
      kioskId: 1,
      storeId: 1
    });
    const orderId = orderRes.data;

    // 2. 결제 완료 API 호출
    await axios.post(`/api/orders/${orderId}/pay`, {
      paymentMethod: method,
      pointUsed: basketStore.usedPoints || 0
    });
    
    // 3. 결제 완료 화면으로 이동
    router.push(`/order-complete?orderId=${orderId}`);
  } catch (error) {
    console.error('결제 처리 중 오류 발생:', error);
    alert('결제 처리 중 오류가 발생했습니다.');
  }
};

const handleTossPayment = async () => {
  if (basketStore.cartItems.length === 0) {
    alert('장바구니가 비어있습니다. 처음부터 다시 시도해주세요.');
    router.push('/menu');
    return;
  }

  try {
    // 1. 토스 띄우기 전 주문 생성 API 호출 (DB 저장 및 포인트 적립)
    const orderRes = await axios.post('/api/orders', {
      orderType: basketStore.orderType || 'TOGO',
      dryIceCount: basketStore.dryIceCount || 0,
      dryIceMins: basketStore.dryIceMins || 0,
      phoneNumber: basketStore.phoneNumber || null,
      kioskId: 1,
      storeId: 1
    });
    const orderId = orderRes.data;

    const tossPayments = await loadTossPayments(TOSS_CLIENT_KEY);
    
    let orderName = '주문 상품';
    if (basketStore.cartItems.length > 0) {
      if (basketStore.cartItems.length === 1) {
        orderName = basketStore.cartItems[0].productName;
      } else {
        orderName = `${basketStore.cartItems[0].productName} 외 ${basketStore.cartItems.length - 1}건`;
      }
    }
    
    // 토스페이먼츠 결제창 호출 (파라미터 전달)
    await tossPayments.requestPayment('카드', {
      amount: basketStore.totalPrice - (basketStore.usedPoints || 0),
      orderId: `kiosk_order_${orderId}`, // 토스는 최소 6자리 요구하여 prefix를 붙임
      orderName: orderName,
      customerName: '키오스크고객',
      successUrl: window.location.origin + `/toss/success?pointUsed=${basketStore.usedPoints || 0}`,
      failUrl: window.location.origin + '/toss/fail',
    });
  } catch (error) {
    console.error('토스 결제창 호출 오류:', error);
    alert('토스 결제창을 열 수 없습니다.');
  }
};
</script>

<style scoped>
/* 필요한 스타일이 있다면 추가 (기존 스타일 유지) */
.btn-back-cart {
  padding: 8px 16px;
  background-color: #ff6b6b;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-weight: bold;
}
.btn-back-cart:hover {
  background-color: #fa5252;
}
</style>