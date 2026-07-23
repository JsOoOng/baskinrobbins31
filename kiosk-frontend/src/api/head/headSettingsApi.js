/**
 * [모듈 흐름 안내] headSettingsApi
 * 역할: 환경 설정 화면에서 사용하는 HTTP 요청을 한곳에 모은 API 모듈이다.
 * 호출 흐름: Vue 화면 -> 이 모듈 -> Axios/Vite proxy -> Spring Controller (/head/settings)
 * 데이터 기준: 실제 요청 URL과 현재 백엔드 DTO 필드를 우선한다.
 */
import axios from '../axios';

export const getHeadSettings = async () => {
  const response = await axios.get('/head/settings');
  return response.data;
};

export const updateHeadSettings = async (settingsDto) => {
  const response = await axios.put('/head/settings', settingsDto);
  return response.data;
};
