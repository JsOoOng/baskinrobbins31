package com.kiosk.headquarter.dto;

import com.kiosk.entity.SystemSetting;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * [코드 흐름 안내] SystemSettingDto
 *
 * <p>역할: 환경 설정 계층 사이에서 필요한 값만 전달하는 DTO다.</p>
 * <p>호출 흐름: Controller·Service·프론트 사이의 데이터 모양을 현재 필드 정의로 고정한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemSettingDto {
    private Integer id;
    private Boolean useVoiceGuide;
    private String receiptPrintMode;
    private Integer tumblerDiscountAmount;
    private Integer lowStockAlertCount;
    private Boolean useEasyPay;

    public static SystemSettingDto fromEntity(SystemSetting setting) {
        return SystemSettingDto.builder()
                .id(setting.getId())
                .useVoiceGuide(setting.getUseVoiceGuide())
                .receiptPrintMode(setting.getReceiptPrintMode())
                .tumblerDiscountAmount(setting.getTumblerDiscountAmount())
                .lowStockAlertCount(setting.getLowStockAlertCount())
                .useEasyPay(setting.getUseEasyPay())
                .build();
    }
}
