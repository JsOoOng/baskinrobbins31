package com.kiosk.branch.kiosk.controller;

import com.kiosk.branch.kiosk.repository.BannerRepository;
import com.kiosk.entity.Banner;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.time.LocalDateTime;

/**
 * [코드 흐름 안내] BannerController
 *
 * <p>역할: 지점 운영의 배너 HTTP 요청을 받는 진입점이다.</p>
 * <p>호출 흐름: Vue/API 요청 -> 이 컨트롤러(/api/banners) -> BannerRepository -> 응답 DTO 또는 JSON -> 화면 갱신 순서로 이동한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
@RestController
@RequestMapping("/api/banners")
@RequiredArgsConstructor
public class BannerController {

    private final BannerRepository bannerRepository;

    /**
     * [요청 흐름] GET /api/banners
     * 프론트 요청을 받아 getAllBanners() 메서드가 입력을 받고 BannerRepository 호출 후 결과를 응답한다.
     */
    @GetMapping
    public List<Banner> getAllBanners() {
        return bannerRepository.findVisibleAt(LocalDateTime.now());
    }
}
