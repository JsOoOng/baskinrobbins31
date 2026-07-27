package com.kiosk.customer.order.repository;

import com.kiosk.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

import org.springframework.data.jpa.repository.Query;

/**
 * [코드 흐름 안내] OrderRepository
 *
 * <p>역할: 고객 키오스크의 주문 데이터를 조회하거나 저장하는 DB 접근 계층이다.</p>
 * <p>호출 흐름: Service -> 이 Repository(ORDERS) -> MySQL -> 조회/변경 결과 반환 순서로 동작한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
public interface OrderRepository extends JpaRepository<Order, Integer> {

    @Query("SELECT COALESCE(MAX(o.orderNumber), 0) FROM Order o WHERE o.store.id = :storeId AND o.createdAt >= :startOfDay")
    Integer findMaxOrderNumberByStoreAndDate(@Param("storeId") Integer storeId, @Param("startOfDay") LocalDateTime startOfDay);

}