package com.kiosk.headquarter.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kiosk.headquarter.dto.SystemSettingDto;
import com.kiosk.headquarter.service.HeadSettingsService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/head/settings")
@RequiredArgsConstructor
public class HeadSettingsController {

    private final HeadSettingsService headSettingsService;

    @GetMapping
    public ResponseEntity<SystemSettingDto> getSettings() {
        return ResponseEntity.ok(headSettingsService.getSettings());
    }

    @PutMapping
    public ResponseEntity<SystemSettingDto> updateSettings(@RequestBody SystemSettingDto dto) {
        return ResponseEntity.ok(headSettingsService.updateSettings(dto));
    }
}
