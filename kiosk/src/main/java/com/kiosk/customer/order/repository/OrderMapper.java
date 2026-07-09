package com.kiosk.customer.order.repository;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.kiosk.customer.order.dto.OrderItem;
import com.kiosk.customer.order.dto.OrderResponse;

@Mapper
public interface OrderMapper {

    // 1. 주문 상세 정보 조회 (총금액 등을 포함)
    OrderResponse selectOrderById(int orderId);

    // 2. 특정 주문의 상품 목록 조회
    List<OrderItem> selectOrderItemsByOrderId(int orderId);

    // 3. 상품 ID와 수량으로 재고 차감
    void decreaseProductStock(@Param("productId") int productId, @Param("quantity") int quantity);

    // 4. 주문 상태 업데이트 (결제 완료 시 'COMPLETED' 등으로 변경)
    void updateOrderStatus(@Param("orderId") int orderId, @Param("status") String status);
}