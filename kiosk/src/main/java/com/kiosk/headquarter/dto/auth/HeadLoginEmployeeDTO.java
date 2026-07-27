package com.kiosk.headquarter.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * [코드 흐름 안내] HeadLoginEmployeeDTO
 *
 * <p>역할: 직원 계정 계층 사이에서 필요한 값만 전달하는 DTO다.</p>
 * <p>호출 흐름: Controller·Service·프론트 사이의 데이터 모양을 현재 필드 정의로 고정한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
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