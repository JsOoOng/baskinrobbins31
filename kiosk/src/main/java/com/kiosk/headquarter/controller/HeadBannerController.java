package com.kiosk.headquarter.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.kiosk.headquarter.dto.banner.HeadBannerCreateRequestDTO;
import com.kiosk.headquarter.dto.banner.HeadBannerResponseDTO;
import com.kiosk.headquarter.dto.banner.HeadBannerUpdateRequestDTO;
import com.kiosk.headquarter.service.HeadBannerService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class HeadBannerController {

    private final HeadBannerService headBannerService;

    // 배너 등록
    @PostMapping("/head/banners")
    public HeadBannerResponseDTO createBanner(
            @RequestBody HeadBannerCreateRequestDTO requestDTO) {

        return headBannerService.createBanner(requestDTO);
    }

    // 배너 전체 목록 조회
    @GetMapping("/head/banners")
    public List<HeadBannerResponseDTO> getBannerList() {

        return headBannerService.getBannerList();
    }

    // 활성 배너 목록 조회
    @GetMapping("/head/banners/active")
    public List<HeadBannerResponseDTO> getActiveBannerList() {

        return headBannerService.getActiveBannerList();
    }

    // 배너 상세 조회
    @GetMapping("/head/banners/{bannerId}")
    public HeadBannerResponseDTO getBannerDetail(
            @PathVariable Integer bannerId) {

        return headBannerService.getBannerDetail(bannerId);
    }

    // 배너 수정
    @PutMapping("/head/banners/{bannerId}")
    public HeadBannerResponseDTO updateBanner(
            @PathVariable Integer bannerId,
            @RequestBody HeadBannerUpdateRequestDTO requestDTO) {

        return headBannerService.updateBanner(bannerId, requestDTO);
    }

    // 배너 비활성화
    @DeleteMapping("/head/banners/{bannerId}")
    public String deleteBanner(
            @PathVariable Integer bannerId) {

        return headBannerService.deleteBanner(bannerId);
    }
}