/**
 * [모듈 흐름 안내] headStoreProductApi
 * 역할: 지점 판매 상품 화면에서 사용하는 HTTP 요청을 한곳에 모은 API 모듈이다.
 * 호출 흐름: Vue 화면 -> 이 모듈 -> Axios/Vite proxy -> Spring Controller (/head/stores/${storeId}/products, /head/stores/${storeId}/products/${storeProductId})
 * 데이터 기준: 실제 요청 URL과 현재 백엔드 DTO 필드를 우선한다.
 */
import api from '../axios'

/*
 * 지점 판매 상품 목록 조회
 */
export const getHeadStoreProducts = async (
  storeId
) => {
  const response = await api.get(
    `/head/stores/${storeId}/products`
  )

  return response.data
}

/*
 * 지점 판매 상품 상세 조회
 */
export const getHeadStoreProductDetail = async (
  storeId,
  storeProductId
) => {
  const response = await api.get(
    `/head/stores/${storeId}/products/${storeProductId}`
  )

  return response.data
}

/*
 * 지점 판매 상품 등록
 */
export const createHeadStoreProduct = async (
  storeId,
  requestData
) => {
  const response = await api.post(
    `/head/stores/${storeId}/products`,
    requestData
  )

  return response.data
}

/*
 * 지점 판매 상품 수정
 */
export const updateHeadStoreProduct = async (
  storeId,
  storeProductId,
  requestData
) => {
  const response = await api.put(
    `/head/stores/${storeId}/products/${storeProductId}`,
    requestData
  )

  return response.data
}

/*
 * 지점 판매 상품 삭제
 */
export const deleteHeadStoreProduct = async (
  storeId,
  storeProductId
) => {
  const response = await api.delete(
    `/head/stores/${storeId}/products/${storeProductId}`
  )

  return response.data
}

/*
 * 공통 응답 데이터 추출
 */
export const extractStoreProductData = (
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
export const extractStoreProductErrorMessage = (
  error,
  defaultMessage =
    '지점 판매 메뉴 처리 중 오류가 발생했습니다.'
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