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