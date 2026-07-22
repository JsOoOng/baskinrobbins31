package com.kiosk.headquarter.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kiosk.entity.Banner;
import com.kiosk.headquarter.dto.banner.HeadBannerActiveRequest;
import com.kiosk.headquarter.dto.banner.HeadBannerCreateRequest;
import com.kiosk.headquarter.dto.banner.HeadBannerResponse;
import com.kiosk.headquarter.dto.banner.HeadBannerUpdateRequest;
import com.kiosk.headquarter.repository.HeadBannerMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HeadBannerService {

    private final HeadBannerMapper
            headBannerMapper;

    private final AdminLogService
            adminLogService;

    /*
     * 배너 전체 목록
     */
    public List<HeadBannerResponse> getBanners() {

        return headBannerMapper
                .findAllByOrderByIdDesc()
                .stream()
                .map(HeadBannerResponse::from)
                .toList();
    }

    /*
     * 배너 상세 조회
     */
    public HeadBannerResponse getBanner(
            Integer bannerId
    ) {

        Banner banner =
                findBanner(bannerId);

        return HeadBannerResponse.from(
                banner
        );
    }

    /*
     * 배너 등록
     */
    @Transactional
    public HeadBannerResponse createBanner(
            HeadBannerCreateRequest request
    ) {

        Boolean isActive =
                request.getIsActive() != null
                        ? request.getIsActive()
                        : true;

        Banner banner =
                Banner.builder()
                        .title(
                                normalizeTitle(
                                        request.getTitle()
                                )
                        )
                        .imageUrl(
                                request.getImageUrl()
                                        .trim()
                        )
                        .isActive(isActive)
                        .build();

        Banner savedBanner =
                headBannerMapper.save(banner);

        adminLogService.logAction("배너", "배너 신규 등록");

        return HeadBannerResponse.from(
                savedBanner
        );
    }

    /*
     * 배너 수정
     */
    @Transactional
    public HeadBannerResponse updateBanner(
            Integer bannerId,
            HeadBannerUpdateRequest request
    ) {

        Banner banner =
                findBanner(bannerId);

        Boolean isActive =
                request.getIsActive() != null
                        ? request.getIsActive()
                        : banner.getIsActive();

        banner.updateBanner(
                normalizeTitle(
                        request.getTitle()
                ),
                request.getImageUrl().trim(),
                isActive
        );

        adminLogService.logAction("배너", "배너 정보 수정");

        return HeadBannerResponse.from(
                banner
        );
    }

    /*
     * 노출 상태 변경
     */
    @Transactional
    public HeadBannerResponse updateActive(
            Integer bannerId,
            HeadBannerActiveRequest request
    ) {

        Banner banner =
                findBanner(bannerId);

        banner.changeActive(
                request.getIsActive()
        );

        adminLogService.logAction("배너", "배너 상태 변경 (" + (request.getIsActive() ? "활성" : "비활성") + ")");

        return HeadBannerResponse.from(
                banner
        );
    }

    /*
     * 배너 삭제
     *
     * 현재 테이블에는 삭제 상태 컬럼이 없으므로
     * 실제 행을 삭제합니다.
     */
    @Transactional
    public void deleteBanner(
            Integer bannerId
    ) {

        Banner banner =
                findBanner(bannerId);

        headBannerMapper.delete(banner);

        adminLogService.logAction("배너", "배너 삭제");
    }

    /*
     * 배너 공통 조회
     */
    private Banner findBanner(
            Integer bannerId
    ) {

        if (bannerId == null) {
            throw new IllegalArgumentException(
                    "배너 번호가 없습니다."
            );
        }

        return headBannerMapper
                .findById(bannerId)
                .orElseThrow(
                        () ->
                                new IllegalArgumentException(
                                        "존재하지 않는 배너입니다."
                                )
                );
    }

    /*
     * 빈 제목은 null로 저장
     */
    private String normalizeTitle(
            String title
    ) {

        if (
                title == null ||
                title.isBlank()
        ) {
            return null;
        }

        return title.trim();
    }
}