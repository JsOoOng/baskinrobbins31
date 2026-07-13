package com.kiosk.headquarter.dto.employee;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HeadEmployeeCreateResponse {

    private Integer employeeId;
    private Integer storeId;
    private String loginId;
    private String name;
    private String role;
    private String status;
}