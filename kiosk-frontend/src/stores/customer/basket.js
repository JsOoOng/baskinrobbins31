// src/stores/customer/basket.js
import { defineStore } from 'pinia';
import axios from '@/api/axios'; // 방금 만든 axios 가져오기

export const useBasketStore = defineStore('basket', {
  // 1. 상태 (State): 장바구니 데이터 배열 및 주문 유형
  state: () => ({
    cartItems: [],
    orderType: 'TOGO',
    dryIceCount: 0,
    dryIceMins: 0,
    phoneNumber: '',
    usedPoints: 0,
    pointMode: ''
  }),

  getters: {
    // 🌟 장바구니 뱃지에 띄울 총 수량 계산
    totalCount: (state) => {
      if (!state.cartItems) return 0;
      return state.cartItems.reduce((sum, item) => sum + (item.quantity || 1), 0);
    },
    // 🌟 장바구니 총 결제 금액 계산
    totalPrice: (state) => {
      if (!state.cartItems) return 0;
      return state.cartItems.reduce((sum, item) => sum + ((item.unitPrice || 0) * (item.quantity || 1)), 0);
    }
  },

  actions: {
    setOrderType(type) {
      this.orderType = type;
    },
    setDryIceCount(count) {
      this.dryIceCount = count;
    },
    setDryIceMins(mins) {
      this.dryIceMins = mins;
    },
    setPhoneNumber(phone) {
      this.phoneNumber = phone;
    },
    setUsedPoints(points) {
      this.usedPoints = points;
    },
    setPointMode(mode) {
      this.pointMode = mode;
    },

    // 🔄 서버로부터 장바구니 데이터를 동기화
    async fetchBasket() {
      try {
        const res = await axios.get('/api/customer/basket');
        // 💡 서버가 유효한 아이템 목록을 줄 때만 갱신하고, 빈 값이면 기존 프론트엔드 장바구니를 지우지 않음!
        if (res.data && res.data.items && res.data.items.length > 0) {
          this.cartItems = res.data.items;
        }
      } catch (error) {
        console.error('서버 장바구니 조회 실패:', error);
      }
    },

   // ➕ 장바구니에 아이템 추가 (기존 상품이 안 날아가도록 전체 목록 전송)
   async addToCart(product) {
    // 1. 프론트엔드 메모리(Pinia)에 먼저 추가
    this.cartItems.push({
      productId: product.productId,
      categoryId: product.categoryId,
      productName: product.productName,
      quantity: product.quantity || 1,
      unitPrice: product.unitPrice,
      flavors: product.flavors || [], 
      options: product.options || [],  
      extraSpoons: product.extraSpoons || false
    });

    // 2. 백엔드 세션 서버에 "현재 장바구니 전체 목록"을 통째로 전송하여 덮어쓰기 방지
    try {
      await axios.post('/api/customer/basket', {
        items: this.cartItems.map(item => ({
          productId: item.productId,
          productName: item.productName,
          quantity: item.quantity,
          unitPrice: item.unitPrice,
          flavors: item.flavors || [],
          options: item.options || [],
          extraSpoons: item.extraSpoons || false
        }))
      });
      console.log('서버 장바구니 전체 동기화 완료');
    } catch (error) {
      console.error('서버 장바구니 동기화 실패 (프론트 데이터는 보호됨):', error);
    }
  },

  // 🔄 프론트엔드 장바구니 전체를 백엔드 세션에 강제 동기화하는 함수
  async syncBasket() {
    try {
      // 백엔드가 전체 배열을 받는 API가 없다면 기존 clear 후 하나씩 넣거나,
      // 혹은 백엔드 컨트롤러에 전체 동기화용 API가 있다면 그것을 호출합니다.
      // 가장 확실한 방법은 서버 세션을 비운 뒤 현재 프론트 목록을 다시 전송하는 것입니다.
      await axios.delete('/api/customer/basket');
      for (const item of this.cartItems) {
        await axios.post('/api/customer/basket', {
          productId: item.productId,
          categoryId: item.categoryId,
          productName: item.productName,
          quantity: item.quantity,
          unitPrice: item.unitPrice,
          flavors: item.flavors || [],
          options: item.options || [],
          extraSpoons: item.extraSpoons || false
        });
      }
      console.log('서프/프론트 장바구니 완벽 동기화 완료');
    } catch (error) {
      console.error('장바구니 동기화 실패:', error);
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

    // 🗑️ 장바구니 전체 비우기
    async clearCart() {
      this.cartItems = [];
      try {
        await axios.delete('/api/customer/basket');
      } catch (error) {
        console.error('서버 장바구니 초기화 실패:', error);
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
          items: this.cartItems.map(item => ({
            productId: item.productId,
            quantity: item.quantity,
            unitPrice: item.unitPrice,
            flavors: item.flavors.map(f => ({ flavorId: f.flavorId, quantity: f.quantity })),
            options: item.options 
          }))
        };

        // 서버에 주문 요청
        const res = await axios.post('/api/orders', payload);
        return res.data; 
      } catch (error) {
        console.error('주문 실패:', error);
        throw error;
      }
    }
  }
});