package com.kiosk.headquarter.service;

import com.kiosk.headquarter.dto.employee.HeadEmployeeCreateRequest;
import com.kiosk.headquarter.dto.employee.HeadEmployeeCreateResponse;
import com.kiosk.headquarter.dto.employee.HeadEmployeeInsertDTO;
import com.kiosk.headquarter.mapper.HeadEmployeeMapper;

import org.springframework.stereotype.Service;

@Service
public class HeadEmployeeService {

    private final HeadEmployeeMapper headEmployeeMapper;

    public HeadEmployeeService(HeadEmployeeMapper headEmployeeMapper) {
        this.headEmployeeMapper = headEmployeeMapper;
    }

    public HeadEmployeeCreateResponse createStoreAdmin(HeadEmployeeCreateRequest request) {

        if (request.getStoreId() == null) {
            throw new IllegalArgumentException("지점 ID를 입력해야 합니다.");
        }

        if (request.getLoginId() == null || request.getLoginId().isBlank()) {
            throw new IllegalArgumentException("로그인 ID를 입력해야 합니다.");
        }

        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new IllegalArgumentException("비밀번호를 입력해야 합니다.");
        }

        if (request.getName() == null || request.getName().isBlank()) {
            throw new IllegalArgumentException("담당자 이름을 입력해야 합니다.");
        }

        int storeCount = headEmployeeMapper.countStoreById(request.getStoreId());

        if (storeCount == 0) {
            throw new IllegalArgumentException("존재하지 않는 지점입니다.");
        }

        int loginIdCount = headEmployeeMapper.countEmployeeByLoginId(request.getLoginId());

        if (loginIdCount > 0) {
            throw new IllegalArgumentException("이미 사용 중인 로그인 ID입니다.");
        }

        HeadEmployeeInsertDTO employee = new HeadEmployeeInsertDTO();

        employee.setStoreId(request.getStoreId());
        employee.setLoginId(request.getLoginId());
        employee.setPassword(request.getPassword());
        employee.setName(request.getName());

        // 지점 관리자 계정으로 고정
        employee.setRole("STORE_ADMIN");
        employee.setStatus("ACTIVE");

        headEmployeeMapper.insertStoreAdminEmployee(employee);

        return employee.toResponse();
    }
}