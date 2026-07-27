package com.kiosk.headquarter.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kiosk.entity.Banner;
import com.kiosk.entity.enums.NotificationCategory;
import com.kiosk.entity.enums.NotificationType;
import com.kiosk.headquarter.dto.banner.HeadBannerActiveRequest;
import com.kiosk.headquarter.dto.banner.HeadBannerCreateRequest;
import com.kiosk.headquarter.dto.banner.HeadBannerResponse;
import com.kiosk.headquarter.dto.banner.HeadBannerUpdateRequest;
import com.kiosk.headquarter.repository.HeadBannerMapper;

import lombok.RequiredArgsConstructor;

/**
 * [코드 흐름 안내] HeadBannerService
 *
 * <p>역할: 본사 관리의 배너 업무 규칙과 상태 변경을 처리한다.</p>
 * <p>호출 흐름: Controller 호출 -> 이 서비스 -> HeadBannerMapper, AdminLogService -> Entity/DTO 변환 -> Controller 반환 순서로 동작한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HeadBannerService {

    private final HeadBannerMapper
            headBannerMapper;

    private final AdminLogService
            adminLogService;
    private final HeadNotificationService headNotificationService;

    /*
     * 배너 전체 목록
     */
    /**
     * [메서드 흐름] getBanners
     * Controller 또는 상위 서비스에서 호출되어 HeadBannerMapper, AdminLogService을 사용해 검증·조회·저장 등의 처리를 수행하고 결과를 반환한다.
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
    /**
     * [메서드 흐름] getBanner
     * Controller 또는 상위 서비스에서 호출되어 HeadBannerMapper, AdminLogService을 사용해 검증·조회·저장 등의 처리를 수행하고 결과를 반환한다.
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
    /**
     * [메서드 흐름] createBanner
     * Controller 또는 상위 서비스에서 호출되어 HeadBannerMapper, AdminLogService을 사용해 검증·조회·저장 등의 처리를 수행하고 결과를 반환한다.
     */
    public HeadBannerResponse createBanner(
            HeadBannerCreateRequest request
    ) {
        validatePeriod(request.getStartDate(), request.getEndDate());

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
                        .startDate(request.getStartDate())
                        .endDate(request.getEndDate())
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
    /**
     * [메서드 흐름] updateBanner
     * Controller 또는 상위 서비스에서 호출되어 HeadBannerMapper, AdminLogService을 사용해 검증·조회·저장 등의 처리를 수행하고 결과를 반환한다.
     */
    public HeadBannerResponse updateBanner(
            Integer bannerId,
            HeadBannerUpdateRequest request
    ) {
        validatePeriod(request.getStartDate(), request.getEndDate());

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
                isActive,
                request.getStartDate(),
                request.getEndDate()
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
    /**
     * [메서드 흐름] updateActive
     * Controller 또는 상위 서비스에서 호출되어 HeadBannerMapper, AdminLogService을 사용해 검증·조회·저장 등의 처리를 수행하고 결과를 반환한다.
     */
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
    /**
     * [메서드 흐름] deleteBanner
     * Controller 또는 상위 서비스에서 호출되어 HeadBannerMapper, AdminLogService을 사용해 검증·조회·저장 등의 처리를 수행하고 결과를 반환한다.
     */
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

    /*
     * 쉬운주석: 시작일과 종료일을 모두 입력했다면 종료일이 반드시 더 뒤인지 확인한다.
     */
    private void validatePeriod(LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate != null && endDate != null && !endDate.isAfter(startDate)) {
            throw new IllegalArgumentException("배너 종료일은 시작일보다 뒤여야 합니다.");
        }
    }

    /**
     * 쉬운주석: 매시간 배너 종료일을 확인해 7일·1일 전에 알리고,
     * 종료 시 is_active를 false로 바꿔 키오스크에서 자동으로 숨긴다.
     */
    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void synchronizeBannerPeriods() {
        LocalDateTime now = LocalDateTime.now();
        LocalDate today = now.toLocalDate();

        for (Banner banner : headBannerMapper.findAll()) {
            if (banner.getEndDate() == null) {
                continue;
            }
            long days = ChronoUnit.DAYS.between(
                    today,
                    banner.getEndDate().toLocalDate()
            );
            String reference = banner.getId() + ":" + banner.getEndDate().toLocalDate();
            String name = banner.getTitle() == null ? "제목 없는 배너" : banner.getTitle();

            if (days == 7 || days == 1) {
                String remaining = days == 1 ? "하루" : "일주일";
                headNotificationService.createNotificationOnce(
                        NotificationCategory.BANNER,
                        NotificationType.BANNER_EXPIRING,
                        "배너 종료 " + remaining + " 전",
                        name + " 배너가 " + remaining + " 남았습니다.",
                        "head-banners",
                        reference
                );
            }
            if (!banner.getEndDate().isAfter(now)
                    && Boolean.TRUE.equals(banner.getIsActive())) {
                banner.changeActive(false);
                headNotificationService.createNotificationOnce(
                        NotificationCategory.BANNER,
                        NotificationType.BANNER_ENDED,
                        "배너 종료",
                        name + " 배너가 종료되어 자동으로 숨김 처리되었습니다.",
                        "head-banners",
                        reference
                );
                adminLogService.logAction("배너", name + " 자동 종료 및 숨김");
            }
        }
    }
}
