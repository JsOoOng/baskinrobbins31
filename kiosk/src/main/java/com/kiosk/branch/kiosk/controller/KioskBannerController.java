package com.kiosk.branch.kiosk.controller;

import com.kiosk.branch.kiosk.dto.KioskBannerResponse;
import com.kiosk.branch.kiosk.dto.KioskBannerUpdateRequest;
import com.kiosk.branch.kiosk.service.KioskBannerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * [코드 흐름 안내] KioskBannerController
 *
 * <p>역할: 지점 운영의 키오스크 배너 HTTP 요청을 받는 진입점이다.</p>
 * <p>호출 흐름: Vue/API 요청 -> 이 컨트롤러(/api/kiosk-banners) -> KioskBannerService -> 응답 DTO 또는 JSON -> 화면 갱신 순서로 이동한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
@RestController
@RequestMapping("/api/kiosk-banners")
@RequiredArgsConstructor
public class KioskBannerController {

    private final KioskBannerService kioskBannerService;

    /**
     * [요청 흐름] GET /api/kiosk-banners/{kioskId}
     * 프론트 요청을 받아 getKioskBanner() 메서드가 입력을 받고 KioskBannerService 호출 후 결과를 응답한다.
     */
    @GetMapping("/{kioskId}")
    public ResponseEntity<KioskBannerResponse> getKioskBanner(@PathVariable Integer kioskId) {
        KioskBannerResponse response = kioskBannerService.getKioskBanner(kioskId);
        if (response == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(response);
    }

    /**
     * [요청 흐름] PUT /api/kiosk-banners/{kioskId}
     * 프론트 요청을 받아 updateKioskBanner() 메서드가 입력을 받고 KioskBannerService 호출 후 결과를 응답한다.
     */
    @PutMapping("/{kioskId}")
    public ResponseEntity<Void> updateKioskBanner(
            @PathVariable Integer kioskId,
            @RequestBody KioskBannerUpdateRequest request) {
        kioskBannerService.updateKioskBanner(kioskId, request);
        return ResponseEntity.ok().build();
    }

    /**
     * [요청 흐름] GET /api/kiosk-banners/stream/{kioskId}
     * 프론트 요청을 받아 streamKioskBanner() 메서드가 입력을 받고 KioskBannerService 호출 후 결과를 응답한다.
     */
    @GetMapping(value = "/stream/{kioskId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamKioskBanner(@PathVariable Integer kioskId) {
        return kioskBannerService.subscribeToBannerUpdates(kioskId);
    }
}
