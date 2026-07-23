package com.kiosk.headquarter.dto.employee;

/**
 * [코드 흐름 안내] HeadEmployeeInsertDTO
 *
 * <p>역할: 직원 계정 계층 사이에서 필요한 값만 전달하는 DTO다.</p>
 * <p>호출 흐름: Controller·Service·프론트 사이의 데이터 모양을 현재 필드 정의로 고정한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
public class HeadEmployeeInsertDTO {

    private Integer employeeId;
    private Integer storeId;
    private String loginId;
    private String password;
    private String name;
    private String role;
    private String status;

    public Integer getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Integer employeeId) {
        this.employeeId = employeeId;
    }

    public Integer getStoreId() {
        return storeId;
    }

    public void setStoreId(Integer storeId) {
        this.storeId = storeId;
    }

    public String getLoginId() {
        return loginId;
    }

    public void setLoginId(String loginId) {
        this.loginId = loginId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public HeadEmployeeCreateResponse toResponse() {

        return HeadEmployeeCreateResponse.builder()
                .employeeId(this.employeeId)
                .storeId(this.storeId)
                .loginId(this.loginId)
                .name(this.name)
                .role(this.role)
                .status(this.status)
                .build();
    }
}