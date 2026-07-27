package com.kiosk.branch.status.dto;

import com.kiosk.entity.enums.AutoRestockMode;

import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * [코드 흐름 안내] StoreFlavorRestockRequest
 *
 * <p>역할: 재입고·발주 요청 JSON 값을 Controller와 Service 사이로 전달한다.</p>
 * <p>호출 흐름: 프론트 JSON -> Controller의 @RequestBody -> 이 DTO -> Service 검증 순서로 사용한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
@Getter
@NoArgsConstructor
public class StoreFlavorRestockRequest {


    /*
     * 최소 재고
     */
    private Integer minStock;


    /*
     * 목표 재고
     */
    private Integer targetStock;


    /*
     * 자동 보충 사용 여부
     */
    private Boolean autoRestockEnabled;


    /*
     * 자동 보충 방식
     */
    private AutoRestockMode restockMode;

}