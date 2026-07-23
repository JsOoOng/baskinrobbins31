package com.kiosk.headquarter.dto.deliverie;

import java.time.LocalDateTime;

import com.kiosk.entity.enums.DeliveryStatus;
import com.kiosk.entity.enums.RestockStatus;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class HeadDeliveryResponseDTO {


    private Integer deliveryId;


    private Integer restockRequestId;



    // 지점명
    private String storeName;



    // 재고 품목
    private String itemName;



    // 단위
    private String unit;



    // 신청 수량
    private Integer requestQuantity;



    // 신청 상태
    private RestockStatus restockStatus;



    // 신청 일시
    private LocalDateTime requestedAt;



    // 배송 상태
    private DeliveryStatus deliveryStatus;



    /*
     * 처리 관리자
     */

    // 관리자 ID
    private Integer adminId;


    // 관리자 이름
    private String adminName;

}