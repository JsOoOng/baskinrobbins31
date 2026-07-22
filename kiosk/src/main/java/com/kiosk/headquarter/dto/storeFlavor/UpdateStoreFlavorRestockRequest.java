package com.kiosk.headquarter.dto.storeFlavor;

import com.kiosk.entity.enums.AutoRestockMode;

import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@NoArgsConstructor
public class UpdateStoreFlavorRestockRequest {


    /*
     * 최소 재고
     */
    private Integer minStock;


    /*
     * 목표 재고
     */
    private Integer targetStock;


    /*
     * 자동 보충 사용 여부
     */
    private Boolean autoRestockEnabled;


    /*
     * 자동 보충 방식
     */
    private AutoRestockMode restockMode;

}