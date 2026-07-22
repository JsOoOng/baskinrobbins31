package com.kiosk.headquarter.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kiosk.entity.Policy;
import com.kiosk.entity.enums.PolicyType;
import com.kiosk.headquarter.dto.PolicyRequestDto;
import com.kiosk.headquarter.dto.PolicyResponseDto;
import com.kiosk.headquarter.repository.HeadPolicyRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class HeadPolicyService {

    private final HeadPolicyRepository policyRepository;

    @Transactional(readOnly = true)
    public List<PolicyResponseDto> getAllPolicies() {
        return policyRepository.findAll()
                .stream()
                .map(PolicyResponseDto::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PolicyResponseDto> getPoliciesByType(PolicyType type) {
        return policyRepository.findByType(type)
                .stream()
                .map(PolicyResponseDto::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PolicyResponseDto getPolicy(Integer policyId) {
        Policy policy = policyRepository.findById(policyId)
                .orElseThrow(() -> new IllegalArgumentException("약관/방침을 찾을 수 없습니다."));
        return new PolicyResponseDto(policy);
    }

    @Transactional
    public PolicyResponseDto createPolicy(PolicyRequestDto requestDto) {
        if (requestDto.getIsActive() != null && requestDto.getIsActive()) {
            deactivateOtherPolicies(requestDto.getType());
        }

        Policy policy = requestDto.toEntity();
        Policy savedPolicy = policyRepository.save(policy);
        return new PolicyResponseDto(savedPolicy);
    }

    @Transactional
    public PolicyResponseDto updatePolicy(Integer policyId, PolicyRequestDto requestDto) {
        Policy policy = policyRepository.findById(policyId)
                .orElseThrow(() -> new IllegalArgumentException("약관/방침을 찾을 수 없습니다."));

        if (requestDto.getIsActive() != null && requestDto.getIsActive()) {
            deactivateOtherPolicies(requestDto.getType(), policyId);
        }

        policy.setType(requestDto.getType());
        policy.setVersion(requestDto.getVersion());
        policy.setContent(requestDto.getContent());
        
        if (requestDto.getIsActive() != null) {
            policy.setIsActive(requestDto.getIsActive());
        }

        return new PolicyResponseDto(policy);
    }

    @Transactional
    public void deletePolicy(Integer policyId) {
        if (!policyRepository.existsById(policyId)) {
            throw new IllegalArgumentException("약관/방침을 찾을 수 없습니다.");
        }
        policyRepository.deleteById(policyId);
    }

    @Transactional
    public PolicyResponseDto updateVisibility(Integer policyId, Boolean isActive) {
        Policy policy = policyRepository.findById(policyId)
                .orElseThrow(() -> new IllegalArgumentException("약관/방침을 찾을 수 없습니다."));

        if (isActive) {
            deactivateOtherPolicies(policy.getType(), policyId);
        }

        policy.setIsActive(isActive);
        return new PolicyResponseDto(policy);
    }

    private void deactivateOtherPolicies(PolicyType type) {
        deactivateOtherPolicies(type, null);
    }

    private void deactivateOtherPolicies(PolicyType type, Integer excludePolicyId) {
        List<Policy> activePolicies = policyRepository.findByTypeAndIsActiveTrue(type);
        for (Policy p : activePolicies) {
            if (excludePolicyId == null || !p.getPolicyId().equals(excludePolicyId)) {
                p.setIsActive(false);
            }
        }
    }
}
