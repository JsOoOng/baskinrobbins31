package com.kiosk.customer.settings.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kiosk.headquarter.service.HeadSettingsService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/customer/settings")
@RequiredArgsConstructor
public class CustomerSettingsController {

    private final HeadSettingsService headSettingsService;

    @GetMapping
    public ResponseEntity<Map<String, Boolean>> getCustomerSettings() {
        Boolean configuredValue = headSettingsService.getSettings().getAllowOrderCancel();
        boolean allowOrderCancel = configuredValue == null || configuredValue;
        return ResponseEntity.ok(Map.of("allowOrderCancel", allowOrderCancel));
    }
}
