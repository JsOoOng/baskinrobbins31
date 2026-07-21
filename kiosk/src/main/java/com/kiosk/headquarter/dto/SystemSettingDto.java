package com.kiosk.headquarter.dto;

import com.kiosk.entity.SystemSetting;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
