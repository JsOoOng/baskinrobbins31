/**
 * [모듈 흐름 안내] headInventoryAlertApi
 * 역할: 재고 화면에서 사용하는 HTTP 요청을 한곳에 모은 API 모듈이다.
 * 호출 흐름: Vue 화면 -> 이 모듈 -> Axios/Vite proxy -> Spring Controller (/head/inventory-shortage-alerts, /head/inventory-shortage-alerts/${alertId}/send)
 * 데이터 기준: 실제 요청 URL과 현재 백엔드 DTO 필드를 우선한다.
 */
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
