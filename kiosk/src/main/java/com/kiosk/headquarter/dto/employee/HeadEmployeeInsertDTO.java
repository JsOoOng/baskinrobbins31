package com.kiosk.headquarter.dto.employee;

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
        HeadEmployeeCreateResponse response = new HeadEmployeeCreateResponse();

        response.setEmployeeId(this.employeeId);
        response.setStoreId(this.storeId);
        response.setLoginId(this.loginId);
        response.setName(this.name);
        response.setRole(this.role);
        response.setStatus(this.status);

        return response;
    }
}