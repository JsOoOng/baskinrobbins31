package com.kiosk.headquarter.dto.banner;

import java.time.LocalDateTime;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * [코드 흐름 안내] HeadBannerCreateRequest
 *
 * <p>역할: 배너 요청 JSON 값을 Controller와 Service 사이로 전달한다.</p>
 * <p>호출 흐름: 프론트 JSON -> Controller의 @RequestBody -> 이 DTO -> Service 검증 순서로 사용한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
@Getter
@Setter
@NoArgsConstructor
public class HeadBannerCreateRequest {

    @Size(
            max = 100,
            message = "배너 제목은 100자 이하여야 합니다."
    )
    private String title;

    @NotBlank(
            message = "배너 이미지 URL은 필수입니다."
    )
    @Size(
            max = 255,
            message = "이미지 URL은 255자 이하여야 합니다."
    )
    private String imageUrl;

    private Boolean isActive;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
}
