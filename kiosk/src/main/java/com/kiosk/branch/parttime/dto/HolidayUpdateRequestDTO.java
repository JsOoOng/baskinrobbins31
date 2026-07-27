package com.kiosk.branch.parttime.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * [코드 흐름 안내] HolidayUpdateRequestDTO
 *
 * <p>역할: 직원·일정·급여 계층 사이에서 필요한 값만 전달하는 DTO다.</p>
 * <p>호출 흐름: Controller·Service·프론트 사이의 데이터 모양을 현재 필드 정의로 고정한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
@Getter
@NoArgsConstructor
public class HolidayUpdateRequestDTO {

    private Boolean isHoliday;

}