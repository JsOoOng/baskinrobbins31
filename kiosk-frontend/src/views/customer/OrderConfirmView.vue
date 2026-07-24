<!--
  [화면 흐름 안내] OrderConfirmView
  역할: 고객 키오스크에서 사용자가 보는 화면이다.
  진입: /payment, /order-confirm -> 이 Vue 파일 렌더링
  데이터: 사용자 동작 -> @/api/axios -> 응답/상태 반영
  다음 이동: /kiosk
-->
<template>
  <div class="order-confirm-container">
    <div class="header-section">
      <h2>{{ $t('STEP 02. 결제') }} <span class="sub-title">{{ $t('(주문 내역 확인)') }}</span></h2>
      <button class="btn-back-cart" @click="goBackToCart">{{ $t('이전 화면으로') }} 🔙</button>
    </div>
    
    <div class="order-list">
        <div v-for="(item, index) in basketStore.cartItems" :key="index" class="order-item">
            <div class="item-info">
            <h4>{{ formatCartItemName(item.productName) }}</h4>
            
            <div v-if="item.flavors && item.flavors.length > 0" class="flavor-list">
                {{ $t('선택한 맛:') }} 
                <span v-for="(f, fIndex) in item.flavors" :key="fIndex">
                {{ f.flavorName ? $t(f.flavorName) : $t('맛ID:') + f.flavorId }}
                {{ f.quantity > 1 ? '(' + f.quantity + ')' : '' }}
                {{ fIndex < item.flavors.length - 1 ? ', ' : '' }}
                </span>
            </div>
            
            </div>
            <div class="item-actions">
            <span>{{ $t('수량: {quantity}개', { quantity: item.quantity }) }}</span>
            <!-- 결제 단계이므로 개별 삭제 버튼은 제거됨 -->
            </div>
        </div>
    </div>

    <div class="payment-section">
      <h3>{{ $t('결제 방법을 선택해주세요 (총 ₩{total})', { total: (basketStore.totalPrice - (basketStore.usedPoints || 0)).toLocaleString() }) }}</h3>
      <div class="pay-buttons">
        <button @click="handlePayment('CASH')">{{ $t('현금') }}</button>
        <button @click="handlePayment('CARD')">{{ $t('신용카드') }}</button>
        <button class="btn-toss" @click="handleTossPayment">{{ $t('간편결제') }}</button>
      </div>
    </div>

    <!-- Alert Modal -->
    <div class="alert-modal" v-if="showAlert">
      <div class="alert-content">
        <p>{{ alertMessage }}</p>
        <button @click="closeAlert">{{ $t('확인') }}</button>
      </div>
    </div>
  </div>
</template>

<script setup>

import { onMounted, ref} from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { useBasketStore } from '@/stores/customer/basket';
import { useI18n } from 'vue-i18n';
import { loadTossPayments } from '@tosspayments/payment-sdk';
import axios from '@/api/axios';

const router = useRouter();
const route = useRoute();
const basketStore = useBasketStore();
const { t } = useI18n({ useScope: 'global' });


/* 옵션 표기가 붙은 상품명에서 결제 화면에 보여줄 기본 이름만 추출합니다. */
const formatCartItemName = (name) => {
  if (name.includes(' (')) {
    const parts = name.split(' (');
    const base = t(parts[0]);
    let container = parts[1].replace(')', '');
    if (container === 'CUP') container = t('컵');
    if (container === 'CONE') container = t('콘');
    if (container === 'WAFFLE') container = t('와플콘');
    return `${base} (${container})`;
  }
  return t(name);
}

const showAlert = ref(false);
const alertMessage = ref('');
const closeAlertCallback = ref(null);

/*
 * 결제 오류 안내 모달을 열고, 확인 후 실행할 선택적 콜백도 함께 저장합니다.
 */
const displayAlert = (msg, callback = null) => {
  alertMessage.value = msg;
  showAlert.value = true;
  closeAlertCallback.value = callback;
};

/* 안내 모달을 닫은 뒤 저장된 후속 이동 콜백이 있으면 한 번 실행합니다. */
const closeAlert = () => {
  showAlert.value = false;
  if (closeAlertCallback.value) {
    closeAlertCallback.value();
    closeAlertCallback.value = null;
  }
};

// 토스페이먼츠 클라이언트 키 (.env 파일에서 로드)
const TOSS_CLIENT_KEY = import.meta.env.VITE_TOSS_CLIENT_KEY;

onMounted(() => {
  if (basketStore.cartItems.length === 0) {
    displayAlert(t('장바구니가 비어있어 메인으로 돌아갑니다.'), () => {
      router.push('/kiosk');
    });
  }
});

/* 결제 직전 화면에서 쿠폰·포인트 선택 화면으로 돌아갑니다. */
const goBackToCart = async () => {
  router.push(`/point-discount`);
};

