<template>
  <div class="modal-overlay" v-if="isOpen" @click.self="closeModal">
    <div class="modal-content">
      <button class="close-icon-btn" @click="closeModal">✖</button>
      <h2>{{ product?.productName }}</h2>
      <p class="price">{{ product?.basePrice }}원</p>

      <div class="flavor-section">
        <h3>아이스크림 맛을 선택해주세요 🍦</h3>
        <div class="flavor-list">
          <label><input type="checkbox" value="1" v-model="selectedFlavors"> 엄마는 외계인</label><br>
          <label><input type="checkbox" value="2" v-model="selectedFlavors"> 민트초코</label><br>
          <label><input type="checkbox" value="3" v-model="selectedFlavors"> 뉴욕 치즈케이크</label>
        </div>
      </div>

      <div class="modal-actions">
        <button class="cancel-btn" @click="closeModal">취소</button>
        <button class="cart-btn" @click="addToCart">장바구니 담기</button>
      </div>

    </div>
  </div>
</template>

<script setup>
import { defineProps, defineEmits, ref, watch } from 'vue';

const props = defineProps({
  isOpen: Boolean,
  product: Object
});

const emit = defineEmits(['close', 'add-to-cart']);

// 💡 1. 사용자가 선택한 맛의 ID 번호가 담길 빈 배열 만들기
const selectedFlavors = ref([]);

// 💡 2. 모달창이 새로 열릴 때마다 이전에 체크했던 맛 초기화(비우기)
watch(() => props.isOpen, (newVal) => {
  if (newVal === true) {
    selectedFlavors.value = [];
  }
});

const closeModal = () => {
  emit('close');
};

const addToCart = () => {
  // 💡 3. 유효성 검사 (Validation): 선택한 맛이 0개라면?
  if (selectedFlavors.value.length === 0) {
    alert('최소 1개 이상의 아이스크림 맛을 선택해 주세요! 🍦');
    return; // 함수를 여기서 강제로 끝내서, 장바구니에 안 담기게 막음!
  }

  // 통과했다면, 부모에게 상품 정보와 함께 '선택한 맛 배열'도 같이 넘겨줌
  emit('add-to-cart', {
    ...props.product,
    selectedFlavors: selectedFlavors.value
  });
  
  closeModal();
};
</script>

<style scoped>
.modal-overlay {
  position: fixed; top: 0; left: 0; width: 100vw; height: 100vh;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex; justify-content: center; align-items: center;
  z-index: 1000;
}
.modal-content {
  background-color: white; padding: 30px; border-radius: 15px;
  width: 400px; text-align: center;
  box-shadow: 0 4px 20px rgba(0,0,0,0.2);
  position: relative;
}
.close-icon-btn {
  position: absolute;
  top: 15px;
  right: 15px;
  background-color: #ff4d4f;
  color: white;
  border: none;
  border-radius: 50%;
  width: 30px;
  height: 30px;
  font-size: 14px;
  cursor: pointer;
  display: flex;
  justify-content: center;
  align-items: center;
  box-shadow: 0 2px 5px rgba(0,0,0,0.2);
}
.price { font-size: 1.2rem; color: #ff66b2; font-weight: bold; }
.flavor-section { margin: 20px 0; text-align: left; background: #f9f9f9; padding: 15px; border-radius: 8px;}
.modal-actions { display: flex; gap: 10px; justify-content: center; margin-top: 20px; }
button { padding: 10px 20px; border: none; border-radius: 8px; font-size: 1rem; cursor: pointer; }
.cancel-btn { background-color: #ddd; }
.cart-btn { background-color: #004080; color: white; font-weight: bold; }
</style>