package com.kiosk.headquarter.dto;

import java.time.LocalDateTime;

import com.kiosk.entity.Policy;
import com.kiosk.entity.enums.PolicyType;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * [코드 흐름 안내] PolicyResponseDto
 *
 * <p>역할: 운영 정책 계층 사이에서 필요한 값만 전달하는 DTO다.</p>
 * <p>호출 흐름: Controller·Service·프론트 사이의 데이터 모양을 현재 필드 정의로 고정한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
@Getter
@Setter
@NoArgsConstructor
public class PolicyResponseDto {
    private Integer policyId;
    private PolicyType type;
    private String version;
    private String content;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public PolicyResponseDto(Policy policy) {
        this.policyId = policy.getPolicyId();
        this.type = policy.getType();
        this.version = policy.getVersion();
        this.content = policy.getContent();
        this.isActive = policy.getIsActive();
        this.createdAt = policy.getCreatedAt();
        this.updatedAt = policy.getUpdatedAt();
    }
}
