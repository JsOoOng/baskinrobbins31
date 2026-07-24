package com.kiosk.headquarter.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kiosk.entity.SystemSetting;
import com.kiosk.headquarter.dto.SystemSettingDto;
import com.kiosk.headquarter.repository.SystemSettingRepository;

import lombok.RequiredArgsConstructor;

/**
 * [코드 흐름 안내] HeadSettingsService
 *
 * <p>역할: 본사 관리의 환경 설정 업무 규칙과 상태 변경을 처리한다.</p>
 * <p>호출 흐름: Controller 호출 -> 이 서비스 -> SystemSettingRepository -> Entity/DTO 변환 -> Controller 반환 순서로 동작한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
@Service
@RequiredArgsConstructor
public class HeadSettingsService {

    private final SystemSettingRepository systemSettingRepository;
    private final AdminLogService adminLogService;

    @Transactional
    /**
     * [메서드 흐름] getSettings
     * Controller 또는 상위 서비스에서 호출되어 SystemSettingRepository을 사용해 검증·조회·저장 등의 처리를 수행하고 결과를 반환한다.
     */
    public SystemSettingDto getSettings() {
        SystemSetting setting = systemSettingRepository.findById(1)
                .orElseGet(() -> systemSettingRepository.save(SystemSetting.builder().build()));
        return SystemSettingDto.fromEntity(setting);
    }

    @Transactional
    /**
     * [메서드 흐름] updateSettings
     * Controller 또는 상위 서비스에서 호출되어 SystemSettingRepository을 사용해 검증·조회·저장 등의 처리를 수행하고 결과를 반환한다.
     */
    public SystemSettingDto updateSettings(SystemSettingDto dto) {
        SystemSetting setting = systemSettingRepository.findById(1)
                .orElseGet(() -> SystemSetting.builder().build());

        if (dto.getUseVoiceGuide() != null) setting.setUseVoiceGuide(dto.getUseVoiceGuide());
        if (dto.getReceiptPrintMode() != null) setting.setReceiptPrintMode(dto.getReceiptPrintMode());
        if (dto.getTumblerDiscountAmount() != null) setting.setTumblerDiscountAmount(dto.getTumblerDiscountAmount());
        if (dto.getLowStockAlertCount() != null) setting.setLowStockAlertCount(dto.getLowStockAlertCount());
        if (dto.getUseEasyPay() != null) setting.setUseEasyPay(dto.getUseEasyPay());

        SystemSetting savedSetting = systemSettingRepository.save(setting);
        adminLogService.logAction("환경 설정", "키오스크 공통 환경 설정 변경");
        return SystemSettingDto.fromEntity(savedSetting);
    }
}
