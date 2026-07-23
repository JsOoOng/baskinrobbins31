/**
 * [모듈 흐름 안내] statusApi
 * 역할: 재입고 요청·재고 부족 알림 화면에서 사용하는 HTTP 요청을 한곳에 모은 API 모듈이다.
 * 호출 흐름: Vue 화면 -> 이 모듈 -> Axios/Vite proxy -> Spring Controller (/branch/status/restock, /branch/stores/${storeId})
 * 데이터 기준: 실제 요청 URL과 현재 백엔드 DTO 필드를 우선한다.
 */
import api from '@/api/axios'


/*
 * 발주 신청
 */
export const requestRestock = (data) => {

  return api.post(
    '/branch/status/restock',
    data
  )
}


/*
 * 지점의 미확인 재고 부족 알림 조회
 */
export const getUnconfirmedInventoryShortages = (
  storeId
) => {

  return api.get(
    `/branch/stores/${storeId}`
    + '/inventory-shortage-alerts/unconfirmed'
  )
}


/*
 * 지점의 재고 부족 알림 확인
 */
export const confirmInventoryShortages = (
  storeId
) => {

  return api.patch(
    `/branch/stores/${storeId}`
    + '/inventory-shortage-alerts/confirm'
  )
}