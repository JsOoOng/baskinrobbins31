package com.kiosk.headquarter.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kiosk.entity.Banner;
import com.kiosk.headquarter.dto.banner.HeadBannerCreateRequestDTO;
import com.kiosk.headquarter.dto.banner.HeadBannerResponseDTO;
import com.kiosk.headquarter.dto.banner.HeadBannerUpdateRequestDTO;
import com.kiosk.headquarter.repository.HeadBannerMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HeadBannerService {

    private final HeadBannerMapper headBannerMapper;

    // 배너 등록
    @Transactional
    public HeadBannerResponseDTO createBanner(HeadBannerCreateRequestDTO requestDTO) {

        if (requestDTO.getImageUrl() == null || requestDTO.getImageUrl().isBlank()) {
            throw new IllegalArgumentException("배너 이미지 URL을 입력해주세요.");
        }

        Banner banner = Banner.builder()
                .title(requestDTO.getTitle())
                .imageUrl(requestDTO.getImageUrl())
                .isActive(
                        requestDTO.getIsActive() != null
                                ? requestDTO.getIsActive()
                                : true
                )
                .build();

        Banner savedBanner = headBannerMapper.save(banner);

        return toResponseDTO(savedBanner);
    }

    // 배너 전체 목록 조회
    public List<HeadBannerResponseDTO> getBannerList() {

        return headBannerMapper.findAllByOrderByIdDesc()
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    // 활성 배너 목록 조회
    public List<HeadBannerResponseDTO> getActiveBannerList() {

        return headBannerMapper.findByIsActiveTrueOrderByIdDesc()
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    // 배너 상세 조회
    public HeadBannerResponseDTO getBannerDetail(Integer bannerId) {

        Banner banner = headBannerMapper.findById(bannerId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 배너입니다."));

        return toResponseDTO(banner);
    }

    // 배너 수정
    @Transactional
    public HeadBannerResponseDTO updateBanner(
            Integer bannerId,
            HeadBannerUpdateRequestDTO requestDTO) {

        Banner banner = headBannerMapper.findById(bannerId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 배너입니다."));

        if (requestDTO.getImageUrl() == null || requestDTO.getImageUrl().isBlank()) {
            throw new IllegalArgumentException("배너 이미지 URL을 입력해주세요.");
        }

        banner.updateBanner(
                requestDTO.getTitle(),
                requestDTO.getImageUrl(),
                requestDTO.getIsActive() != null
                        ? requestDTO.getIsActive()
                        : banner.getIsActive()
        );

        return toResponseDTO(banner);
    }

    // 배너 비활성화
    @Transactional
    public String deleteBanner(Integer bannerId) {

        Banner banner = headBannerMapper.findById(bannerId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 배너입니다."));

        banner.deactivate();

        return "배너 비활성화 성공";
    }

    private HeadBannerResponseDTO toResponseDTO(Banner banner) {

        return HeadBannerResponseDTO.builder()
                .bannerId(banner.getId())
                .title(banner.getTitle())
                .imageUrl(banner.getImageUrl())
                .isActive(banner.getIsActive())
                .build();
    }
}