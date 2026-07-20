package com.kiosk.headquarter.dto;

import java.time.LocalDateTime;

import com.kiosk.entity.Policy;
import com.kiosk.entity.enums.PolicyType;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
