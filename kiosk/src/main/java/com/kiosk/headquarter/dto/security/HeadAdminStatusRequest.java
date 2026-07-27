package com.kiosk.headquarter.dto.security;

import com.kiosk.entity.enums.AdminStatus;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * [코드 흐름 안내] HeadAdminStatusRequest
 *
 * <p>역할: 본사 관리자 계정 요청 JSON 값을 Controller와 Service 사이로 전달한다.</p>
 * <p>호출 흐름: 프론트 JSON -> Controller의 @RequestBody -> 이 DTO -> Service 검증 순서로 사용한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
@Getter
@Setter
@NoArgsConstructor
public class HeadAdminStatusRequest {

    /*
     * 변경할 계정 상태
     *
     * ACTIVE
     * INACTIVE
     */
    private AdminStatus status;
}