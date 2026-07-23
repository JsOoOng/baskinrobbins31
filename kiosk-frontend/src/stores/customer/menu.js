/**
 * [모듈 흐름 안내] menu
 * 역할: 고객 메뉴 화면들이 공유하는 Pinia 상태와 동작을 관리한다.
 * 호출 흐름: Vue 화면 -> 이 store의 state/action -> API 또는 localStorage -> 반응형 화면 갱신
 * 데이터 기준: 실제 요청 URL과 현재 백엔드 DTO 필드를 우선한다.
 */
import { defineStore } from 'pinia';
import axios from '@/api/axios'; // axios 다리 가져오기

export const useMenuStore = defineStore('menu', {
  state: () => ({
    products: [],     // 아이스크림, 음료 등 메뉴 목록
    categories: [],   // 카테고리 목록
    isLoading: false, // 로딩 상태 (스피너용)
  }),

  actions: {
    // 백엔드에서 메뉴 목록을 가져오는 함수
    async fetchMenus() {
      this.isLoading = true;
      try {
        // 백엔드의 GET /api/customer/menus (예시) 주소로 요청.
        const response = await axios.get('/api/customer/menus');
        
        // 성공하면 가져온 데이터를 내 메모리에 저장
        this.products = response.data;
        console.log('메뉴 데이터 가져오기 성공:', this.products);
      } catch (error) {
        console.error('메뉴를 불러오는데 실패:', error);
      } finally {
        this.isLoading = false;
      }
    }
  }
});