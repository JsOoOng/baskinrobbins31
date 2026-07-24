package com.kiosk.headquarter.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kiosk.entity.enums.EventStatus;
import com.kiosk.entity.enums.RestockStatus;
import com.kiosk.entity.enums.StoreStatus;
import com.kiosk.entity.RestockRequest;
import com.kiosk.headquarter.dto.dashboard.HeadDashboardResponse;
import com.kiosk.headquarter.dto.dashboard.HeadDashboardResponse.StoreSummaryDto;
import com.kiosk.headquarter.dto.dashboard.HeadDashboardResponse.DashboardInventoryRequestDto;
import com.kiosk.headquarter.dto.statistics.HeadStatisticsSummaryResponse;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

import com.kiosk.entity.AdminActionLog;
import com.kiosk.headquarter.repository.AdminActionLogRepository;
import com.kiosk.headquarter.repository.HeadBannerMapper;
import com.kiosk.headquarter.repository.HeadProductMapper;
import com.kiosk.headquarter.repository.HeadRestockRequestMapper;
import com.kiosk.headquarter.repository.HeadStoreMapper;
import com.kiosk.headquarter.repository.StoreStatusHistoryRepository;
import com.kiosk.headquarter.repository.HeadEventRepository;
import com.kiosk.headquarter.dto.dashboard.HeadDashboardResponse.RecentActionDto;

import lombok.RequiredArgsConstructor;

