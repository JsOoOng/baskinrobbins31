/**
 * [모듈 흐름 안내] headProductApi
 * 역할: 상품·메뉴 화면에서 사용하는 HTTP 요청을 한곳에 모은 API 모듈이다.
 * 호출 흐름: Vue 화면 -> 이 모듈 -> Axios/Vite proxy -> Spring Controller (/head/products, /head/products/${productId}, /head/products/${productId}/display)
 * 데이터 기준: 실제 요청 URL과 현재 백엔드 DTO 필드를 우선한다.
 */
import api from '../axios'

/*
 * 본사 상품 목록 조회
 *
 * GET /head/products
 */
export const getHeadProducts = async () => {
  const response = await api.get(
    '/head/products'
  )

  return response.data
}

/*
 * 본사 상품 상세 조회
 *
 * GET /head/products/{productId}
 */
export const getHeadProductDetail = async (
  productId
) => {
  const response = await api.get(
    `/head/products/${productId}`
  )

  return response.data
}

/*
 * 본사 상품 등록
 *
 * POST /head/products
 */
export const createHeadProduct = async (
  productForm
) => {
  const response = await api.post(
    '/head/products',
    productForm
  )

  return response.data
}

/*
 * 본사 상품 수정
 *
 * PUT /head/products/{productId}
 */
export const updateHeadProduct = async (
  productId,
  productForm
) => {
  const response = await api.put(
    `/head/products/${productId}`,
    productForm
  )

  return response.data
}

/*
 * 고객 화면 노출 상태 변경
 *
 * PATCH /head/products/{productId}/display
 */
export const updateHeadProductDisplay = async (
  productId,
  isDisplay
) => {
  const response = await api.patch(
    `/head/products/${productId}/display`,
    {
      isDisplay
    }
  )

  return response.data
}

/*
 * 공통 응답 객체에서 실제 데이터 추출
 *
 * 다음 두 응답을 모두 처리합니다.
 *
 * 1. { success, message, data }
 * 2. DTO 또는 배열 직접 반환
 */
export const extractProductData = (
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
export const extractProductErrorMessage = (
  error,
  defaultMessage =
    '상품 처리 중 오류가 발생했습니다.'
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