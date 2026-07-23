package com.kiosk.branch.inventory.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BranchInventoryShortageConfirmResponse {

    private Integer storeId;

    /*
     * 이번 요청에서 확인 처리된
     * 부족 알람 개수
     */
    private Integer confirmedCount;

    /*
     * 확인 후 이동할 화면
     */
    private String routeName;
}