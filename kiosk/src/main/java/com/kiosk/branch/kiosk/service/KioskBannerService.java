package com.kiosk.branch.kiosk.service;

import com.kiosk.branch.kiosk.dto.KioskBannerResponse;
import com.kiosk.branch.kiosk.dto.KioskBannerUpdateRequest;
import com.kiosk.branch.kiosk.repository.BannerRepository;
import com.kiosk.branch.kiosk.repository.BranchKioskRepository;
import com.kiosk.branch.kiosk.repository.KioskBannerRepository;
import com.kiosk.entity.Banner;
import com.kiosk.entity.Kiosk;
import com.kiosk.entity.KioskBanner;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * [코드 흐름 안내] KioskBannerService
 *
 * <p>역할: 지점 운영의 키오스크 배너 업무 규칙과 상태 변경을 처리한다.</p>
 * <p>호출 흐름: Controller 호출 -> 이 서비스 -> KioskBannerRepository, BranchKioskRepository, BannerRepository, Map -> Entity/DTO 변환 -> Controller 반환 순서로 동작한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
@Service
@RequiredArgsConstructor
public class KioskBannerService {

    private final KioskBannerRepository kioskBannerRepository;
    private final BranchKioskRepository kioskRepository;
    private final BannerRepository bannerRepository;

    // 키오스크 ID를 키로 하여 SseEmitter 보관
    private final Map<Integer, List<SseEmitter>> emitters = new ConcurrentHashMap<>();

    @Transactional(readOnly = true)
    /**
     * [메서드 흐름] getKioskBanner
     * Controller 또는 상위 서비스에서 호출되어 KioskBannerRepository, BranchKioskRepository, BannerRepository 등을 사용해 검증·조회·저장 등의 처리를 수행하고 결과를 반환한다.
     */
    public KioskBannerResponse getKioskBanner(Integer kioskId) {
        KioskBanner kb = kioskBannerRepository.findByKioskId(kioskId).orElse(null);
        if (kb == null) {
            return null; // 배너가 매핑되어 있지 않음
        }
        return KioskBannerResponse.builder()
                .kioskId(kioskId)
                .bannerId(kb.getBanner().getId())
                .bannerTitle(kb.getBanner().getTitle())
                .bannerImageUrl(kb.getBanner().getImageUrl())
                .build();
    }

    @Transactional
    /**
     * [메서드 흐름] updateKioskBanner
     * Controller 또는 상위 서비스에서 호출되어 KioskBannerRepository, BranchKioskRepository, BannerRepository 등을 사용해 검증·조회·저장 등의 처리를 수행하고 결과를 반환한다.
     */
    public void updateKioskBanner(Integer kioskId, KioskBannerUpdateRequest request) {
        if (request.getBannerId() == null || request.getBannerId() == 0) {
            kioskBannerRepository.findByKioskId(kioskId).ifPresent(kioskBannerRepository::delete);
            notifyKiosk(kioskId, "DEFAULT");
            return;
        }

        Kiosk kiosk = kioskRepository.findById(kioskId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid kiosk ID"));
        Banner banner = bannerRepository.findById(request.getBannerId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid banner ID"));

        KioskBanner kb = kioskBannerRepository.findByKioskId(kioskId)
                .orElseGet(() -> KioskBanner.builder()
                        .kiosk(kiosk)
                        .banner(banner)
                        .build());
        
        kb.updateBanner(banner);
        kioskBannerRepository.save(kb);

        // SSE를 통해 해당 키오스크에 알림 전송
        notifyKiosk(kioskId, banner.getImageUrl());
    }

    /**
     * [메서드 흐름] subscribeToBannerUpdates
     * Controller 또는 상위 서비스에서 호출되어 KioskBannerRepository, BranchKioskRepository, BannerRepository 등을 사용해 검증·조회·저장 등의 처리를 수행하고 결과를 반환한다.
     */
    public SseEmitter subscribeToBannerUpdates(Integer kioskId) {
        // 타임아웃 30분
        SseEmitter emitter = new SseEmitter(30 * 60 * 1000L);
        emitters.computeIfAbsent(kioskId, k -> new java.util.concurrent.CopyOnWriteArrayList<>()).add(emitter);

        emitter.onCompletion(() -> removeEmitter(kioskId, emitter));
        emitter.onTimeout(() -> removeEmitter(kioskId, emitter));
        emitter.onError((e) -> removeEmitter(kioskId, emitter));

        try {
            // 연결 확인용 더미 이벤트
            emitter.send(SseEmitter.event().name("INIT").data("Connected for kiosk " + kioskId));
        } catch (IOException e) {
            removeEmitter(kioskId, emitter);
        }

        return emitter;
    }

    private void removeEmitter(Integer kioskId, SseEmitter emitter) {
        List<SseEmitter> kioskEmitters = emitters.get(kioskId);
        if (kioskEmitters != null) {
            kioskEmitters.remove(emitter);
        }
    }

    private void notifyKiosk(Integer kioskId, String newImageUrl) {
        List<SseEmitter> kioskEmitters = emitters.get(kioskId);
        if (kioskEmitters != null) {
            for (SseEmitter emitter : kioskEmitters) {
                try {
                    emitter.send(SseEmitter.event().name("BANNER_UPDATE").data(newImageUrl));
                } catch (IOException e) {
                    kioskEmitters.remove(emitter);
                }
            }
        }
    }
}
