package com.kiosk.branch.status.reopsitory;

import org.apache.ibatis.annotations.Mapper;

import com.kiosk.entity.RestockRequest;

/**
 * [코드 흐름 안내] BranchRestockRequestMapper
 *
 * <p>역할: 지점 운영의 재입고·발주 데이터를 조회하거나 저장하는 DB 접근 계층이다.</p>
 * <p>호출 흐름: Service -> 이 Mapper -> MySQL -> 조회/변경 결과 반환 순서로 동작한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
@Mapper
public interface BranchRestockRequestMapper {

    /*
     * 발주 요청 등록
     *
     * 성공하면 1을 반환합니다.
     *
     * Mapper XML의 useGeneratedKeys 설정으로
     * 생성된 request_id가
     * restockRequest.id에 저장됩니다.
     */
    int insert(
            RestockRequest restockRequest
    );
}