package com.kiosk.branch.kiosk.controller;

import com.kiosk.branch.kiosk.dto.KioskBannerResponse;
import com.kiosk.branch.kiosk.dto.KioskBannerUpdateRequest;
import com.kiosk.branch.kiosk.service.KioskBannerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/kiosk-banners")
@RequiredArgsConstructor
public class KioskBannerController {

    private final KioskBannerService kioskBannerService;

    @GetMapping("/{kioskId}")
    public ResponseEntity<KioskBannerResponse> getKioskBanner(@PathVariable Integer kioskId) {
        KioskBannerResponse response = kioskBannerService.getKioskBanner(kioskId);
        if (response == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{kioskId}")
    public ResponseEntity<Void> updateKioskBanner(
            @PathVariable Integer kioskId,
            @RequestBody KioskBannerUpdateRequest request) {
        kioskBannerService.updateKioskBanner(kioskId, request);
        return ResponseEntity.ok().build();
    }

    @GetMapping(value = "/stream/{kioskId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamKioskBanner(@PathVariable Integer kioskId) {
        return kioskBannerService.subscribeToBannerUpdates(kioskId);
    }
}
