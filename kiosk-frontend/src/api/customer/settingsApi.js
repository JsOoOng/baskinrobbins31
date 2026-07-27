/**
 * 고객 화면에서 필요한 본사 공통 설정을 가져오는 API 모듈이다.
 * 예: 결제 전에 주문 취소를 허용할지 여부를 키오스크 화면에서 확인할 때 사용한다.
 */
import axios from '../axios';

// 서버 응답에서 실제 설정 데이터만 꺼내 호출한 화면에 전달한다.
export const getCustomerSettings = async () => {
  const response = await axios.get('/api/customer/settings');
  return response.data;
};
