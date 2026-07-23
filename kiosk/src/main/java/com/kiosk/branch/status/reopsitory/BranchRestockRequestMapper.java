package com.kiosk.branch.status.reopsitory;

import org.apache.ibatis.annotations.Mapper;

import com.kiosk.entity.RestockRequest;

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