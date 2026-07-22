package com.kiosk.customer.call.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kiosk.customer.call.dto.CallRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/calls")
@RequiredArgsConstructor
public class CallController {

    private final SimpMessagingTemplate messagingTemplate;

    @PostMapping
    public ResponseEntity<String> callStaff(@RequestBody CallRequest request) {
        log.info("직원 호출 요청 수신: 지점 ID = {}, 키오스크 = {}, 사유 = {}", 
                 request.getStoreId(), request.getKioskNo(), request.getReason());

        if (request.getStoreId() == null) {
            return ResponseEntity.badRequest().body("지점 정보가 없습니다.");
        }

        // 해당 지점을 구독 중인 관리자들에게 메시지 브로드캐스트
        String destination = "/topic/stores/" + request.getStoreId() + "/calls";
        messagingTemplate.convertAndSend(destination, request);

        return ResponseEntity.ok("직원 호출이 완료되었습니다.");
    }
}
