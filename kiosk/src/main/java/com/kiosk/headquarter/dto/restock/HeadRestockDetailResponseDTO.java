package com.kiosk.headquarter.dto.restock;

import java.time.LocalDateTime;

import com.kiosk.entity.enums.RestockStatus;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class HeadRestockDetailResponseDTO {

    private Integer requestId;

    private Integer storeId;
    private String storeName;

    private Integer itemId;
    private String itemName;
    private String unit;
    private Integer unitPrice;

    private Integer requestQuantity;
    private Integer totalPrice;

    private RestockStatus status;

    private Integer adminId;
    private String adminName;

    private LocalDateTime requestedAt;
}