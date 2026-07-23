/**
 * [모듈 흐름 안내] headRestockApi
 * 역할: 재입고·발주 화면에서 사용하는 HTTP 요청을 한곳에 모은 API 모듈이다.
 * 호출 흐름: Vue 화면 -> 이 모듈 -> Axios/Vite proxy -> Spring Controller (/head/restocks, /head/restocks/waiting, /head/restocks/${requestId}, /head/restocks/${requestId}/approve, /head/restocks/${requestId}/shipping, /head/restocks/${requestId}/complete 등)
 * 데이터 기준: 실제 요청 URL과 현재 백엔드 DTO 필드를 우선한다.
 */
import api from '../axios'

/*
 * 발주 요청 전체 목록 조회
 * GET /head/restocks
 */
export const getHeadRestocks = async () => {
  const response = await api.get('/head/restocks')
  return response.data
}

/*
 * 승인 대기 발주 요청 목록 조회
 * GET /head/restocks/waiting
 */
export const getWaitingHeadRestocks = async () => {
  const response = await api.get('/head/restocks/waiting')
  return response.data
}

/*
 * 발주 요청 상세 조회
 * GET /head/restocks/{requestId}
 */
export const getHeadRestockDetail = async (requestId) => {
  const response = await api.get(`/head/restocks/${requestId}`)
  return response.data
}

/*
 * 발주 승인
 * PUT /head/restocks/{requestId}/approve
 */
export const approveHeadRestock = async (requestId, requestDTO = {}) => {
  const response = await api.put(`/head/restocks/${requestId}/approve`, requestDTO)
  return response.data
}

/*
 * 배송 처리
 * PUT /head/restocks/{requestId}/shipping
 */
export const startHeadRestockShipping = async (requestId, requestDTO = {}) => {
  const response = await api.put(`/head/restocks/${requestId}/shipping`, requestDTO)
  return response.data
}

/*
 * 완료 처리
 * PUT /head/restocks/{requestId}/complete
 */
export const completeHeadRestock = async (requestId, requestDTO = {}) => {
  const response = await api.put(`/head/restocks/${requestId}/complete`, requestDTO)
  return response.data
}

/*
 * 반려 처리
 * PUT /head/restocks/{requestId}/reject
 */
export const rejectHeadRestock = async (requestId, requestDTO = {}) => {
  const response = await api.put(`/head/restocks/${requestId}/reject`, requestDTO)
  return response.data
}

/*
 * 재고 신청 API 오류 메시지 추출
 */
export const extractRestockErrorMessage = (
  error,
  defaultMessage = '재고 신청 요청 처리 중 오류가 발생했습니다.'
) => {
  return (
    error?.response?.data?.message ||
    error?.response?.data?.error ||
    (
      typeof error?.response?.data === 'string'
        ? error.response.data
        : null
    ) ||
    error?.message ||
    defaultMessage
  )
}
