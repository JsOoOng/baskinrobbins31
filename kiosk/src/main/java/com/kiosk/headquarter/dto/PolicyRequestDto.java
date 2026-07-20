package com.kiosk.headquarter.dto;

import com.kiosk.entity.Policy;
import com.kiosk.entity.enums.PolicyType;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PolicyRequestDto {
    private PolicyType type;
    private String version;
    private String content;
    private Boolean isActive;

    public Policy toEntity() {
        return Policy.builder()
                .type(this.type)
                .version(this.version)
                .content(this.content)
                .isActive(this.isActive != null ? this.isActive : false)
                .build();
    }
}
