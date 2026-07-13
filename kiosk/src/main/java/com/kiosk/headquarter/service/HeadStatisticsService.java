package com.kiosk.headquarter.service;

import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.kiosk.headquarter.dto.statistics.HeadDailySalesResponseDTO;
import com.kiosk.headquarter.dto.statistics.HeadExpenseSummaryResponseDTO;
import com.kiosk.headquarter.dto.statistics.HeadProductSalesResponseDTO;
import com.kiosk.headquarter.dto.statistics.HeadSalesSummaryResponseDTO;
import com.kiosk.headquarter.dto.statistics.HeadStoreSalesResponseDTO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class HeadStatisticsService {

    private final JdbcTemplate jdbcTemplate;

    // 전체 매출 요약
    public HeadSalesSummaryResponseDTO getSalesSummary() {

        String sql = """
                SELECT
                    COUNT(DISTINCT o.order_id) AS order_count,
                    COALESCE(SUM(o.total_price), 0) AS total_order_amount,
                    COALESCE(SUM(p.final_amount), 0) AS total_paid_amount,
                    COALESCE(SUM(p.coupon_discount), 0) AS total_coupon_discount,
                    COALESCE(SUM(p.point_used), 0) AS total_point_used,
                    COALESCE((SELECT SUM(se.amount) FROM store_expenses se), 0) AS total_expense_amount
                FROM orders o
                LEFT JOIN payments p
                    ON o.order_id = p.order_id
                    AND p.payment_status = 'PAID'
                WHERE o.order_status <> 'CANCELED'
                """;

        return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
            long totalPaidAmount = rs.getLong("total_paid_amount");
            long totalExpenseAmount = rs.getLong("total_expense_amount");

            return HeadSalesSummaryResponseDTO.builder()
                    .orderCount(rs.getLong("order_count"))
                    .totalOrderAmount(rs.getLong("total_order_amount"))
                    .totalPaidAmount(totalPaidAmount)
                    .totalCouponDiscount(rs.getLong("total_coupon_discount"))
                    .totalPointUsed(rs.getLong("total_point_used"))
                    .totalExpenseAmount(totalExpenseAmount)
                    .netProfit(totalPaidAmount - totalExpenseAmount)
                    .build();
        });
    }

    // 지점별 매출 조회
    public List<HeadStoreSalesResponseDTO> getStoreSalesList() {

        String sql = """
                SELECT
                    s.store_id,
                    s.store_name,
                    COUNT(DISTINCT o.order_id) AS order_count,
                    COALESCE(SUM(p.final_amount), 0) AS total_paid_amount
                FROM stores s
                LEFT JOIN orders o
                    ON s.store_id = o.store_id
                    AND o.order_status <> 'CANCELED'
                LEFT JOIN payments p
                    ON o.order_id = p.order_id
                    AND p.payment_status = 'PAID'
                GROUP BY s.store_id, s.store_name
                ORDER BY total_paid_amount DESC
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) ->
                HeadStoreSalesResponseDTO.builder()
                        .storeId(rs.getInt("store_id"))
                        .storeName(rs.getString("store_name"))
                        .orderCount(rs.getLong("order_count"))
                        .totalPaidAmount(rs.getLong("total_paid_amount"))
                        .build()
        );
    }

    // 상품별 판매량 / 매출 조회
    public List<HeadProductSalesResponseDTO> getProductSalesList() {

        String sql = """
                SELECT
                    p.product_id,
                    p.product_name,
                    COALESCE(SUM(oi.quantity), 0) AS total_quantity,
                    COALESCE(SUM(oi.quantity * oi.unit_price), 0) AS total_sales_amount
                FROM products p
                JOIN order_items oi
                    ON p.product_id = oi.product_id
                JOIN orders o
                    ON oi.order_id = o.order_id
                WHERE o.order_status <> 'CANCELED'
                GROUP BY p.product_id, p.product_name
                ORDER BY total_quantity DESC
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) ->
                HeadProductSalesResponseDTO.builder()
                        .productId(rs.getInt("product_id"))
                        .productName(rs.getString("product_name"))
                        .totalQuantity(rs.getLong("total_quantity"))
                        .totalSalesAmount(rs.getLong("total_sales_amount"))
                        .build()
        );
    }

	 // 일자별 매출 조회
	    public List<HeadDailySalesResponseDTO> getDailySalesList() {
	
	        String sql = """
	                SELECT
	                    DATE(o.created_at) AS sales_date,
	                    COUNT(DISTINCT o.order_id) AS order_count,
	                    COALESCE(SUM(p.final_amount), 0) AS total_paid_amount
	                FROM orders o
	                LEFT JOIN payments p
	                    ON o.order_id = p.order_id
	                    AND p.payment_status = 'PAID'
	                WHERE o.order_status <> 'CANCELED'
	                  AND o.created_at IS NOT NULL
	                GROUP BY DATE(o.created_at)
	                ORDER BY sales_date DESC
	                """;
	
	        return jdbcTemplate.query(sql, (rs, rowNum) -> {
	            java.sql.Date salesDate = rs.getDate("sales_date");
	
	            return HeadDailySalesResponseDTO.builder()
	                    .salesDate(salesDate != null ? salesDate.toLocalDate() : null)
	                    .orderCount(rs.getLong("order_count"))
	                    .totalPaidAmount(rs.getLong("total_paid_amount"))
	                    .build();
	        });
	    }

    // 지출 카테고리별 조회
    public List<HeadExpenseSummaryResponseDTO> getExpenseSummaryList() {

        String sql = """
                SELECT
                    expense_category,
                    COUNT(*) AS expense_count,
                    COALESCE(SUM(amount), 0) AS total_expense_amount
                FROM store_expenses
                GROUP BY expense_category
                ORDER BY total_expense_amount DESC
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) ->
                HeadExpenseSummaryResponseDTO.builder()
                        .expenseCategory(rs.getString("expense_category"))
                        .expenseCount(rs.getLong("expense_count"))
                        .totalExpenseAmount(rs.getLong("total_expense_amount"))
                        .build()
        );
    }
}