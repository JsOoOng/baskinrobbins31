package com.kiosk.headquarter.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kiosk.headquarter.dto.banner.HeadBannerActiveRequest;
import com.kiosk.headquarter.dto.banner.HeadBannerCreateRequest;
import com.kiosk.headquarter.dto.banner.HeadBannerResponse;
import com.kiosk.headquarter.dto.banner.HeadBannerUpdateRequest;
import com.kiosk.headquarter.service.HeadBannerService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * [코드 흐름 안내] HeadBannerController
 *
 * <p>역할: 본사 관리의 배너 HTTP 요청을 받는 진입점이다.</p>
 * <p>호출 흐름: Vue/API 요청 -> 이 컨트롤러(/head/banners) -> HeadBannerService -> 응답 DTO 또는 JSON -> 화면 갱신 순서로 이동한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
@RestController
@RequestMapping("/head/banners")
@RequiredArgsConstructor
public class HeadBannerController {

    private final HeadBannerService
            headBannerService;

    /*
     * 배너 목록
     *
     * GET /head/banners
     */
    /**
     * [요청 흐름] GET /head/banners
     * 프론트 요청을 받아 getBanners() 메서드가 입력을 받고 HeadBannerService 호출 후 결과를 응답한다.
     */
    @GetMapping
    public ResponseEntity<List<HeadBannerResponse>>
            getBanners() {

        return ResponseEntity.ok(
                headBannerService.getBanners()
        );
    }

    /*
     * 배너 상세
     *
     * GET /head/banners/{bannerId}
     */
    /**
     * [요청 흐름] GET /head/banners/{bannerId}
     * 프론트 요청을 받아 getBanner() 메서드가 입력을 받고 HeadBannerService 호출 후 결과를 응답한다.
     */
    @GetMapping("/{bannerId}")
    public ResponseEntity<HeadBannerResponse>
            getBanner(
                    @PathVariable
                    Integer bannerId
            ) {

        return ResponseEntity.ok(
                headBannerService.getBanner(
                        bannerId
                )
        );
    }

    /*
     * 배너 등록
     *
     * POST /head/banners
     */
    /**
     * [요청 흐름] POST /head/banners
     * 프론트 요청을 받아 createBanner() 메서드가 입력을 받고 HeadBannerService 호출 후 결과를 응답한다.
     */
    @PostMapping
    public ResponseEntity<HeadBannerResponse>
            createBanner(
                    @Valid
                    @RequestBody
                    HeadBannerCreateRequest request
            ) {

        HeadBannerResponse response =
                headBannerService.createBanner(
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    /*
     * 배너 수정
     *
     * PUT /head/banners/{bannerId}
     */
    /**
     * [요청 흐름] PUT /head/banners/{bannerId}
     * 프론트 요청을 받아 updateBanner() 메서드가 입력을 받고 HeadBannerService 호출 후 결과를 응답한다.
     */
    @PutMapping("/{bannerId}")
    public ResponseEntity<HeadBannerResponse>
            updateBanner(
                    @PathVariable
                    Integer bannerId,

                    @Valid
                    @RequestBody
                    HeadBannerUpdateRequest request
            ) {

        return ResponseEntity.ok(
                headBannerService.updateBanner(
                        bannerId,
                        request
                )
        );
    }

    /*
     * 배너 노출 변경
     *
     * PATCH /head/banners/{bannerId}/active
     */
    /**
     * [요청 흐름] PATCH /head/banners/{bannerId}/active
     * 프론트 요청을 받아 updateActive() 메서드가 입력을 받고 HeadBannerService 호출 후 결과를 응답한다.
     */
    @PatchMapping("/{bannerId}/active")
    public ResponseEntity<HeadBannerResponse>
            updateActive(
                    @PathVariable
                    Integer bannerId,

                    @Valid
                    @RequestBody
                    HeadBannerActiveRequest request
            ) {

        return ResponseEntity.ok(
                headBannerService.updateActive(
                        bannerId,
                        request
                )
        );
    }

    /*
     * 배너 삭제
     *
     * DELETE /head/banners/{bannerId}
     */
    /**
     * [요청 흐름] DELETE /head/banners/{bannerId}
     * 프론트 요청을 받아 deleteBanner() 메서드가 입력을 받고 HeadBannerService 호출 후 결과를 응답한다.
     */
    @DeleteMapping("/{bannerId}")
    public ResponseEntity<Void>
            deleteBanner(
                    @PathVariable
                    Integer bannerId
            ) {

        headBannerService.deleteBanner(
                bannerId
        );

        return ResponseEntity
                .noContent()
                .build();
    }
}