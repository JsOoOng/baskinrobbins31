package com.kiosk.headquarter.dto.admin;

import java.time.LocalDateTime;

import com.kiosk.entity.AdminActionLog;

import lombok.Builder;
import lombok.Getter;

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
