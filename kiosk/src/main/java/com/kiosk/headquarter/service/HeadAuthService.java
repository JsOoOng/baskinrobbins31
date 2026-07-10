package com.kiosk.headquarter.service;

import com.kiosk.common.config.JwtUtil;
import com.kiosk.headquarter.dto.auth.HeadLoginEmployeeDTO;
import com.kiosk.headquarter.dto.auth.HeadLoginRequest;
import com.kiosk.headquarter.dto.auth.HeadLoginResponse;
import com.kiosk.headquarter.repository.HeadAuthMapper;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class HeadAuthService {

    private final HeadAuthMapper headAuthMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public HeadAuthService(
            HeadAuthMapper headAuthMapper,
            PasswordEncoder passwordEncoder,
            JwtUtil jwtUtil
    ) {
        this.headAuthMapper = headAuthMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @Transactional
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

        // 3. 본사 권한 검사
        if (!"HEAD_ADMIN".equals(employee.getRole())
                && !"ADMIN".equals(employee.getRole())
                && !"SUPER_ADMIN".equals(employee.getRole())) {
            throw new IllegalArgumentException("본사 관리자 권한이 없습니다.");
        }

        // 4. 계정 상태 검사
        if (!"ACTIVE".equals(employee.getStatus())) {
            throw new IllegalArgumentException("비활성화된 계정입니다.");
        }

        // 5. 비밀번호 검사
        if (!isPasswordMatched(request.getPassword(), employee.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        // 6. 응답 DTO 생성
        HeadLoginResponse loginUser = employee.toResponse();

        // HEAD_ADMIN이면 JWT 안에서는 ADMIN으로 통일
        if ("HEAD_ADMIN".equals(loginUser.getRole())) {
            loginUser.setRole("ADMIN");
        }

        // 7. 기존 평문 비밀번호면 로그인 성공 시 BCrypt로 자동 변경
        if (!isBCrypt(employee.getPassword())) {
            String encodedPassword = passwordEncoder.encode(request.getPassword());

            headAuthMapper.updatePassword(
                    employee.getEmployeeId(),
                    encodedPassword
            );
        }

        // 8. JWT 발급
        String token = jwtUtil.createToken(loginUser);

        // 9. 응답에 토큰 저장
        loginUser.setToken(token);

        return loginUser;
    }

    private boolean isPasswordMatched(String rawPassword, String savedPassword) {
        if (savedPassword == null) {
            return false;
        }

        if (isBCrypt(savedPassword)) {
            return passwordEncoder.matches(rawPassword, savedPassword);
        }

        // 기존 테스트 계정이 1234 같은 평문인 경우
        return rawPassword.equals(savedPassword);
    }

    private boolean isBCrypt(String password) {
        return password.startsWith("$2a$")
                || password.startsWith("$2b$")
                || password.startsWith("$2y$");
    }
}