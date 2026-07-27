package com.kiosk.headquarter.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kiosk.headquarter.dto.statistics.HeadProductSalesResponse;
import com.kiosk.headquarter.dto.statistics.HeadSalesTrendResponse;
import com.kiosk.headquarter.dto.statistics.HeadStatisticsPeriod;
import com.kiosk.headquarter.dto.statistics.HeadStatisticsSummaryResponse;
import com.kiosk.headquarter.dto.statistics.HeadStoreSalesResponse;
import com.kiosk.headquarter.repository.HeadStatisticsMapper;
import com.kiosk.headquarter.repository.HeadStatisticsMapper.ProductSalesProjection;
import com.kiosk.headquarter.repository.HeadStatisticsMapper.StoreSalesProjection;
import com.kiosk.headquarter.repository.HeadStatisticsMapper.SummaryProjection;
import com.kiosk.headquarter.repository.HeadStatisticsMapper.TrendProjection;
import com.kiosk.headquarter.repository.HeadStoreMapper;
import com.kiosk.headquarter.dto.statistics.HeadFlavorSalesResponse;
import com.kiosk.headquarter.repository.HeadStatisticsMapper.FlavorSalesProjection;

import lombok.RequiredArgsConstructor;

/**
 * [코드 흐름 안내] HeadStatisticsService
 *
 * <p>역할: 본사 관리의 통계 업무 규칙과 상태 변경을 처리한다.</p>
 * <p>호출 흐름: Controller 호출 -> 이 서비스 -> HeadStatisticsMapper, HeadStoreMapper -> Entity/DTO 변환 -> Controller 반환 순서로 동작한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HeadStatisticsService {

    private final HeadStatisticsMapper
            headStatisticsMapper;

    private final HeadStoreMapper
            headStoreMapper;

    /*
     * 통계 요약 조회
     */
    /**
     * [메서드 흐름] getSummary
     * Controller 또는 상위 서비스에서 호출되어 HeadStatisticsMapper, HeadStoreMapper을 사용해 검증·조회·저장 등의 처리를 수행하고 결과를 반환한다.
     */
    public HeadStatisticsSummaryResponse getSummary(
            LocalDate startDate,
            LocalDate endDate,
            Integer storeId
    ) {

        DateRange range =
                createDateRange(
                        startDate,
                        endDate
                );

        validateStore(storeId);

        SummaryProjection projection =
                headStatisticsMapper.findSummary(
                        range.startDateTime(),
                        range.endDateTime(),
                        storeId
                );

        long totalSales =
                toLong(
                        projection.getTotalSales()
                );

        long orderCount =
                toLong(
                        projection.getOrderCount()
                );

        long salesQuantity =
                toLong(
                        projection.getSalesQuantity()
                );

        long averageOrderAmount =
                orderCount == 0
                        ? 0
                        : totalSales / orderCount;

        return HeadStatisticsSummaryResponse
                .builder()
                .totalSales(totalSales)
                .orderCount(orderCount)
                .salesQuantity(salesQuantity)
                .averageOrderAmount(
                        averageOrderAmount
                )
                .build();
    }

    /*
     * 기간별 추이
     */
    /**
     * [메서드 흐름] getSalesTrend
     * Controller 또는 상위 서비스에서 호출되어 HeadStatisticsMapper, HeadStoreMapper을 사용해 검증·조회·저장 등의 처리를 수행하고 결과를 반환한다.
     */
    public List<HeadSalesTrendResponse>
            getSalesTrend(
                    LocalDate startDate,
                    LocalDate endDate,
                    Integer storeId,
                    HeadStatisticsPeriod period
            ) {

        DateRange range =
                createDateRange(
                        startDate,
                        endDate
                );

        validateStore(storeId);

        HeadStatisticsPeriod normalizedPeriod =
                period != null
                        ? period
                        : HeadStatisticsPeriod.DAILY;

        List<TrendProjection> projections =
                switch (normalizedPeriod) {

                    case DAILY ->
                            headStatisticsMapper
                                    .findDailyTrend(
                                            range.startDateTime(),
                                            range.endDateTime(),
                                            storeId
                                    );

                    case WEEKLY ->
                            headStatisticsMapper
                                    .findWeeklyTrend(
                                            range.startDateTime(),
                                            range.endDateTime(),
                                            storeId
                                    );

                    case MONTHLY ->
                            headStatisticsMapper
                                    .findMonthlyTrend(
                                            range.startDateTime(),
                                            range.endDateTime(),
                                            storeId
                                    );

                    case YEARLY ->
                            headStatisticsMapper
                                    .findYearlyTrend(
                                            range.startDateTime(),
                                            range.endDateTime(),
                                            storeId
                                    );
                };

        return projections
                .stream()
                .map(this::toTrendResponse)
                .toList();
    }

    /*
     * 지점별 매출 순위
     */
    /**
     * [메서드 흐름] getStoreSalesRanking
     * Controller 또는 상위 서비스에서 호출되어 HeadStatisticsMapper, HeadStoreMapper을 사용해 검증·조회·저장 등의 처리를 수행하고 결과를 반환한다.
     */
    public List<HeadStoreSalesResponse>
            getStoreSalesRanking(
                    LocalDate startDate,
                    LocalDate endDate,
                    Integer storeId,
                    Integer limit
            ) {

        DateRange range =
                createDateRange(
                        startDate,
                        endDate
                );

        validateStore(storeId);

        int normalizedLimit =
                normalizeLimit(limit);

        List<StoreSalesProjection> projections =
                headStatisticsMapper
                        .findStoreSalesRanking(
                                range.startDateTime(),
                                range.endDateTime(),
                                storeId,
                                PageRequest.of(
                                        0,
                                        normalizedLimit
                                )
                        );

        return projections
                .stream()
                .map(
                        projection ->
                                HeadStoreSalesResponse
                                        .builder()
                                        .storeId(
                                                projection
                                                        .getStoreId()
                                        )
                                        .storeName(
                                                projection
                                                        .getStoreName()
                                        )
                                        .totalSales(
                                                toLong(
                                                        projection
                                                                .getTotalSales()
                                                )
                                        )
                                        .orderCount(
                                                toLong(
                                                        projection
                                                                .getOrderCount()
                                                )
                                        )
                                        .salesQuantity(
                                                toLong(
                                                        projection
                                                                .getSalesQuantity()
                                                )
                                        )
                                        .build()
                )
                .toList();
    }

    /*
     * 상품별 판매 순위
     */
    /**
     * [메서드 흐름] getProductSalesRanking
     * Controller 또는 상위 서비스에서 호출되어 HeadStatisticsMapper, HeadStoreMapper을 사용해 검증·조회·저장 등의 처리를 수행하고 결과를 반환한다.
     */
    public List<HeadProductSalesResponse>
            getProductSalesRanking(
                    LocalDate startDate,
                    LocalDate endDate,
                    Integer storeId,
                    Integer limit
            ) {

        DateRange range =
                createDateRange(
                        startDate,
                        endDate
                );
        
        validateStore(storeId);

        int normalizedLimit =
                normalizeLimit(limit);

        List<ProductSalesProjection> projections =
                headStatisticsMapper
                        .findProductSalesRanking(
                                range.startDateTime(),
                                range.endDateTime(),
                                storeId,
                                PageRequest.of(
                                        0,
                                        normalizedLimit
                                )
                        );

        return projections
                .stream()
                .map(
                        projection ->
                                HeadProductSalesResponse
                                        .builder()
                                        .productId(
                                                projection
                                                        .getProductId()
                                        )
                                        .productName(
                                                projection
                                                        .getProductName()
                                        )
                                        .salesAmount(
                                                toLong(
                                                        projection
                                                                .getSalesAmount()
                                                )
                                        )
                                        .salesQuantity(
                                                toLong(
                                                        projection
                                                                .getSalesQuantity()
                                                )
                                        )
                                        .orderCount(
                                                toLong(
                                                        projection
                                                                .getOrderCount()
                                                )
                                        )
                                        .build()
                )
                .toList();
    }
    
    /*
     * 맛별 판매 순위
     */
    /**
     * [메서드 흐름] getFlavorSalesRanking
     * Controller 또는 상위 서비스에서 호출되어 HeadStatisticsMapper, HeadStoreMapper을 사용해 검증·조회·저장 등의 처리를 수행하고 결과를 반환한다.
     */
    public List<HeadFlavorSalesResponse>
            getFlavorSalesRanking(
                    LocalDate startDate,
                    LocalDate endDate,
                    Integer storeId,
                    Integer limit
            ) {

        DateRange range =
                createDateRange(
                        startDate,
                        endDate
                );

        validateStore(storeId);

        int normalizedLimit =
                normalizeLimit(limit);

        List<FlavorSalesProjection> projections =
                headStatisticsMapper
                        .findFlavorSalesRanking(
                                range.startDateTime(),
                                range.endDateTime(),
                                storeId,
                                PageRequest.of(
                                        0,
                                        normalizedLimit
                                )
                        );

        return projections
                .stream()
                .map(
                        projection ->
                                HeadFlavorSalesResponse
                                        .builder()
                                        .flavorId(
                                                projection
                                                        .getFlavorId()
                                        )
                                        .flavorName(
                                                projection
                                                        .getFlavorName()
                                        )
                                        .salesAmount(
                                                toLong(
                                                        projection
                                                                .getSalesAmount()
                                                )
                                        )
                                        .salesQuantity(
                                                toLong(
                                                        projection
                                                                .getSalesQuantity()
                                                )
                                        )
                                        .orderCount(
                                                toLong(
                                                        projection
                                                                .getOrderCount()
                                                )
                                        )
                                        .build()
                )
                .toList();
    }

    private HeadSalesTrendResponse toTrendResponse(
            TrendProjection projection
    ) {

        return HeadSalesTrendResponse
                .builder()
                .periodLabel(
                        projection.getPeriodLabel()
                )
                .totalSales(
                        toLong(
                                projection.getTotalSales()
                        )
                )
                .orderCount(
                        toLong(
                                projection.getOrderCount()
                        )
                )
                .salesQuantity(
                        toLong(
                                projection.getSalesQuantity()
                        )
                )
                .build();
    }

    /*
     * 시작일은 00:00 포함
     * 종료일 다음 날 00:00 미만
     *
     * 예:
     * 7월 1일 ~ 7월 31일
     * →
     * 7월 1일 00:00 이상
     * 8월 1일 00:00 미만
     */
    private DateRange createDateRange(
            LocalDate startDate,
            LocalDate endDate
    ) {

        if (
                startDate == null ||
                endDate == null
        ) {
            throw new IllegalArgumentException(
                    "조회 시작일과 종료일이 필요합니다."
            );
        }

        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException(
                    "종료일은 시작일보다 빠를 수 없습니다."
            );
        }

        return new DateRange(
                startDate.atStartOfDay(),
                endDate
                        .plusDays(1)
                        .atStartOfDay()
        );
    }

    private void validateStore(
            Integer storeId
    ) {

        if (storeId == null) {
            return;
        }

        if (!headStoreMapper.existsById(storeId)) {
            throw new IllegalArgumentException(
                    "존재하지 않는 지점입니다."
            );
        }
    }

    private int normalizeLimit(
            Integer limit
    ) {

        if (limit == null) {
            return 10;
        }

        return Math.max(
                1,
                Math.min(limit, 50)
        );
    }

    private long toLong(
            Number number
    ) {

        return number == null
                ? 0L
                : number.longValue();
    }

    private record DateRange(
            LocalDateTime startDateTime,
            LocalDateTime endDateTime
    ) {
    }
}