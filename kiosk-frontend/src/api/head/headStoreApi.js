/**
 * [모듈 흐름 안내] headStoreApi
 * 역할: 지점 화면에서 사용하는 HTTP 요청을 한곳에 모은 API 모듈이다.
 * 호출 흐름: Vue 화면 -> 이 모듈 -> Axios/Vite proxy -> Spring Controller (/head/stores, /head/stores/${storeId}, /head/stores/${storeId}/employees)
 * 데이터 기준: 실제 요청 URL과 현재 백엔드 DTO 필드를 우선한다.
 */
import api from '../axios'

/*
 * 지점 목록 조회
 *
 * GET /head/stores
 */
export const getHeadStores = async () => {
  const response = await api.get(
    '/head/stores'
  )

  return response.data
}

/*
 * 지점 상세 조회
 *
 * GET /head/stores/{storeId}
 */
export const getHeadStoreDetail = async (
  storeId
) => {
  const response = await api.get(
    `/head/stores/${storeId}`
  )

  return response.data
}

/*
 * 지점 등록
 *
 * POST /head/stores
 */
export const createHeadStore = async (
  storeForm
) => {
  const response = await api.post(
    '/head/stores',
    storeForm
  )

  return response.data
}

/*
 * 지점 수정
 *
 * PUT /head/stores/{storeId}
 */
export const updateHeadStore = async (
  storeId,
  storeForm
) => {
  const response = await api.put(
    `/head/stores/${storeId}`,
    storeForm
  )

  return response.data
}

/*
 * 지점 관리자 계정 생성
 *
 * POST /head/stores/{storeId}/employees
 */
export const createHeadStoreEmployee = async (
  storeId,
  employeeForm
) => {
  const response = await api.post(
    `/head/stores/${storeId}/employees`,
    employeeForm
  )

  return response.data
}

/*
 * 공통 응답 데이터 추출
 *
 * 다음 응답을 모두 지원합니다.
 *
 * { success, message, data }
 * 배열 직접 반환
 * DTO 직접 반환
 */
export const extractStoreData = (
  responseBody
) => {
  if (
    responseBody !== null &&
    typeof responseBody === 'object' &&
    Object.prototype.hasOwnProperty.call(
      responseBody,
      'data'
    )
  ) {
    return responseBody.data
  }

  return responseBody
}

/*
 * 오류 메시지 추출
 */
export const extractStoreErrorMessage = (
  error,
  defaultMessage =
    '지점 처리 중 오류가 발생했습니다.'
) => {
  const responseData =
    error?.response?.data

  if (typeof responseData === 'string') {
    return responseData
  }

  return (
    responseData?.message ||
    responseData?.error ||
    error?.message ||
    defaultMessage
  )
}