/**
 * [코드 흐름 안내] HeadDashboardService
 *
 * <p>역할: 본사 관리의 대시보드 업무 규칙과 상태 변경을 처리한다.</p>
 * <p>호출 흐름: Controller 호출 -> 이 서비스 -> HeadStoreMapper, HeadProductMapper, HeadRestockRequestMapper, HeadBannerMapper 등 -> Entity/DTO 변환 -> Controller 반환 순서로 동작한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HeadDashboardService {

    private final HeadStoreMapper headStoreMapper;
    private final HeadProductMapper headProductMapper;
    private final HeadRestockRequestMapper headRestockRequestMapper;
    private final HeadBannerMapper headBannerMapper;
    private final HeadStatisticsService headStatisticsService;
    private final AdminActionLogRepository adminActionLogRepository;
    private final HeadEventRepository headEventRepository;
    private final StoreStatusHistoryRepository storeStatusHistoryRepository;

    /**
     * [메서드 흐름] getDashboardSummary
     * Controller 또는 상위 서비스에서 호출되어 HeadStoreMapper, HeadProductMapper, HeadRestockRequestMapper 등을 사용해 검증·조회·저장 등의 처리를 수행하고 결과를 반환한다.
     */
    public HeadDashboardResponse getDashboardSummary(String comparisonPeriod) {
        // 비교 기준(comparisonPeriod)에 따른 로직은 현재 통계 요약 쿼리에서 당일 데이터를 기본으로 조회합니다.
        // 필요 시 comparisonPeriod 값(예: "전일 대비", "전주 대비")에 따라 증감률을 계산하는 로직을 추가할 수 있습니다.
        
        LocalDate today = LocalDate.now();

        // 1. 전체 지점 수 및 운영 중인 지점 수
        long totalStores = headStoreMapper.count();
        long activeStores = headStoreMapper.countByStoreStatus(StoreStatus.OPEN);
        LocalDateTime comparisonDate = getComparisonDate(comparisonPeriod);
        long previousTotalStores =
                headStoreMapper.countByCreatedAtLessThanEqual(comparisonDate);
        long previousActiveStores =
                countActiveStoresAt(comparisonDate);
        long stoppedStores = headStoreMapper.countByStoreStatus(StoreStatus.CLOSED);
        long waitingStores = headStoreMapper.countByStoreStatus(StoreStatus.DAY_OFF); // 임시로 DAY_OFF를 승인 대기/휴무로 매핑

        // 2. 전체 상품 수
        long totalProducts = headProductMapper.count();

        // 3. 처리 대기 재고 신청
        long pendingInventory = headRestockRequestMapper.countByStatus(RestockStatus.WAITING);

        // 4. 진행 중인 할인 수 (할인율 0 초과인 상품 개수)
        long activeDiscounts = headProductMapper.countByDiscountRateGreaterThan(0);
        
        // 4-1. 진행 중인 이벤트 수
        long activeEvents = headEventRepository.countByEventStatus(EventStatus.ACTIVE);

        // 5. 노출 중인 배너 수
        long activeBanners = headBannerMapper.countByIsActiveTrue();

        // 6. 선택한 기간과 바로 앞의 동일한 길이 기간을 비교한다.
        SalesComparisonRange salesRange = getSalesComparisonRange(comparisonPeriod, today);
        HeadStatisticsSummaryResponse todayStats = headStatisticsService.getSummary(
                salesRange.currentStart(),
                salesRange.currentEnd(),
                null
        );
        long todaySales = todayStats.getTotalSales();
        long todayOrders = todayStats.getOrderCount();
        HeadStatisticsSummaryResponse comparisonStats =
                headStatisticsService.getSummary(
                        salesRange.previousStart(),
                        salesRange.previousEnd(),
                        null
                );
        long todaySalesChange =
                todaySales - comparisonStats.getTotalSales();
        long todayOrdersChange =
                todayOrders - comparisonStats.getOrderCount();

        // 7. 지점 운영 현황 (UI 구조에 맞춤)
        List<StoreSummaryDto> storeSummary = List.of(
            StoreSummaryDto.builder().label("정상 운영").value(activeStores).total(totalStores).type("normal").build(),
            StoreSummaryDto.builder().label("승인 대기").value(waitingStores).total(totalStores).type("waiting").build(),
            StoreSummaryDto.builder().label("운영 중단").value(stoppedStores).total(totalStores).type("stopped").build()
        );

        // 8. 최근 관리자 작업 이력
        List<AdminActionLog> recentLogs = adminActionLogRepository.findTop3ByOrderByCreatedAtDesc();
        List<RecentActionDto> recentActions = recentLogs.stream()
            .map(log -> RecentActionDto.builder()
                .administrator(log.getAdministrator())
                .action(log.getAction())
                .type(log.getType())
                .time(formatLogTime(log.getCreatedAt().toLocalDate(), log.getCreatedAt().toLocalTime(), today))
                .build())
            .collect(Collectors.toList());

        // 대시보드에서 검색·필터·정렬할 수 있도록 재고 신청 전체를 최신순으로 제공한다.
        List<RestockRequest> recentRequests = headRestockRequestMapper.findAllByOrderByIdDesc();
        List<DashboardInventoryRequestDto> inventoryRequests = recentRequests.stream().map(req -> {
            String storeName = "";
            String productName = "";
            if (req.getStoreInventory() != null) {
                storeName = req.getStoreInventory().getStore().getStoreName();
                productName = req.getStoreInventory().getItem().getProduct().getProductName();
            } else if (req.getStoreFlavor() != null) {
                storeName = req.getStoreFlavor().getStore().getStoreName();
                productName = req.getStoreFlavor().getFlavor().getFlavorName();
            }
            
            String status = "";
            String statusType = "";
            if (req.getStatus() != null) {
                switch(req.getStatus()) {
                    case WAITING: status = "승인 대기"; statusType = "waiting"; break;
                    case APPROVED: status = "승인 완료"; statusType = "approved"; break;
                    case SHIPPING: status = "배송 중"; statusType = "shipping"; break;
                    case COMPLETED: status = "배송 완료"; statusType = "completed"; break;
                    case REJECTED: status = "반려"; statusType = "rejected"; break;
                    case CANCELED: status = "취소"; statusType = "canceled"; break;
                    default: status = req.getStatus().name(); statusType = "default"; break;
                }
            }
            
            String reqDate = req.getRequestedAt() != null ? req.getRequestedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : "";
            
            return DashboardInventoryRequestDto.builder()
                .requestNumber(req.getId())
                .storeName(storeName)
                .productName(productName)
                .quantity(req.getRequestQuantity())
                .requestDate(reqDate)
                .status(status)
                .statusType(statusType)
                .build();
        }).collect(Collectors.toList());

        return HeadDashboardResponse.builder()
                .totalStores(totalStores)
                .activeStores(activeStores)
                .totalStoresChange(totalStores - previousTotalStores)
                .activeStoresChange(activeStores - previousActiveStores)
                .totalProducts(totalProducts)
                .pendingInventory(pendingInventory)
                .activeDiscounts(activeDiscounts)
                .activeEvents(activeEvents)
                .activeBanners(activeBanners)
                .todaySales(todaySales)
                .todayOrders(todayOrders)
                .todaySalesChange(todaySalesChange)
                .todaySalesChangeRate(
                        calculateChangeRate(
                                todaySalesChange,
                                comparisonStats.getTotalSales()
                        )
                )
                .todayOrdersChange(todayOrdersChange)
                .todayOrdersChangeRate(
                        calculateChangeRate(
                                todayOrdersChange,
                                comparisonStats.getOrderCount()
                        )
                )
                .storeSummary(storeSummary)
                .inventoryRequests(inventoryRequests)
                .recentActions(recentActions)
                .build();
    }

    /**
     * 이전 값 대비 증감률을 계산한다.
     * 이전 값이 0이면 0→양수 변화를 100% 증가로 표시해 0으로 나누는 오류를 막는다.
     */
    private double calculateChangeRate(
            long change,
            long previousValue
    ) {
        if (previousValue == 0) {
            return change == 0 ? 0 : 100;
        }
        return (double) change / previousValue * 100;
    }

    /**
     * 화면에서 선택한 비교 기준을 실제 비교 시점으로 변환한다.
     */
    private LocalDateTime getComparisonDate(String comparisonPeriod) {
        LocalDateTime now = LocalDateTime.now();
        if ("전일 대비".equals(comparisonPeriod)) {
            return now.minusDays(1);
        }
        if ("전월 대비".equals(comparisonPeriod)) {
            return now.minusMonths(1);
        }
        if ("전년 동기 대비".equals(comparisonPeriod)
                || "전년도 대비".equals(comparisonPeriod)) {
            return now.minusYears(1);
        }
        return now.minusWeeks(1);
    }

    /**
     * 매출·주문 비교 기간을 현재 구간과 바로 앞의 같은 길이 구간으로 나눈다.
     * 전주/전월/전년은 달력의 특정 하루가 아니라 각각 최근 7/30/365일 합계를 비교한다.
     */
    private SalesComparisonRange getSalesComparisonRange(
            String comparisonPeriod,
            LocalDate today
    ) {
        int days = switch (comparisonPeriod) {
            case "전일 대비" -> 1;
            case "전월 대비" -> 30;
            case "전년 동기 대비", "전년도 대비" -> 365;
            default -> 7;
        };
        LocalDate currentStart = today.minusDays(days - 1L);
        LocalDate previousEnd = currentStart.minusDays(1);
        return new SalesComparisonRange(
                currentStart,
                today,
                previousEnd.minusDays(days - 1L),
                previousEnd
        );
    }

    private record SalesComparisonRange(
            LocalDate currentStart,
            LocalDate currentEnd,
            LocalDate previousStart,
            LocalDate previousEnd
    ) {}

    /**
     * 비교 시점에 이미 존재했던 지점마다 당시 마지막 상태를 찾아 운영 지점 수를 계산한다.
     * 이력 도입 전 한 번도 상태를 바꾸지 않은 지점은 현재 상태를 초기 상태로 사용한다.
     */
    private long countActiveStoresAt(LocalDateTime comparisonDate) {
        return headStoreMapper.findAll().stream()
                .filter(store ->
                        store.getCreatedAt() == null
                                || !store.getCreatedAt().isAfter(comparisonDate)
                )
                .filter(store ->
                        storeStatusHistoryRepository
                                .findTopByStore_IdAndChangedAtLessThanEqualOrderByChangedAtDesc(
                                        store.getId(),
                                        comparisonDate
                                )
                                .map(history -> history.getStoreStatus() == StoreStatus.OPEN)
                                .orElse(store.getStoreStatus() == StoreStatus.OPEN)
                )
                .count();
    }

    private String formatLogTime(LocalDate logDate, java.time.LocalTime logTime, LocalDate today) {
        String timeString = logTime.format(DateTimeFormatter.ofPattern("a hh:mm")).replace("AM", "오전").replace("PM", "오후");
        
        if (logDate.isEqual(today)) {
            return "오늘 " + timeString;
        } else if (logDate.isEqual(today.minusDays(1))) {
            return "어제 " + timeString;
        } else {
            return logDate.format(DateTimeFormatter.ofPattern("yyyy.MM.dd")) + " " + timeString;
        }
    }
}
