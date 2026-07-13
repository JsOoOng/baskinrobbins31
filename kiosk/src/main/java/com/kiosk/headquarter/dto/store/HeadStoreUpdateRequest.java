package com.kiosk.headquarter.dto.store;

import com.kiosk.entity.enums.StoreStatus;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
    private String address;

    @Size(max = 20)
    private String phone;

    @Size(max = 100)
    private String businessHours;

    @NotNull(message = "지점 상태는 필수입니다.")
    private StoreStatus storeStatus;
}