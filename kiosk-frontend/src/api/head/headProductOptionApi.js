/**
 * [모듈 흐름 안내] headProductOptionApi
 * 역할: 상품 옵션 화면에서 사용하는 HTTP 요청을 한곳에 모은 API 모듈이다.
 * 호출 흐름: Vue 화면 -> 이 모듈 -> Axios/Vite proxy -> Spring Controller (/head/products/${productId}/options, /head/products/${productId}/options/${optionId})
 * 데이터 기준: 실제 요청 URL과 현재 백엔드 DTO 필드를 우선한다.
 */
import api from '../axios'

/*
 * 상품 옵션 목록 조회
 */
export const getHeadProductOptions = async (
  productId
) => {
  const response = await api.get(
    `/head/products/${productId}/options`
  )

  return response.data
}

/*
 * 상품 옵션 등록
 */
export const createHeadProductOption = async (
  productId,
  optionForm
) => {
  const response = await api.post(
    `/head/products/${productId}/options`,
    optionForm
  )

  return response.data
}

/*
 * 상품 옵션 수정
 */
export const updateHeadProductOption = async (
  productId,
  optionId,
  optionForm
) => {
  const response = await api.put(
    `/head/products/${productId}/options/${optionId}`,
    optionForm
  )

  return response.data
}

/*
 * 상품 옵션 삭제
 */
export const deleteHeadProductOption = async (
  productId,
  optionId
) => {
  const response = await api.delete(
    `/head/products/${productId}/options/${optionId}`
  )

  return response.data
}

/*
 * HeadApiResponse 또는 일반 응답 처리
 */
export const extractProductOptionData = (
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
export const extractProductOptionErrorMessage = (
  error,
  defaultMessage =
    '상품 옵션 처리 중 오류가 발생했습니다.'
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