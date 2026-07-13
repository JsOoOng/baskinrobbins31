import axios from 'axios'

const instance = axios.create({
  /*
   * Spring Boot 서버 주소
   *
   * 개발 환경:
   * Vue        → http://localhost:5173
   * Spring Boot → http://localhost:8888
   */
  baseURL: 'http://localhost:8888',

  /*
   * 서버가 일정 시간 동안 응답하지 않으면 요청 종료
   * 단위: 밀리초
   */
  timeout: 10000,

  // 세션 쿠키(JSESSIONID)를 공유하기 위한 핵심 설정
  // 필요없다면 아래 한 줄은 제거.
  withCredentials: true,

  headers: {
    'Content-Type': 'application/json',
    Accept: 'application/json'
  }
})

/*
 * 요청 인터셉터
 *
 * 모든 Axios 요청이 서버로 전송되기 직전에 실행됩니다.
 * localStorage에 JWT가 있다면 Authorization 헤더에 자동으로 넣습니다.
 */
instance.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')

    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }

    return config
  },

  (error) => {
    return Promise.reject(error)
  }
)

/*
 * 응답 인터셉터
 *
 * 서버 응답의 상태 코드에 따라 공통 처리를 수행합니다.
 */
instance.interceptors.response.use(
  /*
   * 정상 응답
   */
  (response) => {
    return response
  },

  /*
   * 오류 응답
   */
  (error) => {
    const status = error.response?.status
    const currentPath = window.location.pathname

    /*
     * 401 Unauthorized
     *
     * 토큰 없음
     * 토큰 만료
     * 잘못된 토큰
     */
    if (status === 401) {
      clearAuthentication()

      const loginPath =
        currentPath.startsWith('/head')
          ? '/head/login'
          : '/branch/login'

      if (currentPath !== loginPath) {
        window.location.href = loginPath
      }
    }

    /*
     * 403 Forbidden
     *
     * 로그인은 되어 있지만 해당 기능에 대한 권한이 없는 경우
     */
    if (status === 403) {
      if (
        currentPath.startsWith('/head') &&
        currentPath !== '/head/not-authorized'
      ) {
        window.location.href = '/head/not-authorized'
      }
    }

    return Promise.reject(error)
  }
)

/*
 * 공통 로그인 정보 삭제
 *
 * 본사 관련 저장 값과 기존 공통 저장 값을 모두 제거합니다.
 */
const clearAuthentication = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('headUser')
}

export default instance