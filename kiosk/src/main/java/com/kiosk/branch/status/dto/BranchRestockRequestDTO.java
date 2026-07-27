package com.kiosk.branch.status.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * [코드 흐름 안내] BranchRestockRequestDTO
 *
 * <p>역할: 재입고·발주 계층 사이에서 필요한 값만 전달하는 DTO다.</p>
 * <p>호출 흐름: Controller·Service·프론트 사이의 데이터 모양을 현재 필드 정의로 고정한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
@Getter
@Setter
@NoArgsConstructor
public class BranchRestockRequestDTO {

    /*
     * 재고 부족 알람 ID
     *
     * 부족 알람을 통해 신청하는 경우 사용합니다.
     * 지점 관리자가 직접 신청하는 경우에는
     * null일 수 있습니다.
     *
     * 화면에는 표시하지 않습니다.
     */
    private Integer alertId;

    /*
     * 상품 재고 ID
     *
     * 일반 재고 발주일 경우 사용합니다.
     */
    private Integer storeInventoryId;

    /*
     * 맛 재고 ID
     *
     * 아이스크림 맛 발주일 경우 사용합니다.
     */
    private Integer storeFlavorId;

    /*
     * 발주 요청 수량
     */
    private Integer requestQuantity;
}