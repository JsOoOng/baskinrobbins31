package com.kiosk.headquarter.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

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
