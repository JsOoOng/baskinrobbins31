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

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/head/statistics")
@RequiredArgsConstructor
public class HeadStatisticsController {

    private final HeadStatisticsService
            headStatisticsService;

    /*
     * 통계 요약
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
}