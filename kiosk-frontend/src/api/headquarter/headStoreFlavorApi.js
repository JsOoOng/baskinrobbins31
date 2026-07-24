/**
 * [모듈 흐름 안내] headStoreFlavorApi
 * 역할: 지점별 맛 화면에서 사용하는 HTTP 요청을 한곳에 모은 API 모듈이다.
 * 호출 흐름: Vue 화면 -> 이 모듈 -> Axios/Vite proxy -> Spring Controller (/head/flavor-inventory, /head/flavor-inventory/store/${storeId}, /head/flavor-inventory/${storeFlavorId})
 * 데이터 기준: 실제 요청 URL과 현재 백엔드 DTO 필드를 우선한다.
 */
import api from '@/api/axios'


// 전체 맛 재고 조회
export const getHeadStoreFlavorInventories = () => {

    return api.get(
        '/head/flavor-inventory'
    )

}



// 지점별 맛 재고 조회
export const getHeadStoreFlavorInventoriesByStore = (
    storeId
) => {

    return api.get(
        `/head/flavor-inventory/store/${storeId}`
    )

}



// 자동보충 설정 변경
export const updateHeadStoreFlavorRestockSetting = (
    storeFlavorId,
    request
) => {

    return api.patch(
        `/head/flavor-inventory/${storeFlavorId}`,
        request
    )

}

/*
 * 본사 맛 재고 화면 → 통 입고 모달 → PATCH API
 * 일반 상품 개수가 아닌 아이스크림 컨테이너 수량을 백엔드에 전달합니다.
 */
export const stockInHeadStoreFlavor = (
    storeFlavorId,
    containerQuantity
) => {
    return api.patch(
        `/head/flavor-inventory/${storeFlavorId}/stock-in`,
        { containerQuantity }
    )
}
