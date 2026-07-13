<template>
  <div class="order-confirm-container">
    <div class="header-section" style="display: flex; justify-content: space-between; align-items: center;">
      <h2>STEP 01. 주문 내역을 확인해주세요</h2>
      <button class="btn-back-cart" @click="goBackToCart">장바구니로 돌아가기 🔙</button>
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
  const orderId = route.query.orderId;
  if (!orderId) {
    router.push('/menu');
    return;
  }
  
  if (!confirm('현재 결제를 취소하고 장바구니로 돌아가시겠습니까? (장바구니 내역은 유지됩니다)')) return;

  try {
    // 1. 주문 취소 API 호출
    await axios.post(`/api/orders/${orderId}/cancel`);
    // 2. 장바구니 화면으로 복귀
    router.push('/menu');
  } catch (error) {
    console.error('주문 취소 실패:', error);
    alert('주문 취소 중 오류가 발생했습니다.');
  }
};

const handlePayment = async (method) => {
  const orderId = route.query.orderId;
  
  if (!orderId) {
    alert('주문 번호를 찾을 수 없습니다. 처음부터 다시 시도해주세요.');
    router.push('/menu');
    return;
  }

  try {
    // 🌟 2단계: 방금 만든 주문서로 결제 및 재고 차감 진행
    await axios.post(`/api/orders/${orderId}/pay`, { paymentMethod: method });

    alert(`${method === 'CARD' ? '신용카드' : '현금'} 결제가 완료되었습니다! 영수증 번호를 확인해주세요.`);
    
    // 3단계: 장바구니 초기화 및 메인 이동 대신 완료화면으로 이동
    await basketStore.fetchBasket(); // 서버에서 세션이 비워졌으니 화면 갱신
    router.push(`/order-complete?orderId=${orderId}`); 
  } catch (error) {
    console.error('결제 오류 발생:', error);
    const errorMsg = error.response?.data?.error || '결제 처리 중 오류가 발생했습니다.';
    
    // 예외 발생 시 안내 팝업 후 장바구니 복귀 여부 묻기
    if (confirm(`결제 실패: ${errorMsg}\n장바구니로 돌아가 주문을 수정하시겠습니까?`)) {
      try {
        await axios.post(`/api/orders/${orderId}/cancel`);
        router.push('/menu');
      } catch (cancelError) {
        alert('주문 취소 및 장바구니 이동에 실패했습니다.');
        router.push('/menu');
      }
    }
  }
};

const handleTossPayment = async () => {
  const orderId = route.query.orderId;
  if (!orderId) {
    alert('주문 번호를 찾을 수 없습니다. 처음부터 다시 시도해주세요.');
    router.push('/menu');
    return;
  }

  try {
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
      amount: basketStore.totalPrice,
      orderId: `kiosk_order_${orderId}`, // 토스는 최소 6자리를 요구하므로 prefix를 붙임
      orderName: orderName,
      customerName: '키오스크고객',
      successUrl: window.location.origin + '/toss/success',
      failUrl: window.location.origin + '/toss/fail',
    });
  } catch (error) {
    console.error('토스 결제창 호출 오류:', error);
    alert('결제창을 띄우는 중 오류가 발생했습니다.');
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