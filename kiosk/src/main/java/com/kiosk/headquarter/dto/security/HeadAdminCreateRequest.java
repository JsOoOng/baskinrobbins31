package com.kiosk.headquarter.dto.security;

import com.kiosk.entity.enums.AdminRole;
import com.kiosk.entity.enums.AdminStatus;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * [코드 흐름 안내] HeadAdminCreateRequest
 *
 * <p>역할: 본사 관리자 계정 요청 JSON 값을 Controller와 Service 사이로 전달한다.</p>
 * <p>호출 흐름: 프론트 JSON -> Controller의 @RequestBody -> 이 DTO -> Service 검증 순서로 사용한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
@Getter
@Setter
@NoArgsConstructor
public class HeadAdminCreateRequest {

    /*
     * 관리자 로그인 ID
     */
    private String loginId;

    /*
     * 초기 비밀번호
     */
    private String password;

    /*
     * 관리자 이름
     */
    private String name;

    /*
     * 소속 부서
     */
    private String department;

    /*
     * 관리자 역할
     *
     * SUPER_ADMIN
     * HEAD_ADMIN
     * ADMIN
     */
    private AdminRole role;

    /*
     * 계정 상태
     *
     * ACTIVE
     * INACTIVE
     */
    private AdminStatus status;
}