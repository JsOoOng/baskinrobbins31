package com.kiosk.branch.auth.service;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kiosk.branch.auth.dto.AuthRequest;
import com.kiosk.branch.auth.dto.AuthResponse;
import com.kiosk.branch.auth.repository.EmployeeMapper;
import com.kiosk.entity.Employee;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {


    private final EmployeeMapper employeeMapper;


    public AuthResponse login(AuthRequest request) {


        Employee employee = employeeMapper
                .findByLoginId(request.getLoginId())
                .orElseThrow(() -> 
                    new IllegalArgumentException("아이디가 존재하지 않습니다.")
                );


        if(!employee.getPassword()
                .equals(request.getPassword())) {

            throw new IllegalArgumentException(
                    "비밀번호가 틀렸습니다."
            );
        }


        return AuthResponse.builder()
                .employeeId(employee.getId())
                .name(employee.getName())
                .role(employee.getRole().name())
                .storeId(employee.getStore().getId())
                .storeName(employee.getStore().getStoreName())
                .build();
    }

}