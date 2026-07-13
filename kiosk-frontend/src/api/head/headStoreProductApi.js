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