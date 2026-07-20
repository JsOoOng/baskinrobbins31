package com.kiosk.headquarter.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kiosk.entity.Policy;
import com.kiosk.entity.enums.PolicyType;

public interface HeadPolicyRepository extends JpaRepository<Policy, Integer> {
    List<Policy> findByType(PolicyType type);
    List<Policy> findByTypeAndIsActiveTrue(PolicyType type);
}