/*
 * 현금·카드 결제 전체 흐름
 * 장바구니 백업 → POST /api/orders로 주문 생성
 * → POST /api/orders/{orderId}/pay로 할인·결제 저장
 * → 성공 시 장바구니 삭제 및 /order-complete 이동
 * → 실패 시 백업 장바구니 복원 순서입니다.
 */
const handlePayment = async (method) => {
  if (basketStore.cartItems.length === 0) {
    displayAlert(t('장바구니가 비어있습니다.'));
    return;
  }

  // 💡 1. 결제 시도 전 현재 장바구니 내역을 안전하게 백업 (깊은 복사)
  const backupCartItems = JSON.parse(JSON.stringify(basketStore.cartItems));

  try {
    // 2. 주문 생성 API 호출 (이때 백엔드에서 재고 부족 검증 수행)
    const orderRes = await axios.post('/api/orders', {
      orderType: basketStore.orderType || 'TOGO',
      dryIceCount: basketStore.dryIceCount || 0,
      dryIceMins: basketStore.dryIceMins || 0,
      phoneNumber: basketStore.phoneNumber || null,
      pointUsed: basketStore.usedPoints || 0,
      userCouponId: basketStore.usedCouponId || null,
      kioskId: Number(localStorage.getItem('kioskId')) || 1,
      storeId: Number(localStorage.getItem('storeId')) || 1,
      items: basketStore.cartItems.map(item => {
        const baseItem = {
          productId: item.productId || item.id,
          quantity: item.quantity,
          unitPrice: item.unitPrice
        };

        // 💡 기존 맛 데이터 구조({ flavorName, quantity })에 맞춰 flavorId도 유연하게 찾아서 전송
        return {
          ...baseItem,
          flavors: item.flavors ? item.flavors.map(f => ({
            flavorId: f.flavorId || f.id || 0, // flavorId가 없다면 id나 기본값 처리
            flavorName: f.flavorName,          // 맛 이름도 함께 전달 필요할 수 있음
            quantity: f.quantity || 1
          })) : []
        };
      })
    });
    const orderId = orderRes.data;

    // 3. 결제 완료 API 호출 
    await axios.post(`/api/orders/${orderId}/pay`, {
      paymentMethod: method,
      pointUsed: basketStore.usedPoints || 0,
      userCouponId: basketStore.usedCouponId || null
    });
    
    // 💡 4. 결제가 완전히 성공한 시점에만 장바구니 비우기 (필요한 경우)
    basketStore.clearCart(); 

    // 5. 결제 완료 화면으로 이동
    router.push(`/order-complete?orderId=${orderId}`);

  } catch (error) {
    console.error('결제 처리 중 오류 발생:', error);

    // 💡 1. 백업해둔 장바구니를 프론트에 되살림
    basketStore.cartItems = backupCartItems;

    // 💡 2. 꼬인 백엔드 세션도 프론트 상태에 맞춰 다시 동기화!
    await basketStore.syncBasket();

    let errorMessage = '결제 처리 중 오류가 발생했습니다.';
    if (error.response && error.response.data) {
      if (typeof error.response.data === 'string') {
        errorMessage = error.response.data;
      } else if (error.response.data.error) {
        errorMessage = error.response.data.error;
      }
    }
    displayAlert(errorMessage);
  }
};

/*
 * Toss 간편결제 흐름
 * 주문 생성 → Toss SDK 결제창 요청 → 성공 URL(/toss/success)에서 승인 API 호출
 * 순서로 진행하며, 결제창 호출 전 오류가 나면 장바구니를 복원합니다.
 */
const handleTossPayment = async () => {
  if (basketStore.cartItems.length === 0) {
    displayAlert(t('장바구니가 비어있습니다. 처음부터 다시 시도해주세요.'), () => {
      router.push('/kiosk');
    });
    return;
  }

  // 💡 1. 토스 결제 시도 전 현재 장바구니 내역을 안전하게 백업 (깊은 복사)
  const backupCartItems = JSON.parse(JSON.stringify(basketStore.cartItems));

  try {
    // 2. 토스 띄우기 전 주문 생성 API 호출 (이때 백엔드에서 재고 부족 검증 수행)
    const orderRes = await axios.post('/api/orders', {
      orderType: basketStore.orderType || 'TOGO',
      dryIceCount: basketStore.dryIceCount || 0,
      dryIceMins: basketStore.dryIceMins || 0,
      phoneNumber: basketStore.phoneNumber || null,
      pointUsed: basketStore.usedPoints || 0,
      userCouponId: basketStore.usedCouponId || null,
      kioskId: Number(localStorage.getItem('kioskId')) || 1,
      storeId: Number(localStorage.getItem('storeId')) || 1,
      items: basketStore.cartItems.map(item => {
        const baseItem = {
          productId: item.productId || item.id,
          quantity: item.quantity,
          unitPrice: item.unitPrice
        };

        if (item.categoryId === 1) {
          return {
            ...baseItem,
            flavors: item.flavors || []
          };
        } else {
          return baseItem;
        }
      })
    });
    const orderId = orderRes.data;

    const tossPayments = await loadTossPayments(TOSS_CLIENT_KEY);
    
    let orderName = t('주문 상품');
    if (basketStore.cartItems.length > 0) {
      if (basketStore.cartItems.length === 1) {
        orderName = formatCartItemName(basketStore.cartItems[0].productName);
      } else {
        orderName = t('{product} 외 {count}건', { product: formatCartItemName(basketStore.cartItems[0].productName), count: basketStore.cartItems.length - 1 });
      }
    }
    
    // 토스페이먼츠 결제창 호출 (파라미터 전달)
    await tossPayments.requestPayment('카드', {
      amount: basketStore.totalPrice - (basketStore.usedPoints || 0),
      orderId: `kiosk_order_${orderId}`,
      orderName: orderName,
      customerName: '키오스크고객',
      successUrl: window.location.origin + `/toss/success?pointUsed=${basketStore.usedPoints || 0}`,
      failUrl: window.location.origin + '/toss/fail',
    });

  } catch (error) {
    console.error('토스 결제창 호출 오류:', error);

    // 💡 3. 토스 결제 실패(재고 부족 등) 시 백업해둔 장바구니를 강제로 되살림!
    basketStore.cartItems = backupCartItems;

    let errorMessage = '토스 결제창을 열 수 없습니다.';
    
    if (error.response && error.response.data) {
      if (typeof error.response.data === 'string') {
        errorMessage = error.response.data;
      } else if (error.response.data.error) {
        errorMessage = error.response.data.error;
      }
    }

    displayAlert(errorMessage);
  }
};
</script>

