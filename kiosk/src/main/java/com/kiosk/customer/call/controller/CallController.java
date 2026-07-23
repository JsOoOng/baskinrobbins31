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

/**
 * [코드 흐름 안내] CallController
 *
 * <p>역할: 고객 키오스크의 직원 호출 HTTP 요청을 받는 진입점이다.</p>
 * <p>호출 흐름: Vue/API 요청 -> 이 컨트롤러(/api/calls) -> SimpMessagingTemplate -> 응답 DTO 또는 JSON -> 화면 갱신 순서로 이동한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
@Slf4j
@RestController
@RequestMapping("/api/calls")
@RequiredArgsConstructor
public class CallController {

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * [요청 흐름] POST /api/calls
     * 프론트 요청을 받아 callStaff() 메서드가 입력을 받고 SimpMessagingTemplate 호출 후 결과를 응답한다.
     */
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
