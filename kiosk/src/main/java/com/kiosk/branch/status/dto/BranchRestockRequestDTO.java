package com.kiosk.branch.status.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
public class BranchRestockRequestDTO {


    /*
     * 상품 재고 ID
     *
     * 상품 발주일 경우 사용
     */
    private Integer storeInventoryId;



    /*
     * 맛 재고 ID
     *
     * 맛 발주일 경우 사용
     */
    private Integer storeFlavorId;



    /*
     * 발주 요청 수량
     */
    private Integer requestQuantity;


}