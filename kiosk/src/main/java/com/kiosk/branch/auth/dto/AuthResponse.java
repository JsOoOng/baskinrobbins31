package com.kiosk.branch.auth.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AuthResponse {

    private Integer employeeId;

    private String name;

    private String role;

    private Integer storeId;

    private String storeName;

}