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
