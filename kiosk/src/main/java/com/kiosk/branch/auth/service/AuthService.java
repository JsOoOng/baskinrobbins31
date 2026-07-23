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

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private static final String LOGIN_FAILED_MESSAGE =
            "아이디 또는 비밀번호가 일치하지 않습니다.";

    private final EmployeeMapper employeeMapper;
    private final PasswordEncoder passwordEncoder;


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
