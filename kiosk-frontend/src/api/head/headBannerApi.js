/**
 * [모듈 흐름 안내] headBannerApi
 * 역할: 배너 화면에서 사용하는 HTTP 요청을 한곳에 모은 API 모듈이다.
 * 호출 흐름: Vue 화면 -> 이 모듈 -> Axios/Vite proxy -> Spring Controller (/head/banners, /head/banners/${bannerId}, /head/banners/${bannerId}/active)
 * 데이터 기준: 실제 요청 URL과 현재 백엔드 DTO 필드를 우선한다.
 */
import api from '../axios'

/*
 * 배너 목록 조회
 */
export const getHeadBanners = async () => {
  const response = await api.get(
    '/head/banners'
  )

  return response.data
}

/*
 * 배너 상세 조회
 */
export const getHeadBannerDetail = async (
  bannerId
) => {
  const response = await api.get(
    `/head/banners/${bannerId}`
  )

  return response.data
}

/*
 * 배너 등록
 */
export const createHeadBanner = async (
  requestData
) => {
  const response = await api.post(
    '/head/banners',
    requestData
  )

  return response.data
}

/*
 * 배너 수정
 */
export const updateHeadBanner = async (
  bannerId,
  requestData
) => {
  const response = await api.put(
    `/head/banners/${bannerId}`,
    requestData
  )

  return response.data
}

/*
 * 노출 상태 변경
 */
export const updateHeadBannerActive = async (
  bannerId,
  isActive
) => {
  const response = await api.patch(
    `/head/banners/${bannerId}/active`,
    {
      isActive
    }
  )

  return response.data
}

/*
 * 배너 삭제
 */
export const deleteHeadBanner = async (
  bannerId
) => {
  const response = await api.delete(
    `/head/banners/${bannerId}`
  )

  return response.data
}

/*
 * 공통 응답 데이터 추출
 */
export const extractBannerData = (
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
export const extractBannerErrorMessage = (
  error,
  defaultMessage =
    '배너 처리 중 오류가 발생했습니다.'
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