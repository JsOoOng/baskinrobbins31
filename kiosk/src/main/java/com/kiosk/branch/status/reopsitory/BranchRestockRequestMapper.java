package com.kiosk.branch.status.reopsitory;

import org.apache.ibatis.annotations.Mapper;

import com.kiosk.entity.RestockRequest;

@Mapper
public interface BranchRestockRequestMapper {


    /*
     * 발주 요청 등록
     */
    void insert(
            RestockRequest restockRequest
    );

}