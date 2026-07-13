package com.kiosk.branch.order.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.kiosk.entity.Order;


public interface BranchOrderMapper extends JpaRepository<Order, Integer> {

	@Query("""
			select distinct o
			from Order o
			left join fetch o.orderItems oi
			left join fetch oi.product
			where o.id = :id
			""")
			Optional<Order> findWithItemsById(@Param("id") Integer id);
	
    // 특정 지점의 주문 조회
    List<Order> findByStore_IdOrderByCreatedAtDesc(Integer storeId);

}