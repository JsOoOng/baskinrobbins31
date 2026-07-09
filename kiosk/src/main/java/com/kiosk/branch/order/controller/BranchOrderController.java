package com.kiosk.branch.order.controller;


import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kiosk.branch.order.dto.BranchOrderDetailResponse;
import com.kiosk.branch.order.dto.BranchOrderListResponse;
import com.kiosk.branch.order.dto.BranchOrderStatusRequest;
import com.kiosk.branch.order.service.BranchOrderService;

import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping("/branch/order")
@RequiredArgsConstructor
public class BranchOrderController {



    private final BranchOrderService orderService;



    /**
     * 지점 주문 목록 조회
     */
    @GetMapping("/{storeId}")
    public List<BranchOrderListResponse> getOrders(
            @PathVariable Integer storeId
    ){

        return orderService.getOrders(storeId);

    }
    
    @GetMapping("/detail/{orderId}")
    public BranchOrderDetailResponse detail(
            @PathVariable Integer orderId
    ){

        return orderService.getOrderDetail(orderId);

    }
    
    @PatchMapping("/{orderId}/status")
    public String changeStatus(
            @PathVariable Integer orderId,
            @RequestBody BranchOrderStatusRequest request
    ){

        orderService.changeStatus(
            orderId,
            request.getStatus()
        );


        return "주문 상태 변경 완료";

    }


}