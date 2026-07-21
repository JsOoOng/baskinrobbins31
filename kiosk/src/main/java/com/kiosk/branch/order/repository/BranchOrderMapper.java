package com.kiosk.branch.order.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.kiosk.entity.Order;
import com.kiosk.entity.enums.OrderStatus;

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