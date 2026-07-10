<template>
  <div class="menu-container">
    <h1>아이스크림 메뉴판</h1>
    
    <div v-if="menuStore.isLoading">메뉴를 불러오는 중이야... 🍦</div>

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
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useMenuStore } from '@/stores/customer/menu';
import OptionModal from '@/components/customer/OptionModal.vue'; // 모달 부품 가져오기!

const menuStore = useMenuStore();

// 💡 모달창 상태를 관리하는 변수들
const isModalOpen = ref(false);       // 모달이 열려있는지 여부 (처음엔 닫힘)
const selectedProduct = ref(null);    // 내가 클릭한 아이스크림 정보 보관

onMounted(() => {
  menuStore.fetchMenus();
});

// 상품을 클릭했을 때 실행되는 함수
const openOptionModal = (product) => {
  selectedProduct.value = product; // 클릭한 아이스크림 정보를 변수에 담고
  isModalOpen.value = true;        // 모달창을 엽니다!
};

// 모달에서 '장바구니 담기'를 눌렀을 때 실행되는 함수
const handleAddToCart = (product) => {
  alert(`${product.productName} 상품이 장바구니에 담겼습니다. (콘솔창 확인)`);
  console.log('장바구니에 담을 데이터:', product);
  // 나중에 여기에 우리가 처음에 만든 basketStore.addToCart()를 연결할 거예요!
};
</script>

<style scoped>
.menu-container { padding: 20px; }
.product-grid { display: flex; gap: 15px; flex-wrap: wrap; }
.product-card { 
  border: 2px solid #ffcce6; padding: 20px; border-radius: 12px; 
  width: 200px; text-align: center; cursor: pointer;
  transition: transform 0.2s;
}
.product-card:hover { transform: scale(1.05); background-color: #fff0f5; }
</style>