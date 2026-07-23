package com.kiosk.headquarter.service;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kiosk.entity.HeadquarterAdmin;
import com.kiosk.entity.enums.AdminRole;
import com.kiosk.entity.enums.AdminStatus;
import com.kiosk.headquarter.dto.security.HeadAdminCreateRequest;
import com.kiosk.headquarter.dto.security.HeadAdminPasswordRequest;
import com.kiosk.headquarter.dto.security.HeadAdminResponse;
import com.kiosk.headquarter.dto.security.HeadAdminStatusRequest;
import com.kiosk.headquarter.dto.security.HeadAdminUpdateRequest;
import com.kiosk.headquarter.repository.HeadAdminMapper;

import lombok.RequiredArgsConstructor;

/**
 * [코드 흐름 안내] HeadSecurityService
 *
 * <p>역할: 본사 관리의 보안 업무 규칙과 상태 변경을 처리한다.</p>
 * <p>호출 흐름: Controller 호출 -> 이 서비스 -> HeadAdminMapper, PasswordEncoder -> Entity/DTO 변환 -> Controller 반환 순서로 동작한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HeadSecurityService {

    private final HeadAdminMapper headAdminMapper;

    private final PasswordEncoder passwordEncoder;

    /*
     * 전체 관리자 목록 조회
     */
    /**
     * [메서드 흐름] getAdmins
     * Controller 또는 상위 서비스에서 호출되어 HeadAdminMapper, PasswordEncoder을 사용해 검증·조회·저장 등의 처리를 수행하고 결과를 반환한다.
     */
    public List<HeadAdminResponse> getAdmins() {

        return headAdminMapper
                .findAllByOrderByIdDesc()
                .stream()
                .map(HeadAdminResponse::from)
                .toList();
    }

    /*
     * 관리자 상세 조회
     */
    /**
     * [메서드 흐름] getAdmin
     * Controller 또는 상위 서비스에서 호출되어 HeadAdminMapper, PasswordEncoder을 사용해 검증·조회·저장 등의 처리를 수행하고 결과를 반환한다.
     */
    public HeadAdminResponse getAdmin(
            Integer adminId
    ) {

        HeadquarterAdmin admin =
                findAdmin(adminId);

        return HeadAdminResponse.from(admin);
    }

    /*
     * 본사 관리자 계정 생성
     */
    @Transactional
    /**
     * [메서드 흐름] createAdmin
     * Controller 또는 상위 서비스에서 호출되어 HeadAdminMapper, PasswordEncoder을 사용해 검증·조회·저장 등의 처리를 수행하고 결과를 반환한다.
     */
    public HeadAdminResponse createAdmin(
            HeadAdminCreateRequest request
    ) {

        validateCreateRequest(request);

        String loginId =
                request.getLoginId().trim();

        if (
                headAdminMapper
                        .existsByLoginId(loginId)
        ) {
            throw new IllegalArgumentException(
                    "이미 사용 중인 로그인 ID입니다."
            );
        }

        AdminStatus status =
                request.getStatus() != null
                        ? request.getStatus()
                        : AdminStatus.ACTIVE;

        HeadquarterAdmin admin =
                HeadquarterAdmin.builder()
                        .loginId(loginId)
                        .password(
                                passwordEncoder.encode(
                                        request.getPassword()
                                )
                        )
                        .name(
                                request.getName().trim()
                        )
                        .department(
                                normalizeDepartment(
                                        request.getDepartment()
                                )
                        )
                        .role(request.getRole())
                        .status(status)
                        .build();

        /*
         * INSERT를 바로 실행하고,
         * createdAt까지 반영된 Entity를 반환합니다.
         */
        HeadquarterAdmin savedAdmin =
                headAdminMapper.saveAndFlush(admin);

        return HeadAdminResponse.from(
                savedAdmin
        );
    }

    /*
     * 관리자 정보 수정
     */
    @Transactional
    /**
     * [메서드 흐름] updateAdmin
     * Controller 또는 상위 서비스에서 호출되어 HeadAdminMapper, PasswordEncoder을 사용해 검증·조회·저장 등의 처리를 수행하고 결과를 반환한다.
     */
    public HeadAdminResponse updateAdmin(
            Integer currentAdminId,
            Integer targetAdminId,
            HeadAdminUpdateRequest request
    ) {

        if (request == null) {
            throw new IllegalArgumentException(
                    "수정할 관리자 정보를 입력해주세요."
            );
        }

        if (
                request.getName() == null ||
                request.getName().isBlank()
        ) {
            throw new IllegalArgumentException(
                    "관리자 이름을 입력해주세요."
            );
        }

        if (request.getRole() == null) {
            throw new IllegalArgumentException(
                    "관리자 역할을 선택해주세요."
            );
        }

        HeadquarterAdmin targetAdmin =
                findAdmin(targetAdminId);

        /*
         * 현재 로그인한 자신의 역할 변경 방지
         */
        if (
                currentAdminId != null &&
                currentAdminId.equals(targetAdminId) &&
                targetAdmin.getRole()
                        != request.getRole()
        ) {
            throw new IllegalArgumentException(
                    "현재 로그인 중인 자신의 역할은 변경할 수 없습니다."
            );
        }

        /*
         * 마지막 활성 최고 관리자 강등 방지
         */
        if (
                targetAdmin.getRole()
                        == AdminRole.SUPER_ADMIN &&
                targetAdmin.getStatus()
                        == AdminStatus.ACTIVE &&
                request.getRole()
                        != AdminRole.SUPER_ADMIN
        ) {
            validateLastActiveSuperAdmin();
        }

        targetAdmin.updateAdmin(
                request.getName().trim(),
                normalizeDepartment(
                        request.getDepartment()
                ),
                request.getRole()
        );

        return HeadAdminResponse.from(
                targetAdmin
        );
    }

    /*
     * 관리자 활성·비활성 상태 변경
     */
    @Transactional
    /**
     * [메서드 흐름] changeStatus
     * Controller 또는 상위 서비스에서 호출되어 HeadAdminMapper, PasswordEncoder을 사용해 검증·조회·저장 등의 처리를 수행하고 결과를 반환한다.
     */
    public HeadAdminResponse changeStatus(
            Integer currentAdminId,
            Integer targetAdminId,
            HeadAdminStatusRequest request
    ) {

        if (
                request == null ||
                request.getStatus() == null
        ) {
            throw new IllegalArgumentException(
                    "변경할 관리자 상태를 선택해주세요."
            );
        }

        HeadquarterAdmin targetAdmin =
                findAdmin(targetAdminId);

        /*
         * 현재 로그인한 자기 계정 비활성화 방지
         */
        if (
                currentAdminId != null &&
                currentAdminId.equals(targetAdminId) &&
                request.getStatus()
                        == AdminStatus.INACTIVE
        ) {
            throw new IllegalArgumentException(
                    "현재 로그인 중인 계정은 비활성화할 수 없습니다."
            );
        }

        /*
         * 마지막 활성 최고 관리자 비활성화 방지
         */
        if (
                targetAdmin.getRole()
                        == AdminRole.SUPER_ADMIN &&
                targetAdmin.getStatus()
                        == AdminStatus.ACTIVE &&
                request.getStatus()
                        == AdminStatus.INACTIVE
        ) {
            validateLastActiveSuperAdmin();
        }

        targetAdmin.changeStatus(
                request.getStatus()
        );

        return HeadAdminResponse.from(
                targetAdmin
        );
    }

    /*
     * 관리자 비밀번호 초기화
     */
    @Transactional
    /**
     * [메서드 흐름] resetPassword
     * Controller 또는 상위 서비스에서 호출되어 HeadAdminMapper, PasswordEncoder을 사용해 검증·조회·저장 등의 처리를 수행하고 결과를 반환한다.
     */
    public void resetPassword(
            Integer targetAdminId,
            HeadAdminPasswordRequest request
    ) {

        if (
                request == null ||
                request.getNewPassword() == null ||
                request.getNewPassword().isBlank()
        ) {
            throw new IllegalArgumentException(
                    "새 비밀번호를 입력해주세요."
            );
        }

        if (
                request.getNewPassword().length() < 4
        ) {
            throw new IllegalArgumentException(
                    "비밀번호는 4자 이상이어야 합니다."
            );
        }

        HeadquarterAdmin targetAdmin =
                findAdmin(targetAdminId);

        String encodedPassword =
                passwordEncoder.encode(
                        request.getNewPassword()
                );

        targetAdmin.changePassword(
                encodedPassword
        );
    }

    /*
     * 관리자 Entity 조회
     */
    private HeadquarterAdmin findAdmin(
            Integer adminId
    ) {

        if (adminId == null) {
            throw new IllegalArgumentException(
                    "관리자 번호가 없습니다."
            );
        }

        return headAdminMapper
                .findById(adminId)
                .orElseThrow(
                        () ->
                                new IllegalArgumentException(
                                        "존재하지 않는 본사 관리자입니다."
                                )
                );
    }

    /*
     * 관리자 생성 요청 검사
     */
    private void validateCreateRequest(
            HeadAdminCreateRequest request
    ) {

        if (request == null) {
            throw new IllegalArgumentException(
                    "관리자 정보를 입력해주세요."
            );
        }

        if (
                request.getLoginId() == null ||
                request.getLoginId().isBlank()
        ) {
            throw new IllegalArgumentException(
                    "로그인 ID를 입력해주세요."
            );
        }

        if (
                request.getPassword() == null ||
                request.getPassword().isBlank()
        ) {
            throw new IllegalArgumentException(
                    "비밀번호를 입력해주세요."
            );
        }

        if (
                request.getPassword().length() < 4
        ) {
            throw new IllegalArgumentException(
                    "비밀번호는 4자 이상이어야 합니다."
            );
        }

        if (
                request.getName() == null ||
                request.getName().isBlank()
        ) {
            throw new IllegalArgumentException(
                    "관리자 이름을 입력해주세요."
            );
        }

        if (request.getRole() == null) {
            throw new IllegalArgumentException(
                    "관리자 역할을 선택해주세요."
            );
        }
    }

    /*
     * 마지막 활성 SUPER_ADMIN 보호
     */
    private void validateLastActiveSuperAdmin() {

        long activeSuperAdminCount =
                headAdminMapper
                        .countByRoleAndStatus(
                                AdminRole.SUPER_ADMIN,
                                AdminStatus.ACTIVE
                        );

        if (activeSuperAdminCount <= 1) {
            throw new IllegalArgumentException(
                    "마지막 활성 최고 관리자 계정은 변경할 수 없습니다."
            );
        }
    }

    /*
     * 빈 부서명은 NULL 처리
     */
    private String normalizeDepartment(
            String department
    ) {

        if (
                department == null ||
                department.isBlank()
        ) {
            return null;
        }

        return department.trim();
    }
}