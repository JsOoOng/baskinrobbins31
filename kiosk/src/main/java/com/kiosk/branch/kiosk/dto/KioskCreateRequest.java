package com.kiosk.branch.kiosk.dto;


import lombok.Getter;
import lombok.Setter;


/**
 * [코드 흐름 안내] KioskCreateRequest
 *
 * <p>역할: 키오스크 기기 요청 JSON 값을 Controller와 Service 사이로 전달한다.</p>
 * <p>호출 흐름: 프론트 JSON -> Controller의 @RequestBody -> 이 DTO -> Service 검증 순서로 사용한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
@Getter
@Setter
public class KioskCreateRequest {


    private Integer storeId;


    private Integer kioskNumber;


    private String deviceSerial;


}