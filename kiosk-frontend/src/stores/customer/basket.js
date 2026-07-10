// src/stores/customer/basket.js
import { defineStore } from 'pinia';
import axios from 'axios'; // 전역 설정된 axios 인스턴스를 가져오는 것이 좋음

// 💡 세션(쿠키) 유지를 위한 필수 설정! (전역 설정 파일에 있다면 생략 가능)
axios.defaults.withCredentials = true; 

export const useBasketStore = defineStore('basket', {
  state: () => ({
    items: [], 
    totalPrice: 0,
  }),

  getters: {
    // 🌟 장바구니 뱃지에 띄울 총 수량 계산
    totalCount: (state) => {
      if (!state.items) return 0;
      return state.items.reduce((sum, item) => sum + item.quantity, 0);
    }
  },

  actions: {
    // 1. 장바구니 조회 (GET)
    async fetchBasket() {
      try {
        const res = await axios.get('http://localhost:8888/api/customer/basket');
        this.items = res.data.items || [];
        this.totalPrice = res.data.totalPrice || 0;
      } catch (error) {
        console.error('장바구니 조회 실패:', error);
      }
    },

    // 2. 장바구니 전체 비우기 (DELETE) - 헤더 X 버튼용
    async clearBasket() {
      try {
        await axios.delete('http://localhost:8888/api/customer/basket');
        this.items = [];
        this.totalPrice = 0;
      } catch (error) {
        console.error('장바구니 비우기 실패:', error);
      }
    },

    // 3. 결제 최종 주문 API (POST)
    async submitOrder(orderInfo) {
      try {
        const payload = {
          storeId: orderInfo.storeId,
          kioskId: orderInfo.kioskId,
          userId: orderInfo.userId, 
          orderType: orderInfo.orderType,
          dryIceMins: orderInfo.dryIceMins,
          totalPrice: this.totalPrice,
          items: this.items.map(item => ({
            productId: item.productId,
            quantity: item.quantity,
            unitPrice: item.unitPrice,
            flavors: item.flavors.map(f => ({ flavorId: f.flavorId, quantity: f.quantity })),
            options: item.options 
          }))
        };

        const res = await axios.post('http://localhost:8888/api/orders', payload);
        return res.data; 
      } catch (error) {
        console.error('주문 실패:', error);
        throw error;
      }
    }
  }
});