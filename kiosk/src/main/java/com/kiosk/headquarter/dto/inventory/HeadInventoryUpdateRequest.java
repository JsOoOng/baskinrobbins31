package com.kiosk.headquarter.dto.inventory;

import com.kiosk.entity.enums.AutoRestockMode;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class HeadInventoryUpdateRequest {

    @NotNull(
            message = "자동 재고 보충 여부는 필수입니다."
    )
    private Boolean autoRestockEnabled;

    @NotNull(
            message = "자동 재고 보충 방식을 선택해주세요."
    )
    private AutoRestockMode restockMode;

    @NotNull(
            message = "최소 재고를 입력해주세요."
    )
    @Min(
            value = 0,
            message = "최소 재고는 0 이상이어야 합니다."
    )
    private Integer minStock;

    @NotNull(
            message = "목표 재고를 입력해주세요."
    )
    @Min(
            value = 1,
            message = "목표 재고는 1 이상이어야 합니다."
    )
    private Integer targetStock;
}