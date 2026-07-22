package com.kiosk.headquarter.dto.restock;

import java.time.LocalDateTime;

import com.kiosk.entity.enums.RestockStatus;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class HeadRestockDetailResponseDTO {


    /*
     * 발주 요청 ID
     */
    private Integer requestId;


    /*
     * 제품 재고 ID
     * 없으면 0
     */
    private Integer storeInventoryId;


    /*
     * 맛 재고 ID
     * 없으면 0
     */
    private Integer storeFlavorId;


    /*
     * 재고명
     */
    private String itemName;


    /*
     * 단위
     */
    private String unit;


    /*
     * 단가
     */
    private Integer unitPrice;


    /*
     * 요청 수량
     */
    private Integer requestQuantity;


    /*
     * 총 금액
     */
    private Integer totalPrice;


    /*
     * 상태
     */
    private RestockStatus status;


    /*
     * 처리 관리자 ID
     */
    private Integer adminId;


    /*
     * 처리 관리자명
     */
    private String adminName;


    /*
     * 요청 시간
     */
    private LocalDateTime requestedAt;

}