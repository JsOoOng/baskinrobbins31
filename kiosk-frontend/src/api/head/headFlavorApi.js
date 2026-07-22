import api from '../axios'

/*
 * 활성화된 맛 목록 조회
 *
 * GET /head/flavors
 */
export const getHeadFlavors = async () => {
  const response = await api.get(
    '/head/flavors'
  )

  return response.data
}

/*
 * 공통 응답 데이터 추출
 */
export const extractFlavorData = (
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