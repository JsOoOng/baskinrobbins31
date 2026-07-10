<template>
  <div class="menu-view-wrapper">
    <div class="menu-container">
      <h1>아이스크림 메뉴판</h1>
      
      <div v-if="menuStore.isLoading">메뉴를 불러오는 중</div>

      <div class="product-grid" v-else>
        <div 
          v-for="product in menuStore.products" 
          :key="product.productId" 
          class="product-card"
          @click="openOptionModal(product)" 
        >
          <h3>{{ product.productName }}</h3>
          <p>{{ product.basePrice }}원</p>
        </div>
      </div>

      <OptionModal 
        :isOpen="isModalOpen" 
        :product="selectedProduct"
        @close="isModalOpen = false"
        @add-to-cart="handleAddToCart"
      />
    </div>

    <CartBottomBar />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useMenuStore } from '@/stores/customer/menu';
import { useBasketStore } from '@/stores/customer/basket'; // 💡 장바구니 데이터 갱신용 스토어 추가
import OptionModal from '@/components/customer/OptionModal.vue';
import CartBottomBar from '@/components/customer/CartBottomBar.vue';

import axios from '@/api/axios';

const menuStore = useMenuStore();
const basketStore = useBasketStore(); // 💡 스토어 인스턴스 생성

const isModalOpen = ref(false);       
const selectedProduct = ref(null);    

onMounted(() => {
  menuStore.fetchMenus();
  basketStore.fetchBasket(); // 💡 메뉴판 화면에 진입하자마자 기존 장바구니 내역(수량/금액)을 백엔드에서 긁어옵니다.
});

const openOptionModal = (product) => {
  selectedProduct.value = product; 
  isModalOpen.value = true;        
};

const handleAddToCart = async (product) => {
  // 🌟 여기서 unitPrice를 확실하게 넣어주자!
  // product 객체를 console.log(product) 해서 실제 가격 키 이름이 뭔지 확인해봐.
  // 아마 basePrice 혹은 price일 거야.
  
  const requestData = {
    productId: product.productId,
    quantity: 1, 
    unitPrice: product.basePrice || product.price || 0, // 💡 basePrice가 없으면 price를 쓰고, 그것도 없으면 0으로!
    flavors: [], 
    options: []  
  };

  console.log('최종 전송 데이터:', requestData); // 확인용

  try {
    await axios.post('/api/customer/basket', requestData);
    await basketStore.fetchBasket();
    
    alert(`${product.productName} 상품이 장바구니에 담겼어!`);
    isModalOpen.value = false; 
  } catch (error) {
    alert('장바구니에 담는데 실패했어.');
  }
};
</script>

<style scoped>
.menu-view-wrapper {
  position: relative;
  height: 100%;
  /* 하단 바가 메뉴 카드를 가리지 않도록 넉넉하게 여백 주기 */
  padding-bottom: 120px; 
}
.menu-container { padding: 20px; }
.product-grid { display: flex; gap: 15px; flex-wrap: wrap; }
.product-card { 
  border: 2px solid #ffcce6; padding: 20px; border-radius: 12px; 
  width: 200px; text-align: center; cursor: pointer;
  transition: transform 0.2s;
}
.product-card:hover { transform: scale(1.05); background-color: #fff0f5; }
</style>