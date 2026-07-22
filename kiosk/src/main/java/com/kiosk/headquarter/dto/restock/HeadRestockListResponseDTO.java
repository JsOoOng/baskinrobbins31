package com.kiosk.headquarter.dto.restock;

import java.time.LocalDateTime;

import com.kiosk.entity.enums.RestockStatus;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class HeadRestockListResponseDTO {


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
     * 제품명 또는 맛 이름
     */
    private String itemName;



    /*
     * 단위
     */
    private String unit;



    /*
     * 요청 수량
     */
    private Integer requestQuantity;



    /*
     * 발주 상태
     */
    private RestockStatus status;



    /*
     * 처리 관리자
     */
    private Integer adminId;



    /*
     * 관리자 이름
     */
    private String adminName;



    /*
     * 요청 시간
     */
    private LocalDateTime requestedAt;

}