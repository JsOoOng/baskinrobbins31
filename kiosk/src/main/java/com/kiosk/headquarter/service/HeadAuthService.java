package com.kiosk.headquarter.service;

import com.kiosk.headquarter.dto.auth.HeadLoginEmployeeDTO;
import com.kiosk.headquarter.dto.auth.HeadLoginRequest;
import com.kiosk.headquarter.dto.auth.HeadLoginResponse;
import com.kiosk.headquarter.repository.HeadAuthMapper;

import org.springframework.stereotype.Service;

@Service
public class HeadAuthService {

    private final HeadAuthMapper headAuthMapper;

    public HeadAuthService(HeadAuthMapper headAuthMapper) {
        this.headAuthMapper = headAuthMapper;
    }

    public HeadLoginResponse login(HeadLoginRequest request) {

        // 1. 입력값 검사
        if (request.getLoginId() == null || request.getLoginId().isBlank()) {
            throw new IllegalArgumentException("로그인 ID를 입력해야 합니다.");
        }

        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new IllegalArgumentException("비밀번호를 입력해야 합니다.");
        }

        // 2. DB에서 직원 계정 조회
        HeadLoginEmployeeDTO employee =
                headAuthMapper.findEmployeeByLoginId(request.getLoginId());

        if (employee == null) {
            throw new IllegalArgumentException("존재하지 않는 계정입니다.");
        }

        // 3. 비밀번호 검사
        // P0 테스트용: 평문 비교
        // 추후 BCryptPasswordEncoder로 교체해야 함
        if (!request.getPassword().equals(employee.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        // 4. 본사 권한 검사
        if (!"HEAD_ADMIN".equals(employee.getRole())
                && !"SUPER_ADMIN".equals(employee.getRole())) {
            throw new IllegalArgumentException("본사 관리자 권한이 없습니다.");
        }

        // 5. 계정 상태 검사
        if (!"ACTIVE".equals(employee.getStatus())) {
            throw new IllegalArgumentException("비활성화된 계정입니다.");
        }

        // 6. 비밀번호를 제외한 응답 DTO 반환
        return employee.toResponse();
    }
}