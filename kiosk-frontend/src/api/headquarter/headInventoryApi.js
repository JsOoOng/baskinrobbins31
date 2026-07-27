/**
 * [모듈 흐름 안내] headInventoryApi
 * 역할: 재고 화면에서 사용하는 HTTP 요청을 한곳에 모은 API 모듈이다.
 * 호출 흐름: Vue 화면 -> 이 모듈 -> Axios/Vite proxy -> Spring Controller (/head/inventory, /head/inventory/${storeInventoryId}, /head/inventory/${storeInventoryId}/restock-setting)
 * 데이터 기준: 실제 요청 URL과 현재 백엔드 DTO 필드를 우선한다.
 */
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