/**
 * [모듈 흐름 안내] headDashboardApi
 * 역할: 대시보드 화면에서 사용하는 HTTP 요청을 한곳에 모은 API 모듈이다.
 * 호출 흐름: Vue 화면 -> 이 모듈 -> Axios/Vite proxy -> Spring Controller (/head/dashboard/summary)
 * 데이터 기준: 실제 요청 URL과 현재 백엔드 DTO 필드를 우선한다.
 */
import api from '../axios'

/**
 * 대시보드 요약 통계 조회
 * @param {string} comparisonPeriod - 비교 기준 (예: '전일 대비', '전주 대비' 등)
 * @returns {Promise<Object>} 대시보드 통계 데이터
 */
export const getHeadDashboardSummary = async (comparisonPeriod) => {
  const response = await api.get('/head/dashboard/summary', {
    params: { comparisonPeriod }
  })
  
  return response.data
}
