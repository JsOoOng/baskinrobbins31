package com.kiosk.headquarter.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * [코드 흐름 안내] HeadDashboardResponse
 *
 * <p>역할: 대시보드 처리 결과를 프론트에 반환하기 위한 응답 DTO다.</p>
 * <p>호출 흐름: Entity/조회 결과 -> Service 변환 -> 이 DTO -> Controller JSON 응답 순서로 사용한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HeadDashboardResponse {

    private long totalStores;
    private long activeStores;
    private long totalProducts;
    private long pendingInventory;
    private long activeDiscounts;
    private long activeEvents;
    private long activeBanners;
    private long todaySales;
    private long todayOrders;

    private List<StoreSummaryDto> storeSummary;
    private List<DashboardInventoryRequestDto> inventoryRequests;
    private List<RecentActionDto> recentActions;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StoreSummaryDto {
        private String label;
        private long value;
        private long total;
        private String type; // normal, waiting, stopped
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DashboardInventoryRequestDto {
        private Integer requestNumber;
        private String storeName;
        private String productName;
        private Integer quantity;
        private String requestDate;
        private String status;
        private String statusType;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecentActionDto {
        private String administrator;
        private String action;
        private String time;
        private String type;
    }
}
