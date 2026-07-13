package com.kiosk.headquarter.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kiosk.common.config.JwtUtil;
import com.kiosk.headquarter.dto.auth.HeadLoginEmployeeDTO;
import com.kiosk.headquarter.dto.auth.HeadLoginRequest;
import com.kiosk.headquarter.dto.auth.HeadLoginResponse;
import com.kiosk.headquarter.repository.HeadAuthMapper;

@Service
@Transactional(readOnly = true)
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

    /*
     * 본사 관리자 로그인
     */
    @Transactional
    public HeadLoginResponse login(
            HeadLoginRequest request
    ) {

        // 1. 요청 객체 검사
        if (request == null) {
            throw new IllegalArgumentException(
                    "로그인 정보를 입력해주세요."
            );
        }

        // 2. 로그인 ID 검사
        if (request.getLoginId() == null
                || request.getLoginId().isBlank()) {

            throw new IllegalArgumentException(
                    "로그인 ID를 입력해야 합니다."
            );
        }

        // 3. 비밀번호 검사
        if (request.getPassword() == null
                || request.getPassword().isBlank()) {

            throw new IllegalArgumentException(
                    "비밀번호를 입력해야 합니다."
            );
        }

        String loginId =
                request.getLoginId().trim();

        // 4. 본사 관리자 계정 조회
        HeadLoginEmployeeDTO employee =
                headAuthMapper.findEmployeeByLoginId(
                        loginId
                );

        if (employee == null) {
            throw new IllegalArgumentException(
                    "존재하지 않는 계정입니다."
            );
        }

        // 개발 중 확인용
        System.out.println(
                "[본사 로그인 조회]"
                        + " adminId=" + employee.getEmployeeId()
                        + ", loginId=[" + employee.getLoginId() + "]"
                        + ", role=[" + employee.getRole() + "]"
                        + ", status=[" + employee.getStatus() + "]"
        );

        // 5. 본사 관리자 권한 검사
        if (!isHeadRole(employee.getRole())) {
            throw new IllegalArgumentException(
                    "본사 관리자 권한이 없습니다."
            );
        }

        // 6. 본사 관리자 계정 상태 검사
        if (!isActiveStatus(employee.getStatus())) {
            throw new IllegalArgumentException(
                    "비활성화된 계정입니다."
            );
        }

        // 7. 비밀번호 검사
        if (!isPasswordMatched(
                request.getPassword(),
                employee.getPassword()
        )) {
            throw new IllegalArgumentException(
                    "비밀번호가 일치하지 않습니다."
            );
        }

        /*
         * 8. 기존 평문 비밀번호라면
         * 로그인 성공 직후 BCrypt로 변경
         */
        if (!isBCrypt(employee.getPassword())) {

            String encodedPassword =
                    passwordEncoder.encode(
                            request.getPassword()
                    );

            headAuthMapper.updatePassword(
                    employee.getEmployeeId(),
                    encodedPassword
            );
        }

        // 9. 로그인 응답 DTO 생성
        HeadLoginResponse loginUser =
                employee.toResponse();

        /*
         * DB의 역할을 그대로 사용합니다.
         *
         * ADMIN
         * HEAD_ADMIN
         * SUPER_ADMIN
         */

        // 10. JWT 생성
        String token =
                jwtUtil.createToken(loginUser);

        // 11. 응답 DTO에 토큰 저장
        loginUser.setToken(token);

        return loginUser;
    }

    /*
     * 현재 로그인 중인 본사 관리자 정보 조회
     *
     * GET /head/auth/me
     */
    public HeadLoginResponse getLoginUser(
            Integer employeeId
    ) {

        if (employeeId == null) {
            throw new IllegalArgumentException(
                    "관리자 번호가 없습니다."
            );
        }

        HeadLoginEmployeeDTO employee =
                headAuthMapper.findEmployeeByEmployeeId(
                        employeeId
                );

        if (employee == null) {
            throw new IllegalArgumentException(
                    "존재하지 않는 본사 관리자입니다."
            );
        }

        // 로그인과 같은 권한 검사 메서드 사용
        if (!isHeadRole(employee.getRole())) {
            throw new IllegalArgumentException(
                    "본사 관리자 권한이 없습니다."
            );
        }

        // 로그인과 같은 상태 검사 메서드 사용
        if (!isActiveStatus(employee.getStatus())) {
            throw new IllegalArgumentException(
                    "비활성화된 계정입니다."
            );
        }

        HeadLoginResponse response =
                employee.toResponse();

        /*
         * /me에서는 토큰을 새로 만들지 않습니다.
         * Vue는 기존 localStorage 토큰을 유지합니다.
         */
        response.setToken(null);

        return response;
    }

    /*
     * 본사 관리자 권한 검사
     */
    private boolean isHeadRole(String role) {

        if (role == null) {
            return false;
        }

        String normalizedRole =
                role.trim().toUpperCase();

        return "ADMIN".equals(normalizedRole)
                || "HEAD_ADMIN".equals(normalizedRole)
                || "SUPER_ADMIN".equals(normalizedRole);
    }

    /*
     * 본사 관리자 상태 검사
     *
     * headquarter_admins 테이블은
     * ACTIVE 상태를 사용합니다.
     */
    private boolean isActiveStatus(String status) {

        if (status == null) {
            return false;
        }

        return "ACTIVE".equalsIgnoreCase(
                status.trim()
        );
    }

    /*
     * 비밀번호 검사
     */
    private boolean isPasswordMatched(
            String rawPassword,
            String savedPassword
    ) {

        if (savedPassword == null) {
            return false;
        }

        if (isBCrypt(savedPassword)) {
            return passwordEncoder.matches(
                    rawPassword,
                    savedPassword
            );
        }

        /*
         * 기존 개발용 평문 계정 지원
         *
         * 예: 1234
         */
        return rawPassword.equals(
                savedPassword
        );
    }

    /*
     * BCrypt 문자열 여부 확인
     */
    private boolean isBCrypt(String password) {

        if (password == null) {
            return false;
        }

        return password.startsWith("$2a$")
                || password.startsWith("$2b$")
                || password.startsWith("$2y$");
    }
}