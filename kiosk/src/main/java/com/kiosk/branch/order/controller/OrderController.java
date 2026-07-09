package com.kiosk.branch.order.controller;


import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kiosk.branch.order.dto.OrderDetailResponse;
import com.kiosk.branch.order.dto.OrderListResponse;
import com.kiosk.branch.order.dto.OrderStatusRequest;
import com.kiosk.branch.order.service.OrderService;

import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping("/branch/order")
@RequiredArgsConstructor
public class OrderController {



    private final OrderService orderService;



    /**
     * 지점 주문 목록 조회
     */
    @GetMapping("/{storeId}")
    public List<OrderListResponse> getOrders(
            @PathVariable Integer storeId
    ){

        return orderService.getOrders(storeId);

    }
    
    @GetMapping("/detail/{orderId}")
    public OrderDetailResponse detail(
            @PathVariable Integer orderId
    ){

        return orderService.getOrderDetail(orderId);

    }
    
    @PatchMapping("/{orderId}/status")
    public String changeStatus(
            @PathVariable Integer orderId,
            @RequestBody OrderStatusRequest request
    ){

        orderService.changeStatus(
            orderId,
            request.getStatus()
        );


        return "주문 상태 변경 완료";

    }


}