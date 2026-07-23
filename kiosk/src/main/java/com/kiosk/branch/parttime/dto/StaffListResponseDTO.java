package com.kiosk.branch.parttime.dto;

import com.kiosk.entity.enums.StaffStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;


/**
 * [코드 흐름 안내] StaffListResponseDTO
 *
 * <p>역할: 직원 계층 사이에서 필요한 값만 전달하는 DTO다.</p>
 * <p>호출 흐름: Controller·Service·프론트 사이의 데이터 모양을 현재 필드 정의로 고정한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
@Getter
@Builder
@AllArgsConstructor
public class StaffListResponseDTO {


    private Integer staffId;


    private String name;


    private String hp;


    private String email;


    private String address;


    private String birthDate;


    private String healthCertEndDate;


    private Integer hourlyWage;


    private StaffStatus status;


}