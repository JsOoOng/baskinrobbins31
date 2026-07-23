import api from '@/api/axios'

/*
 * 본사 재고 부족 활성 알람 목록 조회
 *
 * GET /head/inventory-shortage-alerts
 */
export const getActiveInventoryShortageAlerts = () => {
  return api.get('/head/inventory-shortage-alerts')
}

/*
 * 지점에 재고 부족 알람 전송
 *
 * POST /head/inventory-shortage-alerts/{alertId}/send
 */
export const sendInventoryShortageAlertToStore = (alertId, adminId) => {
  return api.post(`/head/inventory-shortage-alerts/${alertId}/send`, {
    adminId
  })
}
