package com.kiosk.headquarter.dto.restock;

import java.time.LocalDateTime;

import com.kiosk.entity.enums.RestockStatus;

import lombok.Builder;
import lombok.Getter;

/**
 * [코드 흐름 안내] HeadRestockDetailResponseDTO
 *
 * <p>역할: 재입고·발주 계층 사이에서 필요한 값만 전달하는 DTO다.</p>
 * <p>호출 흐름: Controller·Service·프론트 사이의 데이터 모양을 현재 필드 정의로 고정한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
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