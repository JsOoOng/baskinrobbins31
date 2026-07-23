package com.kiosk.headquarter.dto.admin;

import java.time.LocalDateTime;

import com.kiosk.entity.AdminActionLog;

import lombok.Builder;
import lombok.Getter;

/**
 * [코드 흐름 안내] AdminLogResponseDto
 *
 * <p>역할: 본사 관리자 작업 로그 계층 사이에서 필요한 값만 전달하는 DTO다.</p>
 * <p>호출 흐름: Controller·Service·프론트 사이의 데이터 모양을 현재 필드 정의로 고정한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
@Getter
@Builder
public class AdminLogResponseDto {
    private Long id;
    private String administrator;
    private String type;
    private String action;
    private LocalDateTime createdAt;
    private String displayTime;

    public static AdminLogResponseDto from(AdminActionLog log, String displayTime) {
        return AdminLogResponseDto.builder()
                .id(log.getId())
                .administrator(log.getAdministrator())
                .type(log.getType())
                .action(log.getAction())
                .createdAt(log.getCreatedAt())
                .displayTime(displayTime)
                .build();
    }
}
