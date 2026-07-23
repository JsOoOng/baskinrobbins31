package com.kiosk.branch.auth.service;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.kiosk.branch.auth.dto.AuthRequest;
import com.kiosk.branch.auth.dto.AuthResponse;
import com.kiosk.branch.auth.repository.EmployeeMapper;
import com.kiosk.entity.Employee;
import com.kiosk.entity.enums.EmployeeStatus;
import com.kiosk.entity.enums.Role;

import lombok.RequiredArgsConstructor;

/**
 * [코드 흐름 안내] AuthService
 *
 * <p>역할: 지점 운영의 인증 업무 규칙과 상태 변경을 처리한다.</p>
 * <p>호출 흐름: Controller 호출 -> 이 서비스 -> EmployeeMapper, PasswordEncoder -> Entity/DTO 변환 -> Controller 반환 순서로 동작한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private static final String LOGIN_FAILED_MESSAGE =
            "아이디 또는 비밀번호가 일치하지 않습니다.";

    private final EmployeeMapper employeeMapper;
    private final PasswordEncoder passwordEncoder;


    /**
     * [메서드 흐름] login
     * Controller 또는 상위 서비스에서 호출되어 EmployeeMapper, PasswordEncoder을 사용해 검증·조회·저장 등의 처리를 수행하고 결과를 반환한다.
     */
    public AuthResponse login(AuthRequest request) {

        if (request == null
                || request.getLoginId() == null
                || request.getLoginId().isBlank()
                || request.getPassword() == null
                || request.getPassword().isBlank()) {
            throw new IllegalArgumentException(LOGIN_FAILED_MESSAGE);
        }

        Employee employee = employeeMapper
                .findByLoginId(request.getLoginId().trim())
                .orElseThrow(() ->
                        new IllegalArgumentException(LOGIN_FAILED_MESSAGE)
                );

        if (!isBCrypt(employee.getPassword())
                || !passwordEncoder.matches(
                        request.getPassword(),
                        employee.getPassword()
                )) {
            throw new IllegalArgumentException(LOGIN_FAILED_MESSAGE);
        }

        if (employee.getStatus() != EmployeeStatus.EMPLOYED
                || (employee.getRole() != Role.MANAGER
                    && employee.getRole() != Role.STAFF)
                || employee.getStore() == null) {
            throw new IllegalArgumentException(LOGIN_FAILED_MESSAGE);
        }


        return AuthResponse.builder()
                .employeeId(employee.getId())
                .name(employee.getName())
                .role(employee.getRole().name())
                .storeId(employee.getStore().getId())
                .storeName(employee.getStore().getStoreName())
                .build();
    }

    private boolean isBCrypt(String password) {

        return password != null
                && (password.startsWith("$2a$")
                    || password.startsWith("$2b$")
                    || password.startsWith("$2y$"));
    }

}
