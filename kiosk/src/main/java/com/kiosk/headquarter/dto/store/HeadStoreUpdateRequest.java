package com.kiosk.headquarter.dto.store;

import com.kiosk.entity.enums.StoreStatus;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * [코드 흐름 안내] HeadStoreUpdateRequest
 *
 * <p>역할: 지점 요청 JSON 값을 Controller와 Service 사이로 전달한다.</p>
 * <p>호출 흐름: 프론트 JSON -> Controller의 @RequestBody -> 이 DTO -> Service 검증 순서로 사용한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
@Getter
@Setter
@NoArgsConstructor
public class HeadStoreUpdateRequest {

    @NotBlank(message = "지점명은 필수입니다.")
    @Size(max = 100)
    private String storeName;

    @Size(max = 20)
    private String businessNumber;

    @NotBlank(message = "지역은 필수입니다.")
    @Size(max = 50)
    private String region;

    @NotBlank(message = "주소는 필수입니다.")
    @Size(max = 255)
    private String address;

    @Size(max = 20)
    private String phone;

    @Size(max = 100)
    private String businessHours;

    @NotNull(message = "지점 상태는 필수입니다.")
    private StoreStatus storeStatus;
}