import api from '../axios'

/*
 * 본사 관리자 목록 조회
 */
export const getHeadAdmins = async () => {
  const response = await api.get(
    '/head/admins'
  )

  return response.data
}

/*
 * 본사 관리자 상세 조회
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
 * 본사 관리자 계정 생성
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
 * 관리자 비밀번호 초기화
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
 * HeadApiResponse의 data 추출
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