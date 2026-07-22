package com.kiosk.headquarter.service;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kiosk.entity.enums.RestockStatus;
import com.kiosk.entity.enums.StoreStatus;
import com.kiosk.headquarter.dto.dashboard.HeadDashboardResponse;
import com.kiosk.headquarter.dto.dashboard.HeadDashboardResponse.StoreSummaryDto;
import com.kiosk.headquarter.dto.statistics.HeadStatisticsSummaryResponse;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

import com.kiosk.entity.AdminActionLog;
import com.kiosk.headquarter.repository.AdminActionLogRepository;
import com.kiosk.headquarter.repository.HeadBannerMapper;
import com.kiosk.headquarter.repository.HeadProductMapper;
import com.kiosk.headquarter.repository.HeadRestockRequestMapper;
import com.kiosk.headquarter.repository.HeadStoreMapper;
import com.kiosk.headquarter.dto.dashboard.HeadDashboardResponse.RecentActionDto;

import lombok.RequiredArgsConstructor;

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

    public HeadDashboardResponse getDashboardSummary(String comparisonPeriod) {
        // 비교 기준(comparisonPeriod)에 따른 로직은 현재 통계 요약 쿼리에서 당일 데이터를 기본으로 조회합니다.
        // 필요 시 comparisonPeriod 값(예: "전일 대비", "전주 대비")에 따라 증감률을 계산하는 로직을 추가할 수 있습니다.
        
        LocalDate today = LocalDate.now();

        // 1. 전체 지점 수 및 운영 중인 지점 수
        long totalStores = headStoreMapper.count();
        long activeStores = headStoreMapper.countByStoreStatus(StoreStatus.OPEN);
        long stoppedStores = headStoreMapper.countByStoreStatus(StoreStatus.CLOSED);
        long waitingStores = headStoreMapper.countByStoreStatus(StoreStatus.DAY_OFF); // 임시로 DAY_OFF를 승인 대기/휴무로 매핑

        // 2. 전체 상품 수
        long totalProducts = headProductMapper.count();

        // 3. 처리 대기 재고 신청
        long pendingInventory = headRestockRequestMapper.countByStatus(RestockStatus.WAITING);

        // 4. 진행 중인 할인 수 (할인율 0 초과인 상품 개수)
        long activeDiscounts = headProductMapper.countByDiscountRateGreaterThan(0);

        // 5. 노출 중인 배너 수
        long activeBanners = headBannerMapper.countByIsActiveTrue();

        // 6. 오늘 전체 매출 및 주문 수 (HeadStatisticsService 활용)
        HeadStatisticsSummaryResponse todayStats = headStatisticsService.getSummary(today, today, null);
        long todaySales = todayStats.getTotalSales();
        long todayOrders = todayStats.getOrderCount();

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

        return HeadDashboardResponse.builder()
                .totalStores(totalStores)
                .activeStores(activeStores)
                .totalProducts(totalProducts)
                .pendingInventory(pendingInventory)
                .activeDiscounts(activeDiscounts)
                .activeBanners(activeBanners)
                .todaySales(todaySales)
                .todayOrders(todayOrders)
                .storeSummary(storeSummary)
                .inventoryRequests(Collections.emptyList()) // P2 기능이므로 빈 배열 반환
                .recentActions(recentActions)
                .build();
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
