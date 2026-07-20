package com.kiosk.headquarter.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kiosk.entity.enums.PolicyType;
import com.kiosk.headquarter.dto.PolicyRequestDto;
import com.kiosk.headquarter.dto.PolicyResponseDto;
import com.kiosk.headquarter.service.HeadPolicyService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/head/policies")
@RequiredArgsConstructor
public class HeadPolicyController {

    private final HeadPolicyService policyService;

    @GetMapping
    public ResponseEntity<List<PolicyResponseDto>> getPolicies(@RequestParam(value = "type", required = false) PolicyType type) {
        if (type != null) {
            return ResponseEntity.ok(policyService.getPoliciesByType(type));
        }
        return ResponseEntity.ok(policyService.getAllPolicies());
    }

    @GetMapping("/{policyId}")
    public ResponseEntity<PolicyResponseDto> getPolicy(@PathVariable("policyId") Integer policyId) {
        return ResponseEntity.ok(policyService.getPolicy(policyId));
    }

    @PostMapping
    public ResponseEntity<PolicyResponseDto> createPolicy(@RequestBody PolicyRequestDto requestDto) {
        return ResponseEntity.ok(policyService.createPolicy(requestDto));
    }

    @PutMapping("/{policyId}")
    public ResponseEntity<PolicyResponseDto> updatePolicy(
            @PathVariable("policyId") Integer policyId,
            @RequestBody PolicyRequestDto requestDto) {
        return ResponseEntity.ok(policyService.updatePolicy(policyId, requestDto));
    }

    @DeleteMapping("/{policyId}")
    public ResponseEntity<Void> deletePolicy(@PathVariable("policyId") Integer policyId) {
        policyService.deletePolicy(policyId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{policyId}/active")
    public ResponseEntity<PolicyResponseDto> updateVisibility(
            @PathVariable("policyId") Integer policyId,
            @RequestBody Map<String, Boolean> request) {
        Boolean isActive = request.get("isActive");
        return ResponseEntity.ok(policyService.updateVisibility(policyId, isActive));
    }
}
