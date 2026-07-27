package com.kiosk.customer.order.repository;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.kiosk.customer.order.dto.OrderResponse;
import com.kiosk.customer.order.dto.Payment;

/**
 * [코드 흐름 안내] OrderMapper
 *
 * <p>역할: 고객 키오스크의 주문 데이터를 조회하거나 저장하는 DB 접근 계층이다.</p>
 * <p>호출 흐름: Service -> 이 Mapper -> MySQL -> 조회/변경 결과 반환 순서로 동작한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
@Mapper
public interface OrderMapper {
    
    // 통합: 주문 정보 + 상품 목록 + 맛 정보를 한 번에 조회
    OrderResponse selectOrderWithDetails(int orderId);
    
    // 재고 차감 (필요)
    int decreaseProductStock(
        @Param("productId") int productId, 
        @Param("quantity") int quantity
    );

    // 주문 상태 변경 (필수)
    int updateOrderStatus(
    		@Param("orderId") int orderId, 
    		@Param("status") String status
    );
    
    
	 // 결제 정보 저장
	 void insertPayment(Payment payment);
	
	 void updatePaymentStatus(@Param("orderId") int orderId, 
             @Param("paymentMethod") String paymentMethod, 
             @Param("finalAmount") int finalAmount);
	 
	 int selectProductStock(@Param("productId") int productId);
}


