// src/stores/basket.js
import { defineStore } from 'pinia';
import axios from '@/api/axios'; // 방금 만든 axios 가져오기

export const useBasketStore = defineStore('basket', {
  // 1. 상태 (State): 장바구니 데이터 배열 및 주문 유형
  state: () => ({
    cartItems: [],
    orderType: 'TOGO',
    dryIceCount: 0
  }),

  // 2. 계산된 상태 (Getters): 총 금액 및 총 수량 계산
  getters: {
    totalPrice: (state) => {
      return state.cartItems.reduce((sum, item) => sum + (item.unitPrice * item.quantity), 0);
    },
    totalQuantity: (state) => {
      return state.cartItems.length;
    }
  },

  // 3. 기능 (Actions): 장바구니 조작 및 서버 통신
  actions: {
    setOrderType(type) {
      this.orderType = type;
    },
    setDryIceCount(count) {
      this.dryIceCount = count;
    },

    // 🔄 서버로부터 장바구니 데이터를 동기화
    async fetchBasket() {
      try {
        const res = await axios.get('/api/customer/basket');
        if (res.data && res.data.items) {
          this.cartItems = res.data.items;
        } else {
          this.cartItems = [];
        }
      } catch (error) {
        console.error('서버 장바구니 조회 실패:', error);
      }
    },

    // ➕ 장바구니에 아이템 추가
    async addToCart(product) {
      // 프론트엔드 메모리(Pinia)에 먼저 추가
      this.cartItems.push({
        productId: product.productId,
        quantity: product.quantity || 1,
        unitPrice: product.unitPrice,
        flavors: product.flavors || [], // [{ flavorId: 1, quantity: 1 }] 모양
        options: product.options || [],  // [101, 201] 모양 (옵션 ID 숫자 배열)
        extraSpoons: product.extraSpoons || false
      });

      // 백엔드 세션 서버에 장바구니 저장 요청
      try {
        await axios.post('/api/customer/basket', {
          productId: product.productId,
          productName: product.productName,
          quantity: product.quantity || 1,
          unitPrice: product.unitPrice,
          flavors: product.flavors || [],
          options: product.options || [],
          extraSpoons: product.extraSpoons || false
        });
        console.log('서버 장바구니 동기화 완료');
      } catch (error) {
        console.error('서버 장바구니 저장 실패:', error);
      }
    },

    // ❌ 장바구니 특정 인덱스 아이템 삭제
    async removeFromCart(index) {
      this.cartItems.splice(index, 1);
      try {
        await axios.delete(`/api/customer/basket/${index}`);
      } catch (error) {
        console.error('서버 장바구니 삭제 실패:', error);
      }
    },

    // 🔄 장바구니 특정 아이템 수량 변경
    async updateQuantity(index, quantity) {
      if (quantity < 1) return;
      this.cartItems[index].quantity = quantity;
      try {
        await axios.put(`/api/customer/basket/${index}?quantity=${quantity}`);
      } catch (error) {
        console.error('서버 장바구니 수량 변경 실패:', error);
      }
    },

    // 🔄 장바구니 전체 비우기
    async clearCart() {
      this.cartItems = [];
      try {
        await axios.delete('/api/customer/basket');
        console.log('서버 장바구니 비우기 완료');
      } catch (error) {
        console.error('서버 장바구니 초기화 실패:', error);
      }
    }
  }
});