package com.kiosk.branch.auth.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.kiosk.branch.auth.dto.AuthRequest;
import com.kiosk.branch.auth.dto.AuthResponse;
import com.kiosk.branch.auth.exception.LoginAttemptException;
import com.kiosk.branch.auth.repository.EmployeeMapper;
import com.kiosk.entity.Employee;
import com.kiosk.entity.enums.EmployeeStatus;
import com.kiosk.entity.enums.Role;

import lombok.RequiredArgsConstructor;

/**
 * [코드 흐름 안내] AuthService
 *
 * <p>역할: 지점 운영의 인증 업무 규칙과 상태 변경을 처리한다.</p>
 *
 * <p>
 * 호출 흐름:
 * Controller
 * -> AuthService
 * -> EmployeeMapper / PasswordEncoder / LoginAttemptService
 * -> Entity/DTO 변환
 * -> Controller 반환
 * </p>
 *
 * <p>
 * 로그인 실패 횟수 제한:
 * 존재하는 로그인 ID에 대해서만 실패 횟수를 기록한다.
 * 5회 실패 시 LoginAttemptService에서 일시적으로 로그인을 차단한다.
 * </p>
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    /**
     * 로그인 실패 시 사용자에게 반환할 고정 메시지
     *
     * 존재하지 않는 ID인지,
     * 비밀번호가 틀렸는지,
     * 계정 상태가 잘못되었는지
     * 구분하지 않도록 동일한 메시지를 사용한다.
     */
    private static final String LOGIN_FAILED_MESSAGE =
            "로그인에 실패하였습니다.";


    /**
     * 직원 조회 Mapper
     */
    private final EmployeeMapper employeeMapper;


    /**
     * BCrypt 비밀번호 검증 객체
     */
    private final PasswordEncoder passwordEncoder;


    /**
     * 로그인 실패 횟수 및 일시적인 로그인 차단 관리
     */
    private final LoginAttemptService loginAttemptService;


    /**
     * [메서드 흐름] login
     *
     * <p>
     * 로그인 ID와 비밀번호를 검증하고,
     * 로그인 실패 횟수 제한 및 차단 상태를 확인한다.
     * </p>
     *
     * <p>처리 순서</p>
     * <ol>
     *     <li>요청값 검증</li>
     *     <li>로그인 ID 정리</li>
     *     <li>직원 계정 조회</li>
     *     <li>로그인 차단 여부 확인</li>
     *     <li>비밀번호 검증</li>
     *     <li>직원 상태 및 권한 검증</li>
     *     <li>로그인 성공 시 실패 기록 초기화</li>
     *     <li>로그인 응답 생성</li>
     * </ol>
     */
    public AuthResponse login(AuthRequest request) {

        // =========================================================
        // 1. 요청값 기본 검증
        // =========================================================

        if (request == null
                || request.getLoginId() == null
                || request.getLoginId().isBlank()
                || request.getPassword() == null
                || request.getPassword().isBlank()) {

            throw new IllegalArgumentException(
                    LOGIN_FAILED_MESSAGE
            );
        }


        // =========================================================
        // 2. 로그인 ID 정리
        // =========================================================

        String loginId = request.getLoginId().trim();


        // =========================================================
        // 3. 로그인 ID로 직원 조회
        // =========================================================
        //
        // 존재하지 않는 ID는 여기서 바로 실패한다.
        //
        // 따라서 존재하지 않는 ID를 대상으로 무작위 로그인 시도를
        // 하더라도 LoginAttemptService의 실패 횟수에는 포함되지 않는다.
        //
        // 또한 사용자에게는 "존재하지 않는 ID"라는 정보를
        // 노출하지 않고 동일한 로그인 실패 메시지를 반환한다.
        // =========================================================

        Employee employee = employeeMapper
                .findByLoginId(loginId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                LOGIN_FAILED_MESSAGE
                        )
                );


        // =========================================================
        // 4. 로그인 차단 여부 확인
        // =========================================================
        //
        // 실제 존재하는 계정에 대해서만 확인한다.
        //
        // 이미 5회 실패하여 차단된 경우에는
        // 비밀번호 검증을 수행하지 않는다.
        // =========================================================

        if (loginAttemptService.isBlocked(loginId)) {

            int failedAttempts =
                    loginAttemptService.getFailedCount(loginId);

            throw new LoginAttemptException(
                    "로그인 실패 횟수를 초과했습니다. 10분 후 다시 시도해주세요.",
                    failedAttempts,
                    5,
                    true
            );
        }


        // =========================================================
        // 5. 비밀번호 검증
        // =========================================================

        if (!matchesPassword(
                request.getPassword(),
                employee.getPassword()
        )) {

            /*
             * 존재하는 계정의 비밀번호가 틀린 경우에만
             * 로그인 실패 횟수를 증가시킨다.
             *
             * loginFailed()에서 실패 횟수가 증가하고
             * 5회째라면 차단 상태가 된다.
             */
            LoginAttemptService.LoginAttemptResult result =
                    loginAttemptService.loginFailed(loginId);


            /*
             * 실패 횟수와 차단 여부를
             * LoginAttemptException에 담아서 전달한다.
             *
             * GlobalExceptionHandler가 이 값을 JSON으로
             * 변환하여 Vue에 전달한다.
             */
            throw new LoginAttemptException(
                    result.isBlocked()
                            ? "로그인 실패 횟수를 초과했습니다. 10분 후 다시 시도해주세요."
                            : "아이디 또는 비밀번호가 올바르지 않습니다.",
                    result.getFailedAttempts(),
                    result.getMaxAttempts(),
                    result.isBlocked()
            );
        }


        // =========================================================
        // 6. 직원 상태 및 권한 검증
        // =========================================================

        if (employee.getStatus() != EmployeeStatus.EMPLOYED
                || (employee.getRole() != Role.MANAGER
                    && employee.getRole() != Role.STAFF)
                || employee.getStore() == null) {

            /*
             * 계정 상태 또는 권한이 올바르지 않은 경우에도
             * 사용자에게 상세한 계정 정보를 노출하지 않는다.
             */
            throw new IllegalArgumentException(
                    LOGIN_FAILED_MESSAGE
            );
        }


        // =========================================================
        // 7. 정상 로그인 성공
        // =========================================================
        //
        // 이전에 로그인 실패 기록이 있었다면 초기화한다.
        //
        // 예:
        //
        // 3회 실패
        // -> 올바른 비밀번호 입력
        // -> 로그인 성공
        // -> 실패 횟수 0으로 초기화
        // =========================================================

        loginAttemptService.loginSucceeded(loginId);


        // =========================================================
        // 8. 로그인 응답 생성
        // =========================================================

        return AuthResponse.builder()
                .employeeId(employee.getId())
                .name(employee.getName())
                .role(employee.getRole().name())
                .storeId(employee.getStore().getId())
                .storeName(employee.getStore().getStoreName())
                .build();
    }


    /**
     * 지점 비밀번호 검증
     *
     * <p>
     * DB에 저장된 비밀번호가 BCrypt 해시인지 먼저 확인한 뒤
     * PasswordEncoder를 이용하여 입력된 비밀번호와 비교한다.
     * </p>
     *
     * <p>
     * 평문 비밀번호는 허용하지 않는다.
     * </p>
     *
     * @param rawPassword 사용자가 입력한 평문 비밀번호
     * @param storedPassword DB에 저장된 BCrypt 해시
     * @return 비밀번호가 일치하면 true
     */
    private boolean matchesPassword(
            String rawPassword,
            String storedPassword
    ) {

        return storedPassword != null
                && (
                    storedPassword.startsWith("$2a$")
                    || storedPassword.startsWith("$2b$")
                    || storedPassword.startsWith("$2y$")
                )
                && passwordEncoder.matches(
                        rawPassword,
                        storedPassword
                );
    }
}