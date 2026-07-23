package com.kiosk.customer.order.controller;

import java.util.Base64;
import java.util.Map;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.kiosk.customer.order.dto.OrderCreateRequest;
import com.kiosk.customer.order.dto.OrderResponse;
import com.kiosk.customer.order.dto.TossConfirmRequest;
import com.kiosk.customer.order.repository.OrderMapper;
import com.kiosk.customer.order.service.OrderService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

/**
 * [코드 흐름 안내] OrderController
 *
 * <p>역할: 고객 키오스크의 주문 HTTP 요청을 받는 진입점이다.</p>
 * <p>호출 흐름: Vue/API 요청 -> 이 컨트롤러(/api/orders) -> OrderService, OrderMapper -> 응답 DTO 또는 JSON -> 화면 갱신 순서로 이동한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders")
public class OrderController {
	
	private final OrderService orderService;
	private final OrderMapper orderMapper;

    // 0. 결제 버튼 클릭 시 장바구니 데이터를 실제 주문 DB에 저장
    /**
     * [요청 흐름] POST /api/orders
     * 프론트 요청을 받아 createOrder() 메서드가 입력을 받고 OrderService, OrderMapper 호출 후 결과를 응답한다.
     */
    @PostMapping
    public ResponseEntity<Integer> createOrder(@RequestBody OrderCreateRequest request, HttpSession session) {
        // 프론트엔드에서 넘어오지 않은 값이 있다면 기본값 세팅
        if (request.getKioskId() == null) request.setKioskId(1);
        if (request.getStoreId() == null) request.setStoreId(1);
        if (request.getDryIceCount() == null) request.setDryIceCount(0);
        if (request.getDryIceMins() == null) request.setDryIceMins(0);
        if (request.getOrderType() == null) request.setOrderType("TOGO");
        
        int orderId = orderService.createOrder(request, session);
        return ResponseEntity.ok(orderId);
    }
    
    // 1. 주문 내역 및 총금액 조회
    /**
     * [요청 흐름] GET /api/orders/{orderId}
     * 프론트 요청을 받아 getOrderDetails() 메서드가 입력을 받고 OrderService, OrderMapper 호출 후 결과를 응답한다.
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrderDetails(@PathVariable("orderId") int orderId) {
        OrderResponse response = orderService.getOrderDetails(orderId);
        
        if (response == null) {
            return ResponseEntity.notFound().build(); // 주문이 없을 경우 404 처리
        }
        
        return ResponseEntity.ok(response); // 주문이 있으면 데이터와 함께 200 반환
    }

    /**
     * [요청 흐름] POST /api/orders/{orderId}/pay
     * 프론트 요청을 받아 completePayment() 메서드가 입력을 받고 OrderService, OrderMapper 호출 후 결과를 응답한다.
     */
    @PostMapping("/{orderId}/pay")
    public ResponseEntity<String> completePayment(
            @PathVariable("orderId") int orderId, 
            @RequestBody Map<String, Object> request,
            HttpSession session) {

        // 1. 결제 수단 추출
        String paymentMethod = (String) request.get("paymentMethod");
        
        // 2. ⭐ 쿠폰 ID 안전하게 추출 (Type Cast 에러 방지)
        int userCouponId = 0;
        if (request.containsKey("userCouponId") && request.get("userCouponId") != null) {
            userCouponId = Integer.parseInt(String.valueOf(request.get("userCouponId")));
        }
        
        // 3. 포인트 관련 추출 
        boolean usePoints = request.containsKey("usePoints") && (boolean) request.get("usePoints");
        int pointUsed = request.containsKey("pointUsed") && request.get("pointUsed") != null 
                        ? Integer.parseInt(String.valueOf(request.get("pointUsed"))) 
                        : 0;

        // 4. 서비스로 통합 전달
        orderService.processPayment(orderId, paymentMethod, userCouponId, pointUsed, session);
        
        return ResponseEntity.ok("결제 및 재고 차감 완료");
    }
    
    /**
     * [요청 흐름] POST /api/orders/{orderId}/cancel
     * 프론트 요청을 받아 cancelOrder() 메서드가 입력을 받고 OrderService, OrderMapper 호출 후 결과를 응답한다.
     */
    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<String> cancelOrder(@PathVariable("orderId") int orderId) {
        // 주문 상태를 CANCELED로 변경하는 서비스 호출
        orderService.cancelOrder(orderId);
        
        return ResponseEntity.ok("주문 취소 완료");
    }

    @org.springframework.beans.factory.annotation.Value("${toss.secret-key}")
    private String tossSecretKey;

    @org.springframework.beans.factory.annotation.Value("${server.port:8889}")
    private String serverPort;
    
    @org.springframework.beans.factory.annotation.Value("${server.printPort:8888}")
    private String printServerPort;

    /**
     * [요청 흐름] POST /api/orders/toss/confirm
     * 프론트 요청을 받아 confirmTossPayment() 메서드가 입력을 받고 OrderService, OrderMapper 호출 후 결과를 응답한다.
     */
    @PostMapping("/toss/confirm")
    public ResponseEntity<String> confirmTossPayment(@RequestBody TossConfirmRequest tossReq, HttpSession session) {
        String tossUrl = "https://api.tosspayments.com/v1/payments/confirm";
        
        RestTemplate restTemplate = new RestTemplate();
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String encodedAuth = Base64.getEncoder().encodeToString((tossSecretKey + ":").getBytes());
        headers.set("Authorization", "Basic " + encodedAuth);
        
        HttpEntity<TossConfirmRequest> entity = new HttpEntity<>(tossReq, headers);
        
        try {
            // 토스 페이먼츠 승인 서버로 실결제 요청 전송
            ResponseEntity<String> response = restTemplate.postForEntity(tossUrl, entity, String.class);
            
            // 승인이 완료되면 실제 비즈니스 로직(재고 차감, 주문 완료) 처리
            String realOrderIdStr = tossReq.getOrderId().replace("kiosk_order_", "");
            int orderId = Integer.parseInt(realOrderIdStr);
            orderService.processPayment(orderId, "TOSS", 0, tossReq.getPointUsed(), session);
            
            // 영수증 데이터 생성 후 로컬 /receipt API 호출
            try {
                OrderResponse orderRes = orderService.getOrderDetails(orderId);
                String orderNo = String.format("%03d", orderRes.getOrderNumber());
                String orderItem = orderRes.getOrderItems().isEmpty() ? "상품없음" : orderRes.getOrderItems().get(0).getProductName();
                if (orderRes.getOrderItems().size() > 1) {
                    orderItem += " 외 " + (orderRes.getOrderItems().size() - 1) + "건";
                }
                String price = orderRes.getTotalPrice() + "원";
                String orderDate = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy년MM월dd일"));

                HttpHeaders receiptHeaders = new HttpHeaders();
                receiptHeaders.setContentType(MediaType.APPLICATION_JSON);
                String receiptJson = String.format("{\"orderNo\":\"%s\",\"orderItem\":\"%s\",\"price\":\"%s\",\"orderDate\":\"%s\"}", 
                        orderNo, orderItem, price, orderDate);
                HttpEntity<String> receiptEntity = new HttpEntity<>(receiptJson, receiptHeaders);
                
                // 포트 동적 참조
                restTemplate.postForEntity("http://172.16.15.97:" + this.printServerPort + "/receipt", receiptEntity, String.class);
            } catch (Exception ex) {
                System.err.println("영수증 출력 요청 중 오류 (결제는 성공): " + ex.getMessage());
            }

            return ResponseEntity.ok("토스 결제 승인 및 주문 처리 완료");
        } catch (org.springframework.web.client.HttpStatusCodeException e) {
            System.err.println("토스 승인 실패 (HTTP 상태 코드): " + e.getStatusCode());
            System.err.println("토스 에러 응답: " + e.getResponseBodyAsString());
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        } catch (Exception e) {
            System.err.println("토스 승인 중 서버 내부 오류: " + e.getMessage());
            return ResponseEntity.status(500).body("{\"error\": \"서버 내부 오류: " + e.getMessage() + "\"}");
        }
    }

    /**
     * [요청 흐름] POST /api/orders/receipt
     * 프론트 요청을 받아 printReceipt() 메서드가 입력을 받고 OrderService, OrderMapper 호출 후 결과를 응답한다.
     */
    @PostMapping("/receipt")
    public ResponseEntity<String> printReceipt(@RequestBody java.util.Map<String, String> receiptData) {
        System.out.println("===============================================");
        System.out.println("                영수증 출력 완료                 ");
        System.out.println("===============================================");
        System.out.println("주문번호: " + receiptData.get("orderNo"));
        System.out.println("주문상품: " + receiptData.get("orderItem"));
        System.out.println("결제금액: " + receiptData.get("price"));
        System.out.println("주문일자: " + receiptData.get("orderDate"));
        System.out.println("===============================================");
        return ResponseEntity.ok("영수증 출력 성공");
    }
}