<template>
  <div class="cart-bottom-bar">
    <button class="cart-icon-btn" @click="isModalOpen = true">
      🛍️ <span class="badge">{{ basketStore.totalCount }}</span>
    </button>

    <button class="checkout-btn" @click="goToOrderConfirm">
      ₩{{ basketStore.totalPrice.toLocaleString() }} 결제하기 >
    </button>

    <div v-if="isModalOpen" class="cart-modal">
      <div class="modal-content">
        <h3>장바구니 내역</h3>
        <button @click="isModalOpen = false">닫기 ❌</button>

        <ul>
          <li v-for="(item, index) in basketStore.items" :key="index">
            <p><strong>{{ item.productName || getProductName(item.productId) }}</strong></p>
            
            <div v-if="item.flavors && item.flavors.length > 0" class="flavor-list">
                선택한 맛: 
                <span v-for="(f, fIndex) in item.flavors" :key="fIndex" class="flavor-item">
                    {{ f.flavorName || '맛ID:' + f.flavorId }}
                    {{ f.quantity > 1 ? '(' + f.quantity + ')' : '' }}
                    {{ fIndex < item.flavors.length - 1 ? ', ' : '' }}
                </span>
            </div>
            
            <div>
              <button @click="updateQty(index, item.quantity - 1)">-</button>
              <span>{{ item.quantity }}</span>
              <button @click="updateQty(index, item.quantity + 1)">+</button>
              <button @click="removeItem(index)">🗑️ 삭제</button>
            </div>
          </li>
        </ul>
        
        <button class="modal-checkout-btn" @click="goToOrderConfirm">
          ₩{{ basketStore.totalPrice.toLocaleString() }} 결제하기
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { useBasketStore } from '@/stores/customer/basket';
import { useMenuStore } from '@/stores/customer/menu'; // 🌟 메뉴판 데이터도 가져오기

import axios from '@/api/axios';

const router = useRouter();
const basketStore = useBasketStore();
const menuStore = useMenuStore(); // 🌟 메뉴 스토어 등록
const isModalOpen = ref(false);

// 추가된 함수: productId를 가지고 메뉴판 전체 목록에서 이름을 찾아옴
const getProductName = (productId) => {
  const product = menuStore.products.find(p => p.productId === productId);
  return product ? product.productName : '알 수 없는 상품';
};

const goToOrderConfirm = () => {
  if (basketStore.totalCount === 0) {
    alert('장바구니가 비어있습니다!');
    return;
  }
  isModalOpen.value = false;
  router.push('/order-confirm');
};

const updateQty = async (index, newQty) => { // 인덱스를 인자로 받음
  if (newQty < 1) return; 

  try {
    await axios.patch(`/api/customer/basket/${index}?quantity=${newQty}`);
    await basketStore.fetchBasket();
  } catch (error) {
    alert('수량 변경 실패');
  }
};

//  삭제 함수 업데이트
const removeItem = async (index) => { // 인덱스를 인자로 받음
  if (!confirm('이 메뉴를 장바구니에서 삭제하시겠습니까?')) return;

  try {
    await axios.delete(`/api/customer/basket/${index}`);
    await basketStore.fetchBasket();
  } catch (error) {
    alert('삭제 실패');
  }
};
</script>

<style scoped>
/* 🌟 화면 맨 아래에 고정시키는 핵심 CSS */
.cart-bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  width: 100%;
  height: 90px;
  background-color: white;
  border-top: 2px solid #eaeaea;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 40px;
  box-shadow: 0 -4px 15px rgba(0, 0, 0, 0.05);
  z-index: 1000;
  box-sizing: border-box;
}

/* 장바구니 아이콘 버튼 */
.cart-icon-btn {
  background: white;
  border: 2px solid #ddd;
  border-radius: 50px;
  padding: 10px 30px;
  font-size: 1.5rem;
  position: relative;
  cursor: pointer;
}

/* 뱃지 (담은 개수) */
.badge {
  position: absolute;
  top: -10px;
  right: -10px;
  background-color: #ff007f;
  color: white;
  font-size: 1rem;
  font-weight: bold;
  padding: 5px 10px;
  border-radius: 50%;
}

/* 결제하기 버튼 */
.checkout-btn {
  background-color: #ff007f;
  color: white;
  font-size: 1.5rem;
  font-weight: bold;
  border: none;
  border-radius: 50px;
  padding: 15px 40px;
  cursor: pointer;
}

/* 임시 모달 디자인 */
.cart-modal {
  position: fixed; top: 0; left: 0; width: 100vw; height: 100vh;
  background: rgba(0,0,0,0.5); display: flex; justify-content: center; align-items: center;
}
.modal-content {
  background: white; padding: 30px; border-radius: 10px; width: 400px;
}
</style>