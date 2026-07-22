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