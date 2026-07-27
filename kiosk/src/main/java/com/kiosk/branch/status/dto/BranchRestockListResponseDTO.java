package com.kiosk.branch.status.dto;

import java.time.LocalDateTime;

import com.kiosk.entity.enums.RestockStatus;

import lombok.Builder;
import lombok.Getter;

/**
 * [코드 흐름 안내] BranchRestockListResponseDTO
 *
 * <p>역할: 재입고·발주 계층 사이에서 필요한 값만 전달하는 DTO다.</p>
 * <p>호출 흐름: Controller·Service·프론트 사이의 데이터 모양을 현재 필드 정의로 고정한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
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
