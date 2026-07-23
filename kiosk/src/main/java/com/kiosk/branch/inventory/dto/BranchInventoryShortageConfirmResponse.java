package com.kiosk.branch.inventory.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * [코드 흐름 안내] BranchInventoryShortageConfirmResponse
 *
 * <p>역할: 재고 처리 결과를 프론트에 반환하기 위한 응답 DTO다.</p>
 * <p>호출 흐름: Entity/조회 결과 -> Service 변환 -> 이 DTO -> Controller JSON 응답 순서로 사용한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
@Getter
@AllArgsConstructor
public class BranchInventoryShortageConfirmResponse {

    private Integer storeId;

    /*
     * 이번 요청에서 확인 처리된
     * 부족 알람 개수
     */
    private Integer confirmedCount;

    /*
     * 확인 후 이동할 화면
     */
    private String routeName;
}