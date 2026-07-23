package com.kiosk.branch.kiosk.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * [코드 흐름 안내] KioskBannerResponse
 *
 * <p>역할: 키오스크 배너 처리 결과를 프론트에 반환하기 위한 응답 DTO다.</p>
 * <p>호출 흐름: Entity/조회 결과 -> Service 변환 -> 이 DTO -> Controller JSON 응답 순서로 사용한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KioskBannerResponse {
    private Integer kioskId;
    private Integer bannerId;
    private String bannerTitle;
    private String bannerImageUrl;
}
