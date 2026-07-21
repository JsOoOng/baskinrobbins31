package com.kiosk.headquarter.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.kiosk.entity.Order;

@Repository
public interface HeadStatisticsMapper
        extends JpaRepository<Order, Integer> {

    // ====================================================
    // Projection
    // ====================================================

    /*
     * 전체 요약 통계
     */
    interface SummaryProjection {

        Number getTotalSales();

        Number getOrderCount();

        Number getSalesQuantity();
    }

    /*
     * 기간별 매출 추이
     */
    interface TrendProjection {

        String getPeriodLabel();

        Number getTotalSales();

        Number getOrderCount();

        Number getSalesQuantity();
    }

    /*
     * 지점별 매출 통계
     */
    interface StoreSalesProjection {

        Integer getStoreId();

        String getStoreName();

        Number getTotalSales();

        Number getOrderCount();

        Number getSalesQuantity();
    }

    /*
     * 상품별 판매 통계
     */
    interface ProductSalesProjection {

        Integer getProductId();

        String getProductName();

        Number getSalesAmount();

        Number getSalesQuantity();

        Number getOrderCount();
    }

    // ====================================================
    // 전체 요약 통계
    // ====================================================

    /*
     * 매출액:
     * payment_status가 PAID인 final_amount 합계
     *
     * 주문 수:
     * 결제 완료 상태이며 주문 취소가 아닌 주문 수
     *
     * 판매 수량:
     * ORDER_ITEMS의 quantity 합계
     */
    @Query(
            value = """
                    SELECT
                        CAST(
                            COALESCE(
                                SUM(paid.paid_amount),
                                0
                            ) AS SIGNED
                        ) AS totalSales,

                        COUNT(o.order_id)
                            AS orderCount,

                        CAST(
                            COALESCE(
                                SUM(
                                    COALESCE(
                                        item_summary.sales_quantity,
                                        0
                                    )
                                ),
                                0
                            ) AS SIGNED
                        ) AS salesQuantity

                    FROM orders o

                    INNER JOIN (
                        SELECT
                            order_id,
                            SUM(final_amount) AS paid_amount,
                            MIN(payment_date) AS paid_at

                        FROM payments

                        WHERE payment_status = 'PAID'

                        GROUP BY order_id
                    ) paid
                        ON paid.order_id = o.order_id

                    LEFT JOIN (
                        SELECT
                            order_id,
                            SUM(quantity) AS sales_quantity

                        FROM order_items

                        GROUP BY order_id
                    ) item_summary
                        ON item_summary.order_id = o.order_id

                    WHERE o.order_status <> 'CANCELED'

                      AND paid.paid_at >= :startDate

                      AND paid.paid_at < :endDate

                      AND (
                          :storeId IS NULL
                          OR o.store_id = :storeId
                      )
                    """,
            nativeQuery = true
    )
    SummaryProjection findSummary(
            @Param("startDate")
            LocalDateTime startDate,

            @Param("endDate")
            LocalDateTime endDate,

            @Param("storeId")
            Integer storeId
    );

    // ====================================================
    // 일별 매출 추이
    // ====================================================

    @Query(
            value = """
                    SELECT
                        DATE_FORMAT(
                            paid.paid_at,
                            '%Y-%m-%d'
                        ) AS periodLabel,

                        CAST(
                            COALESCE(
                                SUM(paid.paid_amount),
                                0
                            ) AS SIGNED
                        ) AS totalSales,

                        COUNT(o.order_id)
                            AS orderCount,

                        CAST(
                            COALESCE(
                                SUM(
                                    COALESCE(
                                        item_summary.sales_quantity,
                                        0
                                    )
                                ),
                                0
                            ) AS SIGNED
                        ) AS salesQuantity

                    FROM orders o

                    INNER JOIN (
                        SELECT
                            order_id,
                            SUM(final_amount) AS paid_amount,
                            MIN(payment_date) AS paid_at

                        FROM payments

                        WHERE payment_status = 'PAID'

                        GROUP BY order_id
                    ) paid
                        ON paid.order_id = o.order_id

                    LEFT JOIN (
                        SELECT
                            order_id,
                            SUM(quantity) AS sales_quantity

                        FROM order_items

                        GROUP BY order_id
                    ) item_summary
                        ON item_summary.order_id = o.order_id

                    WHERE o.order_status <> 'CANCELED'

                      AND paid.paid_at >= :startDate

                      AND paid.paid_at < :endDate

                      AND (
                          :storeId IS NULL
                          OR o.store_id = :storeId
                      )

                    GROUP BY
                        DATE_FORMAT(
                            paid.paid_at,
                            '%Y-%m-%d'
                        )

                    ORDER BY
                        DATE_FORMAT(
                            paid.paid_at,
                            '%Y-%m-%d'
                        )
                    """,
            nativeQuery = true
    )
    List<TrendProjection> findDailyTrend(
            @Param("startDate")
            LocalDateTime startDate,

            @Param("endDate")
            LocalDateTime endDate,

            @Param("storeId")
            Integer storeId
    );

    // ====================================================
    // 주별 매출 추이
    // ====================================================

    /*
     * periodLabel은 해당 주의 월요일 날짜입니다.
     *
     * 예:
     * 2026-07-13
     */
    @Query(
            value = """
                    SELECT
                        DATE_FORMAT(
                            DATE_SUB(
                                DATE(paid.paid_at),
                                INTERVAL WEEKDAY(
                                    paid.paid_at
                                ) DAY
                            ),
                            '%Y-%m-%d'
                        ) AS periodLabel,

                        CAST(
                            COALESCE(
                                SUM(paid.paid_amount),
                                0
                            ) AS SIGNED
                        ) AS totalSales,

                        COUNT(o.order_id)
                            AS orderCount,

                        CAST(
                            COALESCE(
                                SUM(
                                    COALESCE(
                                        item_summary.sales_quantity,
                                        0
                                    )
                                ),
                                0
                            ) AS SIGNED
                        ) AS salesQuantity

                    FROM orders o

                    INNER JOIN (
                        SELECT
                            order_id,
                            SUM(final_amount) AS paid_amount,
                            MIN(payment_date) AS paid_at

                        FROM payments

                        WHERE payment_status = 'PAID'

                        GROUP BY order_id
                    ) paid
                        ON paid.order_id = o.order_id

                    LEFT JOIN (
                        SELECT
                            order_id,
                            SUM(quantity) AS sales_quantity

                        FROM order_items

                        GROUP BY order_id
                    ) item_summary
                        ON item_summary.order_id = o.order_id

                    WHERE o.order_status <> 'CANCELED'

                      AND paid.paid_at >= :startDate

                      AND paid.paid_at < :endDate

                      AND (
                          :storeId IS NULL
                          OR o.store_id = :storeId
                      )

                    GROUP BY
                        DATE_FORMAT(
                            DATE_SUB(
                                DATE(paid.paid_at),
                                INTERVAL WEEKDAY(
                                    paid.paid_at
                                ) DAY
                            ),
                            '%Y-%m-%d'
                        )

                    ORDER BY
                        DATE_FORMAT(
                            DATE_SUB(
                                DATE(paid.paid_at),
                                INTERVAL WEEKDAY(
                                    paid.paid_at
                                ) DAY
                            ),
                            '%Y-%m-%d'
                        )
                    """,
            nativeQuery = true
    )
    List<TrendProjection> findWeeklyTrend(
            @Param("startDate")
            LocalDateTime startDate,

            @Param("endDate")
            LocalDateTime endDate,

            @Param("storeId")
            Integer storeId
    );

    // ====================================================
    // 월별 매출 추이
    // ====================================================

    @Query(
            value = """
                    SELECT
                        DATE_FORMAT(
                            paid.paid_at,
                            '%Y-%m'
                        ) AS periodLabel,

                        CAST(
                            COALESCE(
                                SUM(paid.paid_amount),
                                0
                            ) AS SIGNED
                        ) AS totalSales,

                        COUNT(o.order_id)
                            AS orderCount,

                        CAST(
                            COALESCE(
                                SUM(
                                    COALESCE(
                                        item_summary.sales_quantity,
                                        0
                                    )
                                ),
                                0
                            ) AS SIGNED
                        ) AS salesQuantity

                    FROM orders o

                    INNER JOIN (
                        SELECT
                            order_id,
                            SUM(final_amount) AS paid_amount,
                            MIN(payment_date) AS paid_at

                        FROM payments

                        WHERE payment_status = 'PAID'

                        GROUP BY order_id
                    ) paid
                        ON paid.order_id = o.order_id

                    LEFT JOIN (
                        SELECT
                            order_id,
                            SUM(quantity) AS sales_quantity

                        FROM order_items

                        GROUP BY order_id
                    ) item_summary
                        ON item_summary.order_id = o.order_id

                    WHERE o.order_status <> 'CANCELED'

                      AND paid.paid_at >= :startDate

                      AND paid.paid_at < :endDate

                      AND (
                          :storeId IS NULL
                          OR o.store_id = :storeId
                      )

                    GROUP BY
                        DATE_FORMAT(
                            paid.paid_at,
                            '%Y-%m'
                        )

                    ORDER BY
                        DATE_FORMAT(
                            paid.paid_at,
                            '%Y-%m'
                        )
                    """,
            nativeQuery = true
    )
    List<TrendProjection> findMonthlyTrend(
            @Param("startDate")
            LocalDateTime startDate,

            @Param("endDate")
            LocalDateTime endDate,

            @Param("storeId")
            Integer storeId
    );

    // ====================================================
    // 연도별 매출 추이
    // ====================================================

    @Query(
            value = """
                    SELECT
                        DATE_FORMAT(
                            paid.paid_at,
                            '%Y'
                        ) AS periodLabel,

                        CAST(
                            COALESCE(
                                SUM(paid.paid_amount),
                                0
                            ) AS SIGNED
                        ) AS totalSales,

                        COUNT(o.order_id)
                            AS orderCount,

                        CAST(
                            COALESCE(
                                SUM(
                                    COALESCE(
                                        item_summary.sales_quantity,
                                        0
                                    )
                                ),
                                0
                            ) AS SIGNED
                        ) AS salesQuantity

                    FROM orders o

                    INNER JOIN (
                        SELECT
                            order_id,
                            SUM(final_amount) AS paid_amount,
                            MIN(payment_date) AS paid_at

                        FROM payments

                        WHERE payment_status = 'PAID'

                        GROUP BY order_id
                    ) paid
                        ON paid.order_id = o.order_id

                    LEFT JOIN (
                        SELECT
                            order_id,
                            SUM(quantity) AS sales_quantity

                        FROM order_items

                        GROUP BY order_id
                    ) item_summary
                        ON item_summary.order_id = o.order_id

                    WHERE o.order_status <> 'CANCELED'

                      AND paid.paid_at >= :startDate

                      AND paid.paid_at < :endDate

                      AND (
                          :storeId IS NULL
                          OR o.store_id = :storeId
                      )

                    GROUP BY
                        DATE_FORMAT(
                            paid.paid_at,
                            '%Y'
                        )

                    ORDER BY
                        DATE_FORMAT(
                            paid.paid_at,
                            '%Y'
                        )
                    """,
            nativeQuery = true
    )
    List<TrendProjection> findYearlyTrend(
            @Param("startDate")
            LocalDateTime startDate,

            @Param("endDate")
            LocalDateTime endDate,

            @Param("storeId")
            Integer storeId
    );

    // ====================================================
    // 지점별 매출 순위
    // ====================================================

    @Query(
            value = """
                    SELECT
                        s.store_id
                            AS storeId,

                        s.store_name
                            AS storeName,

                        CAST(
                            COALESCE(
                                SUM(paid.paid_amount),
                                0
                            ) AS SIGNED
                        ) AS totalSales,

                        COUNT(o.order_id)
                            AS orderCount,

                        CAST(
                            COALESCE(
                                SUM(
                                    COALESCE(
                                        item_summary.sales_quantity,
                                        0
                                    )
                                ),
                                0
                            ) AS SIGNED
                        ) AS salesQuantity

                    FROM orders o

                    INNER JOIN stores s
                        ON s.store_id = o.store_id

                    INNER JOIN (
                        SELECT
                            order_id,
                            SUM(final_amount) AS paid_amount,
                            MIN(payment_date) AS paid_at

                        FROM payments

                        WHERE payment_status = 'PAID'

                        GROUP BY order_id
                    ) paid
                        ON paid.order_id = o.order_id

                    LEFT JOIN (
                        SELECT
                            order_id,
                            SUM(quantity) AS sales_quantity

                        FROM order_items

                        GROUP BY order_id
                    ) item_summary
                        ON item_summary.order_id = o.order_id

                    WHERE o.order_status <> 'CANCELED'

                      AND paid.paid_at >= :startDate

                      AND paid.paid_at < :endDate

                      AND (
                          :storeId IS NULL
                          OR o.store_id = :storeId
                      )

                    GROUP BY
                        s.store_id,
                        s.store_name

                    ORDER BY
                        totalSales DESC,
                        orderCount DESC
                    """,
            nativeQuery = true
    )
    List<StoreSalesProjection> findStoreSalesRanking(
            @Param("startDate")
            LocalDateTime startDate,

            @Param("endDate")
            LocalDateTime endDate,

            @Param("storeId")
            Integer storeId,

            Pageable pageable
    );

    // ====================================================
    // 상품별 판매 순위
    // ====================================================

    /*
     * 상품 매출은 다음 기준입니다.
     *
     * unit_price × quantity
     *
     * 쿠폰 할인이나 포인트 사용 금액은
     * 상품별로 분배되지 않기 때문에 포함하지 않습니다.
     */
    @Query(
            value = """
                    SELECT
                        p.product_id
                            AS productId,

                        p.product_name
                            AS productName,

                        CAST(
                            COALESCE(
                                SUM(
                                    oi.unit_price
                                    * oi.quantity
                                ),
                                0
                            ) AS SIGNED
                        ) AS salesAmount,

                        CAST(
                            COALESCE(
                                SUM(oi.quantity),
                                0
                            ) AS SIGNED
                        ) AS salesQuantity,

                        COUNT(
                            DISTINCT o.order_id
                        ) AS orderCount

                    FROM orders o

                    INNER JOIN (
                        SELECT
                            order_id,
                            MIN(payment_date) AS paid_at

                        FROM payments

                        WHERE payment_status = 'PAID'

                        GROUP BY order_id
                    ) paid
                        ON paid.order_id = o.order_id

                    INNER JOIN order_items oi
                        ON oi.order_id = o.order_id

                    INNER JOIN products p
                        ON p.product_id = oi.product_id

                    WHERE o.order_status <> 'CANCELED'

                      AND paid.paid_at >= :startDate

                      AND paid.paid_at < :endDate

                      AND (
                          :storeId IS NULL
                          OR o.store_id = :storeId
                      )

                    GROUP BY
                        p.product_id,
                        p.product_name

                    ORDER BY
                        salesQuantity DESC,
                        salesAmount DESC
                    """,
            nativeQuery = true
    )
    List<ProductSalesProjection> findProductSalesRanking(
            @Param("startDate")
            LocalDateTime startDate,

            @Param("endDate")
            LocalDateTime endDate,

            @Param("storeId")
            Integer storeId,

            Pageable pageable
    );
}