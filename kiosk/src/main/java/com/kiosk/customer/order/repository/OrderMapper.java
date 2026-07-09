package com.kiosk.customer.order.repository;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.kiosk.customer.order.dto.OrderItemDTO;
import com.kiosk.customer.order.dto.OrderResponse;
import com.kiosk.entity.OrderItem;

@Mapper
public interface OrderMapper {

    // 주문 상세 조회
    OrderResponse selectOrderById(@Param("orderId") int orderId);

    // 주문 상품 목록
    List<OrderItemDTO> selectOrderItemsByOrderId(@Param("orderId") int orderId);

    // 재고 차감
    void decreaseProductStock(
        @Param("productId") int productId,
        @Param("quantity") int quantity
    );

    // 주문 상태 변경
    void updateOrderStatus(
        @Param("orderId") int orderId,
        @Param("status") String status
    );
}