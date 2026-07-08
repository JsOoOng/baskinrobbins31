package com.kiosk.headquarter.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.kiosk.headquarter.dto.employee.HeadEmployeeInsertDTO;

@Mapper
public interface HeadEmployeeMapper {

    int countStoreById(@Param("storeId") Integer storeId);

    int countEmployeeByLoginId(@Param("loginId") String loginId);

    int insertStoreAdminEmployee(HeadEmployeeInsertDTO employee);
}