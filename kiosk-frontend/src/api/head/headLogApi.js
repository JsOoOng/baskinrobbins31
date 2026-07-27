/**
 * [모듈 흐름 안내] headLogApi
 * 역할: 본사 관리자 작업 로그 화면에서 사용하는 HTTP 요청을 한곳에 모은 API 모듈이다.
 * 호출 흐름: Vue 화면 -> 이 모듈 -> Axios/Vite proxy -> Spring Controller (/head/logs)
 * 데이터 기준: 실제 요청 URL과 현재 백엔드 DTO 필드를 우선한다.
 */
import api from '../axios'

/*
 * 관리자 작업 내역 조회 (최신 100건)
 *
 * GET /head/logs
 */
export const getAdminLogs = async () => {
  const response = await api.get(
    '/head/logs'
  )

  return response.data
}
