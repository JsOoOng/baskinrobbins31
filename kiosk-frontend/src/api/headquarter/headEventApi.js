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
  }
};
