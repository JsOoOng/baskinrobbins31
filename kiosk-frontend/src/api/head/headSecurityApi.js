/**
 * [모듈 흐름 안내] headSecurityApi
 * 역할: 보안 화면에서 사용하는 HTTP 요청을 한곳에 모은 API 모듈이다.
 * 호출 흐름: Vue 화면 -> 이 모듈 -> Axios/Vite proxy -> Spring Controller (/head/admins, /head/admins/${adminId}, /head/admins/${adminId}/status, /head/admins/${adminId}/password)
 * 데이터 기준: 실제 요청 URL과 현재 백엔드 DTO 필드를 우선한다.
 */
import api from '../axios'

/*
 * 본사 관리자 목록 조회
 *
 * GET /head/admins
 */
export const getHeadAdmins = async () => {
  const response = await api.get(
    '/head/admins'
  )

  return response.data
}

/*
 * 본사 관리자 상세 조회
 *
 * GET /head/admins/{adminId}
 */
export const getHeadAdminDetail = async (
  adminId
) => {
  const response = await api.get(
    `/head/admins/${adminId}`
  )

  return response.data
}

/*
 * 본사 관리자 등록
 *
 * POST /head/admins
 */
export const createHeadAdmin = async (
  requestData
) => {
  const response = await api.post(
    '/head/admins',
    requestData
  )

  return response.data
}

/*
 * 본사 관리자 정보 수정
 *
 * PUT /head/admins/{adminId}
 */
export const updateHeadAdmin = async (
  adminId,
  requestData
) => {
  const response = await api.put(
    `/head/admins/${adminId}`,
    requestData
  )

  return response.data
}

/*
 * 본사 관리자 상태 변경
 *
 * PATCH /head/admins/{adminId}/status
 */
export const updateHeadAdminStatus = async (
  adminId,
  status
) => {
  const response = await api.patch(
    `/head/admins/${adminId}/status`,
    {
      status
    }
  )

  return response.data
}

/*
 * 본사 관리자 비밀번호 초기화
 *
 * PATCH /head/admins/{adminId}/password
 */
export const resetHeadAdminPassword = async (
  adminId,
  newPassword
) => {
  const response = await api.patch(
    `/head/admins/${adminId}/password`,
    {
      newPassword
    }
  )

  return response.data
}

/*
 * HeadApiResponse에서 data 추출
 */
export const extractSecurityData = (
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
 * 백엔드 오류 메시지 추출
 */
export const extractSecurityErrorMessage = (
  error,
  defaultMessage =
    '관리자 계정 처리 중 오류가 발생했습니다.'
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