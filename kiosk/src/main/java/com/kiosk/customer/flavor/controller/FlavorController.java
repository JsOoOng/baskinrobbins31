package com.kiosk.customer.flavor.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kiosk.customer.flavor.service.FlavorService;

import lombok.RequiredArgsConstructor;

/**
 * [코드 흐름 안내] FlavorController
 *
 * <p>역할: 고객 키오스크의 아이스크림 맛 HTTP 요청을 받는 진입점이다.</p>
 * <p>호출 흐름: Vue/API 요청 -> 이 컨트롤러(/api/v1/stores/{storeId}/flavors) -> FlavorService -> 응답 DTO 또는 JSON -> 화면 갱신 순서로 이동한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
@RestController
@RequestMapping("/api/v1/stores/{storeId}/flavors")
@RequiredArgsConstructor
public class FlavorController {

    private final FlavorService flavorService;

    // 특정 지점에서 현재 판매 가능한(품절되지 않은) 아이스크림 맛 목록 조회
    /**
     * [요청 흐름] GET /api/v1/stores/{storeId}/flavors
     * 프론트 요청을 받아 getAvailableFlavors() 메서드가 입력을 받고 FlavorService 호출 후 결과를 응답한다.
     */
    @GetMapping
    public ResponseEntity<Object> getAvailableFlavors(@PathVariable Long storeId) {
        return ResponseEntity.ok(flavorService.getAvailableFlavorsByStore(storeId));
    }
}