package com.kiosk.headquarter.dto.deliverie;

import java.time.LocalDateTime;

import com.kiosk.entity.enums.DeliveryStatus;
import com.kiosk.entity.enums.RestockStatus;

import lombok.Builder;
import lombok.Getter;

/**
 * [코드 흐름 안내] HeadDeliveryResponseDTO
 *
 * <p>역할: 배송 계층 사이에서 필요한 값만 전달하는 DTO다.</p>
 * <p>호출 흐름: Controller·Service·프론트 사이의 데이터 모양을 현재 필드 정의로 고정한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
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

    // 배송 취소 모달에서 저장한 사유
    private String cancelReason;


    /*
     * 처리 관리자
     */

    // 관리자 ID
    private Integer adminId;


    // 관리자 이름
    private String adminName;

}
