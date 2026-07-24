/**
 * [모듈 흐름 안내] headEventApi
 * 역할: 이벤트 화면에서 사용하는 HTTP 요청을 한곳에 모은 API 모듈이다.
 * 호출 흐름: Vue 화면 -> 이 모듈 -> Axios/Vite proxy -> Spring Controller (/head/events, /head/events/${eventId}, /head/events/${eventId}/visibility)
 * 데이터 기준: 실제 요청 URL과 현재 백엔드 DTO 필드를 우선한다.
 */
import axios from '../axios';

export const headEventApi = {
  // 이벤트 목록 조회
  getAllEvents: async () => {
    const response = await axios.get('/head/events');
    return response.data;
  },

  // 특정 이벤트 조회
  getEvent: async (eventId) => {
    const response = await axios.get(`/head/events/${eventId}`);
    return response.data;
  },

  // 이벤트 생성
  createEvent: async (eventData) => {
    const response = await axios.post('/head/events', eventData);
    return response.data;
  },

  // 이벤트 수정
  updateEvent: async (eventId, eventData) => {
    const response = await axios.put(`/head/events/${eventId}`, eventData);
    return response.data;
  },

  // 이벤트 삭제
  deleteEvent: async (eventId) => {
    const response = await axios.delete(`/head/events/${eventId}`);
    return response.data;
  },

  // 이벤트 가시성(노출 여부) 수정
  updateVisibility: async (eventId, isVisible) => {
    const response = await axios.patch(`/head/events/${eventId}/visibility`, { isVisible });
    return response.data;
  },

  // 쉬운주석: 이벤트 ID와 새 종료일만 보내 기존 이벤트 기간을 연장한다.
  extendEvent: async (eventId, endDate) => {
    const response = await axios.patch(`/head/events/${eventId}/extend`, { endDate });
    return response.data;
  }
};
