import api from '@/api/axios'

/*
 * 전체 지점 재고 목록 조회
 *
 * GET /head/inventory
 */
export const getHeadInventories = () => {
  return api.get('/head/inventory')
}

/*
 * 재고 상세 조회
 *
 * GET /head/inventory/{storeInventoryId}
 */
export const getHeadInventory = (
  storeInventoryId
) => {
  return api.get(
    `/head/inventory/${storeInventoryId}`
  )
}

/*
 * 자동 재고 보충 설정 수정
 *
 * PATCH
 * /head/inventory/{storeInventoryId}/restock-setting
 */
export const updateHeadInventoryRestockSetting = (
  storeInventoryId,
  request
) => {
  return api.patch(
    `/head/inventory/${storeInventoryId}/restock-setting`,
    request
  )
}