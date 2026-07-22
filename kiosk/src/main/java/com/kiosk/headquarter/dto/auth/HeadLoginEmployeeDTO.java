package com.kiosk.headquarter.dto.auth;

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
public class HeadLoginEmployeeDTO {

    /*
     * 실제 DB 컬럼:
     * headquarter_admins.admin_id
     *
     * 기존 로그인 구조에서 employeeId라는 이름을
     * 사용하고 있으므로 DTO 필드명은 유지합니다.
     */
    private Integer employeeId;

    /*
     * 본사 관리자는 지점 소속이 아니므로
     * HeadAuthMapper.xml에서 항상 NULL로 조회됩니다.
     */
    private Integer storeId;

    private String loginId;

    /*
     * 비밀번호 검증에만 사용하며
     * 최종 로그인 응답에는 포함하지 않습니다.
     */
    private String password;

    private String name;

    private String role;

    private String status;

    /*
     * DB 조회 DTO를 로그인 응답 DTO로 변환
     */
    public HeadLoginResponse toResponse() {

        HeadLoginResponse response =
                new HeadLoginResponse();

        response.setEmployeeId(
                this.employeeId
        );

        response.setStoreId(
                this.storeId
        );

        response.setLoginId(
                this.loginId
        );

        response.setName(
                this.name
        );

        response.setRole(
                this.role
        );

        response.setStatus(
                this.status
        );

        return response;
    }
}