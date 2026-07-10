package com.kiosk.headquarter.repository;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.kiosk.headquarter.dto.auth.HeadLoginEmployeeDTO;

@Mapper
public interface HeadAuthMapper {

    HeadLoginEmployeeDTO findEmployeeByLoginId(String loginId);

    void updatePassword(
            @Param("employeeId") Integer employeeId,
            @Param("password") String password
    );
}