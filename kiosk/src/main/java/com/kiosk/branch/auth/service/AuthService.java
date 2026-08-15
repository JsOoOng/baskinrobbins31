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
 * <p>호출 흐름: Controller 호출 -> 이 서비스 -> EmployeeMapper, PasswordEncoder,
 * LoginAttemptService -> Entity/DTO 변환 -> Controller 반환 순서로 동작한다.</p>
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private static final String LOGIN_FAILED_MESSAGE =
            "로그인에 실패하였습니다.";

    private final EmployeeMapper employeeMapper;
    private final PasswordEncoder passwordEncoder;

    // 로그인 실패 횟수 및 일시적 차단 관리
    private final LoginAttemptService loginAttemptService;


    /**
     * [메서드 흐름] login
     *
     * <p>
     * 로그인 ID와 비밀번호를 검증하고,
     * 로그인 실패 횟수 제한 및 차단 상태를 확인한다.
     * </p>
     */
    public AuthResponse login(AuthRequest request) {

        // 1. 요청값 기본 검증
        if (request == null
                || request.getLoginId() == null
                || request.getLoginId().isBlank()
                || request.getPassword() == null
                || request.getPassword().isBlank()) {

            throw new IllegalArgumentException(LOGIN_FAILED_MESSAGE);
        }


        // 2. 로그인 ID 정리
        String loginId = request.getLoginId().trim();


        // 3. 로그인 ID로 직원 조회
        //
        // 존재하지 않는 ID는 여기서 바로 실패한다.
        // 따라서 로그인 실패 횟수에는 포함하지 않는다.
        Employee employee = employeeMapper
                .findByLoginId(loginId)
                .orElseThrow(() ->
                        new IllegalArgumentException(LOGIN_FAILED_MESSAGE)
                );


        // 4. 로그인 차단 여부 확인
        //
        // 실제 존재하는 계정에 대해서만 확인한다.
        if (loginAttemptService.isBlocked(loginId)) {

            throw new IllegalArgumentException(LOGIN_FAILED_MESSAGE);
        }


        // 5. 비밀번호 검증
        if (!matchesPassword(
                request.getPassword(),
                employee.getPassword()
        )) {

            // 존재하는 계정의 비밀번호가 틀린 경우에만
            // 로그인 실패 횟수를 증가시킨다.
            loginAttemptService.loginFailed(loginId);

            throw new IllegalArgumentException(LOGIN_FAILED_MESSAGE);
        }


        // 6. 직원 상태 및 권한 검증
        if (employee.getStatus() != EmployeeStatus.EMPLOYED
                || (employee.getRole() != Role.MANAGER
                    && employee.getRole() != Role.STAFF)
                || employee.getStore() == null) {

            throw new IllegalArgumentException(LOGIN_FAILED_MESSAGE);
        }


        // 7. 정상 로그인 성공
        //
        // 이전에 실패한 기록이 있었다면 초기화한다.
        loginAttemptService.loginSucceeded(loginId);


        // 8. 로그인 응답 생성
        return AuthResponse.builder()
                .employeeId(employee.getId())
                .name(employee.getName())
                .role(employee.getRole().name())
                .storeId(employee.getStore().getId())
                .storeName(employee.getStore().getStoreName())
                .build();
    }


    /**
     * 지점 비밀번호는 BCrypt 해시만 허용하며
     * 평문 저장값은 로그인시키지 않는다.
     */
    private boolean matchesPassword(
            String rawPassword,
            String storedPassword
    ) {

        return storedPassword != null
                && (storedPassword.startsWith("$2a$")
                    || storedPassword.startsWith("$2b$")
                    || storedPassword.startsWith("$2y$"))
                && passwordEncoder.matches(
                        rawPassword,
                        storedPassword
                );
    }
}