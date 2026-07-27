package com.kiosk.branch.order.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.kiosk.entity.Order;

/**
 * [코드 흐름 안내] BranchOrderMapper
 *
 * <p>역할: 지점 운영의 주문 데이터를 조회하거나 저장하는 DB 접근 계층이다.</p>
 * <p>호출 흐름: Service -> 이 Mapper(ORDERS) -> MySQL -> 조회/변경 결과 반환 순서로 동작한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
public interface BranchOrderMapper extends JpaRepository<Order, Integer> {

    @Query("""
            select distinct o
            from Order o
            left join fetch o.payment
            left join fetch o.orderItems oi
            left join fetch oi.product
            where o.id = :id
            """)
    Optional<Order> findWithItemsById(@Param("id") Integer id);

    // 특정 지점의 주문 조회
    @Query("""
            select o
            from Order o
            left join fetch o.payment
            where o.store.id = :storeId
            order by o.createdAt desc
            """)
    List<Order> findByStore_IdOrderByCreatedAtDesc(@Param("storeId") Integer storeId);

    // 결제가 없고 WAITING 상태인 주문 조회
    List<Order> findByPaymentIsNull();

}