package com.kiosk.branch.order.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kiosk.entity.OrderStatusHistory;


/**
 * [코드 흐름 안내] BranchOrderStatusHistoryMapper
 *
 * <p>역할: 지점 운영의 주문 상태 데이터를 조회하거나 저장하는 DB 접근 계층이다.</p>
 * <p>호출 흐름: Service -> 이 Mapper(ORDER_STATUS_HISTORY) -> MySQL -> 조회/변경 결과 반환 순서로 동작한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
public interface BranchOrderStatusHistoryMapper 
        extends JpaRepository<OrderStatusHistory, Integer> {

}