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


/**
 * [코드 흐름 안내] BranchOrderController
 *
 * <p>역할: 지점 운영의 주문 HTTP 요청을 받는 진입점이다.</p>
 * <p>호출 흐름: Vue/API 요청 -> 이 컨트롤러(/branch/order) -> BranchOrderService -> 응답 DTO 또는 JSON -> 화면 갱신 순서로 이동한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
@RestController
@RequestMapping("/branch/order")
@RequiredArgsConstructor
public class BranchOrderController {



    private final BranchOrderService orderService;



    /**
     * 지점 주문 목록 조회
     */
    /**
     * [요청 흐름] GET /branch/order/{storeId}
     * 프론트 요청을 받아 getOrders() 메서드가 입력을 받고 BranchOrderService 호출 후 결과를 응답한다.
     */
    @GetMapping("/{storeId}")
    public List<BranchOrderListResponse> getOrders(
            @PathVariable Integer storeId
    ){

        return orderService.getOrders(storeId);

    }
    
    
    /**
     * [요청 흐름] GET /branch/order/detail/{orderId}
     * 프론트 요청을 받아 detail() 메서드가 입력을 받고 BranchOrderService 호출 후 결과를 응답한다.
     */
    @GetMapping("/detail/{orderId}")
    public BranchOrderDetailResponse detail(
            @PathVariable Integer orderId
    ){

        return orderService.getOrderDetail(orderId);

    }
    
    
    /*
    
    @GetMapping("/detail/{orderId}")
    public BranchOrderDetailResponse detail(@PathVariable Integer orderId) {

        try {
            return orderService.getOrderDetail(orderId);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }*/
    
    /**
     * [요청 흐름] PATCH /branch/order/{orderId}/status
     * 프론트 요청을 받아 changeStatus() 메서드가 입력을 받고 BranchOrderService 호출 후 결과를 응답한다.
     */
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