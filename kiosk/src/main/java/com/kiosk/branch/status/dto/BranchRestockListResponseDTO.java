package com.kiosk.branch.status.dto;

import java.time.LocalDateTime;

import com.kiosk.entity.enums.RestockStatus;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BranchRestockListResponseDTO {

    private Integer requestId;

    private Integer storeInventoryId;

    private Integer storeFlavorId;

    private String itemName;

    private String unit;

    private Integer requestQuantity;

    private RestockStatus status;

    private Integer adminId;

    private String adminName;

    private LocalDateTime requestedAt;

}
