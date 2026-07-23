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

/**
 * [코드 흐름 안내] HeadPolicyController
 *
 * <p>역할: 본사 관리의 운영 정책 HTTP 요청을 받는 진입점이다.</p>
 * <p>호출 흐름: Vue/API 요청 -> 이 컨트롤러(/head/policies) -> HeadPolicyService -> 응답 DTO 또는 JSON -> 화면 갱신 순서로 이동한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
@RestController
@RequestMapping("/head/policies")
@RequiredArgsConstructor
public class HeadPolicyController {

    private final HeadPolicyService policyService;

    /**
     * [요청 흐름] GET /head/policies
     * 프론트 요청을 받아 getPolicies() 메서드가 입력을 받고 HeadPolicyService 호출 후 결과를 응답한다.
     */
    @GetMapping
    public ResponseEntity<List<PolicyResponseDto>> getPolicies(@RequestParam(value = "type", required = false) PolicyType type) {
        if (type != null) {
            return ResponseEntity.ok(policyService.getPoliciesByType(type));
        }
        return ResponseEntity.ok(policyService.getAllPolicies());
    }

    /**
     * [요청 흐름] GET /head/policies/{policyId}
     * 프론트 요청을 받아 getPolicy() 메서드가 입력을 받고 HeadPolicyService 호출 후 결과를 응답한다.
     */
    @GetMapping("/{policyId}")
    public ResponseEntity<PolicyResponseDto> getPolicy(@PathVariable("policyId") Integer policyId) {
        return ResponseEntity.ok(policyService.getPolicy(policyId));
    }

    /**
     * [요청 흐름] POST /head/policies
     * 프론트 요청을 받아 createPolicy() 메서드가 입력을 받고 HeadPolicyService 호출 후 결과를 응답한다.
     */
    @PostMapping
    public ResponseEntity<PolicyResponseDto> createPolicy(@RequestBody PolicyRequestDto requestDto) {
        return ResponseEntity.ok(policyService.createPolicy(requestDto));
    }

    /**
     * [요청 흐름] PUT /head/policies/{policyId}
     * 프론트 요청을 받아 updatePolicy() 메서드가 입력을 받고 HeadPolicyService 호출 후 결과를 응답한다.
     */
    @PutMapping("/{policyId}")
    public ResponseEntity<PolicyResponseDto> updatePolicy(
            @PathVariable("policyId") Integer policyId,
            @RequestBody PolicyRequestDto requestDto) {
        return ResponseEntity.ok(policyService.updatePolicy(policyId, requestDto));
    }

    /**
     * [요청 흐름] DELETE /head/policies/{policyId}
     * 프론트 요청을 받아 deletePolicy() 메서드가 입력을 받고 HeadPolicyService 호출 후 결과를 응답한다.
     */
    @DeleteMapping("/{policyId}")
    public ResponseEntity<Void> deletePolicy(@PathVariable("policyId") Integer policyId) {
        policyService.deletePolicy(policyId);
        return ResponseEntity.noContent().build();
    }

    /**
     * [요청 흐름] PATCH /head/policies/{policyId}/active
     * 프론트 요청을 받아 updateVisibility() 메서드가 입력을 받고 HeadPolicyService 호출 후 결과를 응답한다.
     */
    @PatchMapping("/{policyId}/active")
    public ResponseEntity<PolicyResponseDto> updateVisibility(
            @PathVariable("policyId") Integer policyId,
            @RequestBody Map<String, Boolean> request) {
        Boolean isActive = request.get("isActive");
        return ResponseEntity.ok(policyService.updateVisibility(policyId, isActive));
    }
}
