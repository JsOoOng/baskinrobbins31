package com.kiosk.branch.order.service;


import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kiosk.branch.order.dto.OrderDetailResponse;
import com.kiosk.branch.order.dto.OrderListResponse;
import com.kiosk.branch.order.repository.OrderMapper;
import com.kiosk.branch.order.repository.OrderStatusHistoryMapper;
import com.kiosk.entity.Order;
import com.kiosk.entity.OrderStatusHistory;
import com.kiosk.entity.enums.OrderStatus;

import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {


    private final OrderMapper orderMapper;
    private final OrderStatusHistoryMapper orderStatusHistoryMapper;



    public List<OrderListResponse> getOrders(Integer storeId){


        List<Order> orders =
                orderMapper
                .findByStoreStoreIdOrderByCreatedAtDesc(storeId);



        return orders.stream()
                .map(OrderListResponse::from)
                .toList();

    }
    
    @Transactional(readOnly = true)
    public OrderDetailResponse getOrderDetail(Integer orderId) {


        Order order =
            orderMapper.findWithItemsByOrderId(orderId)
            .orElseThrow(
                () -> new RuntimeException("주문 없음")
            );


        List<OrderDetailResponse.ItemResponse> items =
            order.getOrderItems()
            .stream()
            .map(item ->
                OrderDetailResponse.ItemResponse.builder()
                .productName(
                    item.getProduct().getProductName()
                )
                .quantity(
                    item.getQuantity()
                )
                .unitPrice(
                    item.getUnitPrice()
                )
                .build()
            )
            .toList();



        return OrderDetailResponse.builder()
                .orderId(order.getId())
                .orderNumber(order.getOrderNumber())
                .orderType(
                    order.getOrderType().name()
                )
                .orderStatus(
                    order.getOrderStatus().name()
                )
                .totalPrice(
                    order.getTotalPrice()
                )
                .items(items)
                .build();

    }
    
    @Transactional
    public void changeStatus(
            Integer orderId,
            String status
    ){

        Order order =
            orderMapper.findById(orderId)
            .orElseThrow(
                () -> new RuntimeException("주문 없음")
            );


        OrderStatus newStatus =
            OrderStatus.valueOf(status);


        order.changeOrderStatus(newStatus);


        OrderStatusHistory history =
            OrderStatusHistory.builder()
            .order(order)
            .store(order.getStore())
            .orderStatus(newStatus)
            .build();


        orderStatusHistoryMapper.save(history);

    }
    
    

}
