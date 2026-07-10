package com.kiosk.customer.order.repository;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.kiosk.customer.order.dto.OrderResponse;
import com.kiosk.customer.order.dto.Payment;

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
    void updateOrderStatus(
        @Param("orderId") int orderId, 
        @Param("status") String status
    );
    
	 // 결제 정보 저장
	 void insertPayment(Payment payment);
	
}


