package com.kiosk.headquarter.repository;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.kiosk.headquarter.dto.auth.HeadLoginEmployeeDTO;

@Mapper
public interface HeadAuthMapper {

    HeadLoginEmployeeDTO findEmployeeByLoginId(@Param("loginId") String loginId);
}