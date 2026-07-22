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
