package com.kiosk.branch.order.dto;

import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BranchOrderDetailResponse {

    private Integer orderId;

    private Integer orderNumber;

    private String orderType;

    private String orderStatus;

    private Integer totalPrice;

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


