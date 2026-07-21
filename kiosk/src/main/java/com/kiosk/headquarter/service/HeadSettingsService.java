package com.kiosk.headquarter.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kiosk.entity.SystemSetting;
import com.kiosk.headquarter.dto.SystemSettingDto;
import com.kiosk.headquarter.repository.SystemSettingRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class HeadSettingsService {

    private final SystemSettingRepository systemSettingRepository;

    @Transactional
    public SystemSettingDto getSettings() {
        SystemSetting setting = systemSettingRepository.findById(1)
                .orElseGet(() -> systemSettingRepository.save(SystemSetting.builder().build()));
        return SystemSettingDto.fromEntity(setting);
    }

    @Transactional
    public SystemSettingDto updateSettings(SystemSettingDto dto) {
        SystemSetting setting = systemSettingRepository.findById(1)
                .orElseGet(() -> SystemSetting.builder().build());

        if (dto.getUseVoiceGuide() != null) setting.setUseVoiceGuide(dto.getUseVoiceGuide());
        if (dto.getReceiptPrintMode() != null) setting.setReceiptPrintMode(dto.getReceiptPrintMode());
        if (dto.getTumblerDiscountAmount() != null) setting.setTumblerDiscountAmount(dto.getTumblerDiscountAmount());
        if (dto.getLowStockAlertCount() != null) setting.setLowStockAlertCount(dto.getLowStockAlertCount());
        if (dto.getUseEasyPay() != null) setting.setUseEasyPay(dto.getUseEasyPay());

        SystemSetting savedSetting = systemSettingRepository.save(setting);
        return SystemSettingDto.fromEntity(savedSetting);
    }
}
