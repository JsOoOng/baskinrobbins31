/**
 * [모듈 흐름 안내] headPolicyApi
 * 역할: 운영 정책 화면에서 사용하는 HTTP 요청을 한곳에 모은 API 모듈이다.
 * 호출 흐름: Vue 화면 -> 이 모듈 -> Axios/Vite proxy -> Spring Controller (/head/policies, /head/policies/${policyId}, /head/policies/${policyId}/active)
 * 데이터 기준: 실제 요청 URL과 현재 백엔드 DTO 필드를 우선한다.
 */
import axios from '../axios';

export const headPolicyApi = {
  // 전체 방침 조회 (옵션: type)
  getPolicies: async (type = '') => {
    const response = await axios.get('/head/policies', {
      params: type ? { type } : {}
    });
    return response.data;
  },

  // 특정 방침 조회
  getPolicy: async (policyId) => {
    const response = await axios.get(`/head/policies/${policyId}`);
    return response.data;
  },

  // 방침 생성
  createPolicy: async (policyData) => {
    const response = await axios.post('/head/policies', policyData);
    return response.data;
  },

  // 방침 수정
  updatePolicy: async (policyId, policyData) => {
    const response = await axios.put(`/head/policies/${policyId}`, policyData);
    return response.data;
  },

  // 방침 삭제
  deletePolicy: async (policyId) => {
    const response = await axios.delete(`/head/policies/${policyId}`);
    return response.data;
  },

  // 방침 활성화 상태 토글
  updateActiveStatus: async (policyId, isActive) => {
    const response = await axios.patch(`/head/policies/${policyId}/active`, { isActive });
    return response.data;
  }
};
