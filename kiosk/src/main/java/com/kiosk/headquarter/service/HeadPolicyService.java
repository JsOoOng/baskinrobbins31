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

/**
 * [코드 흐름 안내] HeadPolicyService
 *
 * <p>역할: 본사 관리의 운영 정책 업무 규칙과 상태 변경을 처리한다.</p>
 * <p>호출 흐름: Controller 호출 -> 이 서비스 -> HeadPolicyRepository -> Entity/DTO 변환 -> Controller 반환 순서로 동작한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
@Service
@RequiredArgsConstructor
public class HeadPolicyService {

    private final HeadPolicyRepository policyRepository;

    @Transactional(readOnly = true)
    /**
     * [메서드 흐름] getAllPolicies
     * Controller 또는 상위 서비스에서 호출되어 HeadPolicyRepository을 사용해 검증·조회·저장 등의 처리를 수행하고 결과를 반환한다.
     */
    public List<PolicyResponseDto> getAllPolicies() {
        return policyRepository.findAll()
                .stream()
                .map(PolicyResponseDto::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    /**
     * [메서드 흐름] getPoliciesByType
     * Controller 또는 상위 서비스에서 호출되어 HeadPolicyRepository을 사용해 검증·조회·저장 등의 처리를 수행하고 결과를 반환한다.
     */
    public List<PolicyResponseDto> getPoliciesByType(PolicyType type) {
        return policyRepository.findByType(type)
                .stream()
                .map(PolicyResponseDto::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    /**
     * [메서드 흐름] getPolicy
     * Controller 또는 상위 서비스에서 호출되어 HeadPolicyRepository을 사용해 검증·조회·저장 등의 처리를 수행하고 결과를 반환한다.
     */
    public PolicyResponseDto getPolicy(Integer policyId) {
        Policy policy = policyRepository.findById(policyId)
                .orElseThrow(() -> new IllegalArgumentException("약관/방침을 찾을 수 없습니다."));
        return new PolicyResponseDto(policy);
    }

    @Transactional
    /**
     * [메서드 흐름] createPolicy
     * Controller 또는 상위 서비스에서 호출되어 HeadPolicyRepository을 사용해 검증·조회·저장 등의 처리를 수행하고 결과를 반환한다.
     */
    public PolicyResponseDto createPolicy(PolicyRequestDto requestDto) {
        if (requestDto.getIsActive() != null && requestDto.getIsActive()) {
            deactivateOtherPolicies(requestDto.getType());
        }

        Policy policy = requestDto.toEntity();
        Policy savedPolicy = policyRepository.save(policy);
        return new PolicyResponseDto(savedPolicy);
    }

    @Transactional
    /**
     * [메서드 흐름] updatePolicy
     * Controller 또는 상위 서비스에서 호출되어 HeadPolicyRepository을 사용해 검증·조회·저장 등의 처리를 수행하고 결과를 반환한다.
     */
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
    /**
     * [메서드 흐름] deletePolicy
     * Controller 또는 상위 서비스에서 호출되어 HeadPolicyRepository을 사용해 검증·조회·저장 등의 처리를 수행하고 결과를 반환한다.
     */
    public void deletePolicy(Integer policyId) {
        if (!policyRepository.existsById(policyId)) {
            throw new IllegalArgumentException("약관/방침을 찾을 수 없습니다.");
        }
        policyRepository.deleteById(policyId);
    }

    @Transactional
    /**
     * [메서드 흐름] updateVisibility
     * Controller 또는 상위 서비스에서 호출되어 HeadPolicyRepository을 사용해 검증·조회·저장 등의 처리를 수행하고 결과를 반환한다.
     */
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
