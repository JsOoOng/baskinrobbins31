package com.kiosk.headquarter.dto.inventoryalert;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class HeadInventoryAlertSendRequest {

    /*
     * 알람을 지점에 전송한
     * 본사 관리자 번호
     */
    private Integer adminId;
}