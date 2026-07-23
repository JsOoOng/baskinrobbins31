/**
 * [모듈 흐름 안내] headPolicyStore
 * 역할: 운영 정책 화면들이 공유하는 Pinia 상태와 동작을 관리한다.
 * 호출 흐름: Vue 화면 -> 이 store의 state/action -> API 또는 localStorage -> 반응형 화면 갱신
 * 데이터 기준: 실제 요청 URL과 현재 백엔드 DTO 필드를 우선한다.
 */
import { defineStore } from 'pinia';
import { headPolicyApi } from '@/api/headquarter/headPolicyApi';

export const useHeadPolicyStore = defineStore('headPolicy', {
  state: () => ({
    policies: [],
    isLoading: false,
    error: null,
  }),

  getters: {
    termsOfService: (state) => state.policies.filter(p => p.type === 'TERMS_OF_SERVICE'),
    privacyPolicies: (state) => state.policies.filter(p => p.type === 'PRIVACY_POLICY')
  },

  actions: {
    async fetchPolicies() {
      this.isLoading = true;
      this.error = null;
      try {
        const data = await headPolicyApi.getPolicies();
        this.policies = data;
      } catch (error) {
        this.error = '약관 및 방침 목록을 불러오는데 실패했습니다.';
        console.error(error);
      } finally {
        this.isLoading = false;
      }
    },

    async createPolicy(policyData) {
      this.isLoading = true;
      this.error = null;
      try {
        const newPolicy = await headPolicyApi.createPolicy(policyData);
        // 전체 리프레시 (활성 상태 자동 변경이 서버에서 일어났으므로)
        await this.fetchPolicies();
        return newPolicy;
      } catch (error) {
        this.error = '약관 생성에 실패했습니다.';
        console.error(error);
        throw error;
      } finally {
        this.isLoading = false;
      }
    },

    async updatePolicy(policyId, policyData) {
      this.isLoading = true;
      this.error = null;
      try {
        await headPolicyApi.updatePolicy(policyId, policyData);
        // 서버에서 isActive 로직 처리가 되었으므로 전체 리프레시
        await this.fetchPolicies();
      } catch (error) {
        this.error = '약관 수정에 실패했습니다.';
        console.error(error);
        throw error;
      } finally {
        this.isLoading = false;
      }
    },

    async deletePolicy(policyId) {
      this.isLoading = true;
      this.error = null;
      try {
        await headPolicyApi.deletePolicy(policyId);
        this.policies = this.policies.filter(p => p.policyId !== policyId);
      } catch (error) {
        this.error = '약관 삭제에 실패했습니다.';
        console.error(error);
        throw error;
      } finally {
        this.isLoading = false;
      }
    },

    async updateActiveStatus(policyId, isActive) {
      try {
        await headPolicyApi.updateActiveStatus(policyId, isActive);
        // 단일화 로직으로 인해 여러 항목이 변경될 수 있으므로 전체 다시 조회
        await this.fetchPolicies();
      } catch (error) {
        console.error('활성화 상태 수정 실패:', error);
        throw error;
      }
    }
  }
});
