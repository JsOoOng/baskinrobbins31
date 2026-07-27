package com.kiosk.headquarter.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kiosk.headquarter.dto.statistics.HeadProductSalesResponse;
import com.kiosk.headquarter.dto.statistics.HeadSalesTrendResponse;
import com.kiosk.headquarter.dto.statistics.HeadStatisticsPeriod;
import com.kiosk.headquarter.dto.statistics.HeadStatisticsSummaryResponse;
import com.kiosk.headquarter.dto.statistics.HeadStoreSalesResponse;
import com.kiosk.headquarter.service.HeadStatisticsService;
import com.kiosk.headquarter.dto.statistics.HeadFlavorSalesResponse;

import lombok.RequiredArgsConstructor;

/**
 * [코드 흐름 안내] HeadStatisticsController
 *
 * <p>역할: 본사 관리의 통계 HTTP 요청을 받는 진입점이다.</p>
 * <p>호출 흐름: Vue/API 요청 -> 이 컨트롤러(/head/statistics) -> HeadStatisticsService -> 응답 DTO 또는 JSON -> 화면 갱신 순서로 이동한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
@RestController
@RequestMapping("/head/statistics")
@RequiredArgsConstructor
public class HeadStatisticsController {

    private final HeadStatisticsService
            headStatisticsService;

    /*
     * 통계 요약
     */
    /**
     * [요청 흐름] GET /head/statistics/summary
     * 프론트 요청을 받아 getSummary() 메서드가 입력을 받고 HeadStatisticsService 호출 후 결과를 응답한다.
     */
    @GetMapping("/summary")
    public ResponseEntity<HeadStatisticsSummaryResponse>
            getSummary(
                    @RequestParam
                    @DateTimeFormat(
                            iso = DateTimeFormat.ISO.DATE
                    )
                    LocalDate startDate,

                    @RequestParam
                    @DateTimeFormat(
                            iso = DateTimeFormat.ISO.DATE
                    )
                    LocalDate endDate,

                    @RequestParam(required = false)
                    Integer storeId
            ) {

        return ResponseEntity.ok(
                headStatisticsService.getSummary(
                        startDate,
                        endDate,
                        storeId
                )
        );
    }

    /*
     * 기간별 매출 추이
     */
    /**
     * [요청 흐름] GET /head/statistics/sales-trend
     * 프론트 요청을 받아 getSalesTrend() 메서드가 입력을 받고 HeadStatisticsService 호출 후 결과를 응답한다.
     */
    @GetMapping("/sales-trend")
    public ResponseEntity<List<HeadSalesTrendResponse>>
            getSalesTrend(
                    @RequestParam
                    @DateTimeFormat(
                            iso = DateTimeFormat.ISO.DATE
                    )
                    LocalDate startDate,

                    @RequestParam
                    @DateTimeFormat(
                            iso = DateTimeFormat.ISO.DATE
                    )
                    LocalDate endDate,

                    @RequestParam(required = false)
                    Integer storeId,

                    @RequestParam(
                            defaultValue = "DAILY"
                    )
                    HeadStatisticsPeriod period
            ) {

        return ResponseEntity.ok(
                headStatisticsService.getSalesTrend(
                        startDate,
                        endDate,
                        storeId,
                        period
                )
        );
    }

    /*
     * 지점별 매출 순위
     */
    /**
     * [요청 흐름] GET /head/statistics/stores
     * 프론트 요청을 받아 getStoreSalesRanking() 메서드가 입력을 받고 HeadStatisticsService 호출 후 결과를 응답한다.
     */
    @GetMapping("/stores")
    public ResponseEntity<List<HeadStoreSalesResponse>>
            getStoreSalesRanking(
                    @RequestParam
                    @DateTimeFormat(
                            iso = DateTimeFormat.ISO.DATE
                    )
                    LocalDate startDate,

                    @RequestParam
                    @DateTimeFormat(
                            iso = DateTimeFormat.ISO.DATE
                    )
                    LocalDate endDate,

                    @RequestParam(required = false)
                    Integer storeId,

                    @RequestParam(
                            defaultValue = "10"
                    )
                    Integer limit
            ) {

        return ResponseEntity.ok(
                headStatisticsService
                        .getStoreSalesRanking(
                                startDate,
                                endDate,
                                storeId,
                                limit
                        )
        );
    }

    /*
     * 상품별 판매 순위
     */
    /**
     * [요청 흐름] GET /head/statistics/products
     * 프론트 요청을 받아 getProductSalesRanking() 메서드가 입력을 받고 HeadStatisticsService 호출 후 결과를 응답한다.
     */
    @GetMapping("/products")
    public ResponseEntity<List<HeadProductSalesResponse>>
            getProductSalesRanking(
                    @RequestParam
                    @DateTimeFormat(
                            iso = DateTimeFormat.ISO.DATE
                    )
                    LocalDate startDate,

                    @RequestParam
                    @DateTimeFormat(
                            iso = DateTimeFormat.ISO.DATE
                    )
                    LocalDate endDate,

                    @RequestParam(required = false)
                    Integer storeId,

                    @RequestParam(
                            defaultValue = "10"
                    )
                    Integer limit
            ) {

        return ResponseEntity.ok(
                headStatisticsService
                        .getProductSalesRanking(
                                startDate,
                                endDate,
                                storeId,
                                limit
                        )
        );
    }
    /*
     * 맛별 판매 순위
     */
    /**
     * [요청 흐름] GET /head/statistics/flavors
     * 프론트 요청을 받아 getFlavorSalesRanking() 메서드가 입력을 받고 HeadStatisticsService 호출 후 결과를 응답한다.
     */
    @GetMapping("/flavors")
    public ResponseEntity<List<HeadFlavorSalesResponse>>
            getFlavorSalesRanking(
                    @RequestParam
                    @DateTimeFormat(
                            iso = DateTimeFormat.ISO.DATE
                    )
                    LocalDate startDate,

                    @RequestParam
                    @DateTimeFormat(
                            iso = DateTimeFormat.ISO.DATE
                    )
                    LocalDate endDate,

                    @RequestParam(required = false)
                    Integer storeId,

                    @RequestParam(
                            defaultValue = "10"
                    )
                    Integer limit
            ) {

        return ResponseEntity.ok(
                headStatisticsService
                        .getFlavorSalesRanking(
                                startDate,
                                endDate,
                                storeId,
                                limit
                        )
        );
    }
}