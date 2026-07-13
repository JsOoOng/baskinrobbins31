package com.kiosk.headquarter.repository;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.kiosk.headquarter.dto.auth.HeadLoginEmployeeDTO;

@Mapper
public interface HeadAuthMapper {

    // 로그인 ID로 본사 관리자 조회
    HeadLoginEmployeeDTO findEmployeeByLoginId(
            @Param("loginId") String loginId
    );

    // 관리자 번호로 본사 관리자 조회
    HeadLoginEmployeeDTO findEmployeeByEmployeeId(
            @Param("employeeId") Integer employeeId
    );

    // 비밀번호 변경
    int updatePassword(
            @Param("employeeId") Integer employeeId,
            @Param("password") String password
    );
}