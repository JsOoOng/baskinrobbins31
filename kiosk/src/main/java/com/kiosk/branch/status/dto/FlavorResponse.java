package com.kiosk.branch.status.dto;

import com.kiosk.entity.IcecreamFlavor;

import lombok.Builder;
import lombok.Getter;


/**
 * [코드 흐름 안내] FlavorResponse
 *
 * <p>역할: 아이스크림 맛 처리 결과를 프론트에 반환하기 위한 응답 DTO다.</p>
 * <p>호출 흐름: Entity/조회 결과 -> Service 변환 -> 이 DTO -> Controller JSON 응답 순서로 사용한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
@Getter
@Builder
public class FlavorResponse {


    private Integer flavorId;

    private String flavorName;



    public static FlavorResponse from(IcecreamFlavor flavor){

        return FlavorResponse.builder()
                .flavorId(flavor.getId())
                .flavorName(flavor.getFlavorName())
                .build();

    }

}