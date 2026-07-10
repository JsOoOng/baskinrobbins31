package com.kiosk.customer.flavor.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kiosk.customer.flavor.service.FlavorService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/stores/{storeId}/flavors")
@RequiredArgsConstructor
public class FlavorController {

    private final FlavorService flavorService;

    // 특정 지점에서 현재 판매 가능한(품절되지 않은) 아이스크림 맛 목록 조회
    @GetMapping
    public ResponseEntity<Object> getAvailableFlavors(@PathVariable Long storeId) {
        return ResponseEntity.ok(flavorService.getAvailableFlavorsByStore(storeId));
    }
    
    
}