<style scoped>
.order-confirm-container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background-color: #f4f4f4;
  padding: 30px;
  font-family: 'Pretendard', sans-serif;
  box-sizing: border-box;
}

.header-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background-color: #fff;
  padding: 20px 30px;
  border-radius: 15px;
  box-shadow: 0 4px 6px rgba(0,0,0,0.05);
  margin-bottom: 20px;
}

.header-section h2 {
  margin: 0;
  color: #e91e63;
  font-size: 1.5rem;
}

.sub-title {
  color: #666;
  font-size: 1.1rem;
  font-weight: normal;
}

.btn-back-cart {
  padding: 12px 25px;
  background-color: #fff;
  color: #e91e63;
  border: 2px solid #e91e63;
  border-radius: 30px;
  cursor: pointer;
  font-weight: bold;
  font-size: 1.1rem;
  transition: all 0.2s;
}

.btn-back-cart:hover {
  background-color: #e91e63;
  color: white;
}

.order-list {
  flex: 1;
  background-color: white;
  border-radius: 15px;
  padding: 20px 30px;
  overflow-y: auto;
  box-shadow: 0 4px 6px rgba(0,0,0,0.05);
  margin-bottom: 20px;
}

.order-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 0;
  border-bottom: 1px solid #eee;
}

.order-item:last-child {
  border-bottom: none;
}

.item-info h4 {
  margin: 0 0 10px 0;
  font-size: 1.3rem;
  color: #333;
}

.flavor-list {
  font-size: 1rem;
  color: #888;
}

.item-actions span {
  font-size: 1.2rem;
  font-weight: bold;
  color: #e91e63;
}

.payment-section {
  background-color: white;
  border-radius: 15px;
  padding: 30px;
  box-shadow: 0 4px 15px rgba(0,0,0,0.1);
  text-align: center;
}

.payment-section h3 {
  margin: 0 0 25px 0;
  color: #333;
  font-size: 1.4rem;
}

.pay-buttons {
  display: flex;
  gap: 20px;
  justify-content: center;
}

.pay-buttons button {
  flex: 1;
  padding: 20px;
  font-size: 1.3rem;
  font-weight: bold;
  border-radius: 15px;
  cursor: pointer;
  transition: all 0.2s;
  border: none;
}

.pay-buttons button:nth-child(1) {
  background-color: #f1f3f5;
  color: #495057;
  border: 1px solid #dee2e6;
}

.pay-buttons button:nth-child(2) {
  background-color: #333;
  color: white;
}

.btn-toss {
  background-color: #3182f6 !important;
  color: white !important;
}

.pay-buttons button:hover {
  transform: translateY(-3px);
  box-shadow: 0 5px 15px rgba(0,0,0,0.1);
}

/* Alert Modal */
.alert-modal {
  position: fixed; top: 0; left: 0; width: 100vw; height: 100vh;
  background: rgba(0,0,0,0.5); display: flex; justify-content: center; align-items: center; z-index: 2000;
}
.alert-content {
  background: white; padding: 30px; border-radius: 15px; text-align: center; width: 350px;
  box-shadow: 0 10px 30px rgba(0,0,0,0.2);
}
.alert-content p {
  font-size: 1.2rem; margin-bottom: 25px; color: #333; line-height: 1.5;
}
.alert-content button {
  background-color: #e91e63; color: white; border: none; padding: 12px 30px; border-radius: 30px; font-size: 1.1rem; cursor: pointer; font-weight: bold;
}
</style>
