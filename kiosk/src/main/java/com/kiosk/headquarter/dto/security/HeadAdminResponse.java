package com.kiosk.headquarter.dto.security;

import java.time.LocalDateTime;

import com.kiosk.entity.HeadquarterAdmin;
import com.kiosk.entity.enums.AdminRole;
import com.kiosk.entity.enums.AdminStatus;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class HeadAdminResponse {

    /*
     * 본사 관리자 PK
     */
    private Integer adminId;

    /*
     * 로그인 ID
     */
    private String loginId;

    /*
     * 관리자 이름
     */
    private String name;

    /*
     * 소속 부서
     */
    private String department;

    /*
     * 관리자 역할
     */
    private AdminRole role;

    /*
     * 계정 상태
     */
    private AdminStatus status;

    /*
     * 계정 등록일
     */
    private LocalDateTime createdAt;

    /*
     * Entity → Response DTO 변환
     */
    public static HeadAdminResponse from(
            HeadquarterAdmin admin
    ) {

        if (admin == null) {
            throw new IllegalArgumentException(
                    "관리자 정보가 없습니다."
            );
        }

        return HeadAdminResponse.builder()
                .adminId(admin.getId())
                .loginId(admin.getLoginId())
                .name(admin.getName())
                .department(admin.getDepartment())
                .role(admin.getRole())
                .status(admin.getStatus())
                .createdAt(admin.getCreatedAt())
                .build();
    }
}