package com.kiosk.branch.order.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Builder;
import lombok.Getter;

/**
 * [코드 흐름 안내] BranchOrderDetailResponse
 *
 * <p>역할: 주문 처리 결과를 프론트에 반환하기 위한 응답 DTO다.</p>
 * <p>호출 흐름: Entity/조회 결과 -> Service 변환 -> 이 DTO -> Controller JSON 응답 순서로 사용한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
@Getter
@Builder
public class BranchOrderDetailResponse {

    // 주문 정보
    private Integer orderId;
    private Integer orderNumber;
    private String orderType;
    private String orderStatus;
    
    private Integer dryIceCount;

    // 결제 정보
    private Integer baseAmount;
    private Integer couponDiscount;
    private Integer pointUsed;
    private Integer finalAmount;

    private String paymentMethod;
    private String paymentStatus;

    private LocalDateTime paymentDate;

    // 주문 상품
    private List<ItemResponse> items;


    @Getter
    @Builder
    public static class ItemResponse {

        private String productName;
        private Integer quantity;
        private Integer unitPrice;

        private List<OptionResponse> options;
        private List<FlavorResponse> flavors;
    }

    @Getter
    @Builder
    public static class OptionResponse {

        private String optionType;
        private String optionName;
        private Integer extraPrice;
    }

    @Getter
    @Builder
    public static class FlavorResponse {

        private String flavorName;
        private Integer quantity;
    }
}