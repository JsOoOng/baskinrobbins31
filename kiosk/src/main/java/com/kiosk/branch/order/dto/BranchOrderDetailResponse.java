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
    }
}