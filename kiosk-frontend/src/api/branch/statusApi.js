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