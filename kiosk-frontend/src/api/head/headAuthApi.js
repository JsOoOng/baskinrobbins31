/**
 * [모듈 흐름 안내] headAuthApi
 * 역할: 본사 인증 화면에서 사용하는 HTTP 요청을 한곳에 모은 API 모듈이다.
 * 호출 흐름: Vue 화면 -> 이 모듈 -> Axios/Vite proxy -> Spring Controller (/head/auth/login, /head/auth/me, /head/auth/logout)
 * 데이터 기준: 실제 요청 URL과 현재 백엔드 DTO 필드를 우선한다.
 */
import api from '../axios'

/*
 * 본사 관리자 로그인
 *
 * POST /head/auth/login
 *
 * 요청:
 * {
 *   loginId: 'admin',
 *   password: '1234'
 * }
 */
export const loginHeadAdmin = async (loginForm) => {
  const response = await api.post(
    '/head/auth/login',
    {
      loginId: loginForm.loginId,
      password: loginForm.password
    }
  )

  return response.data
}

/*
 * 현재 로그인한 본사 관리자 정보 조회
 *
 * GET /head/auth/me
 *
 * Authorization 헤더는 axios.js의
 * 요청 인터셉터에서 자동으로 추가됩니다.
 */
export const getHeadAdminMe = async () => {
  const response = await api.get(
    '/head/auth/me'
  )

  return response.data
}

/*
 * 본사 관리자 로그아웃 API 호출
 *
 * JWT 방식이므로 실제 토큰 삭제는
 * Pinia Store 또는 화면에서 처리합니다.
 */
export const logoutHeadAdmin = async () => {
  const response = await api.post(
    '/head/auth/logout'
  )

  return response.data
}

/*
 * HeadApiResponse에서 실제 data 부분을 꺼내는 공통 함수
 *
 * 백엔드 응답 예:
 * {
 *   success: true,
 *   message: '본사 관리자 로그인 성공',
 *   data: {
 *     employeeId: 1,
 *     loginId: 'head01',
 *     name: '관리자',
 *     role: 'HEAD_ADMIN',
 *     token: '...'
 *   }
 * }
 */
export const extractHeadApiData = (responseBody) => {
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
 *
 * 다양한 오류 응답 구조를 대비합니다.
 */
export const extractHeadApiErrorMessage = (
  error,
  defaultMessage = '요청 처리 중 오류가 발생했습니다.'
) => {
  return (
    error?.response?.data?.message ||
    error?.response?.data?.error ||
    error?.message ||
    defaultMessage
  )
}