package com.kiosk.headquarter.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kiosk.headquarter.dto.statistics.HeadDailySalesResponseDTO;
import com.kiosk.headquarter.dto.statistics.HeadExpenseSummaryResponseDTO;
import com.kiosk.headquarter.dto.statistics.HeadProductSalesResponseDTO;
import com.kiosk.headquarter.dto.statistics.HeadSalesSummaryResponseDTO;
import com.kiosk.headquarter.dto.statistics.HeadStoreSalesResponseDTO;
import com.kiosk.headquarter.service.HeadStatisticsService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class HeadStatisticsController {

    private final HeadStatisticsService headStatisticsService;

    // 전체 매출 요약
    @GetMapping("/head/statistics/sales")
    public HeadSalesSummaryResponseDTO getSalesSummary() {
        return headStatisticsService.getSalesSummary();
    }

    // 지점별 매출 조회
    @GetMapping("/head/statistics/stores")
    public List<HeadStoreSalesResponseDTO> getStoreSalesList() {
        return headStatisticsService.getStoreSalesList();
    }

    // 상품별 판매량 / 매출 조회
    @GetMapping("/head/statistics/products")
    public List<HeadProductSalesResponseDTO> getProductSalesList() {
        return headStatisticsService.getProductSalesList();
    }

    // 일자별 매출 조회
    @GetMapping("/head/statistics/period")
    public List<HeadDailySalesResponseDTO> getDailySalesList() {
        return headStatisticsService.getDailySalesList();
    }

    // 지출 카테고리별 조회
    @GetMapping("/head/statistics/expenses")
    public List<HeadExpenseSummaryResponseDTO> getExpenseSummaryList() {
        return headStatisticsService.getExpenseSummaryList();
    }
}