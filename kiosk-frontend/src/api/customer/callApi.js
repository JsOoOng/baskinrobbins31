/**
 * [모듈 흐름 안내] callApi
 * 역할: 직원 호출 화면에서 사용하는 HTTP 요청을 한곳에 모은 API 모듈이다.
 * 호출 흐름: Vue 화면 -> 이 모듈 -> Axios/Vite proxy -> Spring Controller (/api/calls)
 * 데이터 기준: 실제 요청 URL과 현재 백엔드 DTO 필드를 우선한다.
 */
import axios from '@/api/axios';

export const callStaff = async (callRequest) => {
  try {
    const response = await axios.post('/api/calls', callRequest);
    return response.data;
  } catch (error) {
    console.error('직원 호출 API 에러:', error);
    throw error;
  }
};
