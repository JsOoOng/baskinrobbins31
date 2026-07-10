package com.kiosk.customer.flavor.dto; // 본인 패키지에 맞게 수정

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
public class FlavorDto {
    private Integer flavorId;
    private String flavorName;
}