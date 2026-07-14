package com.kiosk.branch.order.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BranchOrderDetailResponse {

    // 주문 정보
    private Integer orderId;
    private Integer orderNumber;
    private String orderType;
    private String orderStatus;

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