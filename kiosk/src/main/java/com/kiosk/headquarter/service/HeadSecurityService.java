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

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HeadSecurityService {

    private final HeadAdminMapper
            headAdminMapper;

    private final PasswordEncoder
            passwordEncoder;

    /*
     * 관리자 목록
     */
    public List<HeadAdminResponse> getAdmins() {

        return headAdminMapper
                .findAllByOrderByIdDesc()
                .stream()
                .map(HeadAdminResponse::from)
                .toList();
    }

    /*
     * 관리자 상세
     */
    public HeadAdminResponse getAdmin(
            Integer adminId
    ) {

        return HeadAdminResponse.from(
                findAdmin(adminId)
        );
    }

    /*
     * 관리자 생성
     */
    @Transactional
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
    public HeadAdminResponse updateAdmin(
            Integer currentAdminId,
            Integer targetAdminId,
            HeadAdminUpdateRequest request
    ) {

        if (request == null) {
            throw new IllegalArgumentException(
                    "수정 정보를 입력해주세요."
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
         * 자신의 역할을 직접 변경하면 현재 JWT 권한과
         * DB 권한이 달라지므로 차단합니다.
         */
        if (
                currentAdminId.equals(targetAdminId) &&
                targetAdmin.getRole() != request.getRole()
        ) {
            throw new IllegalArgumentException(
                    "현재 로그인 중인 자신의 역할은 변경할 수 없습니다."
            );
        }

        /*
         * 마지막 활성 SUPER_ADMIN 강등 방지
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
     * 활성화·비활성화
     */
    @Transactional
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
                    "변경할 계정 상태를 선택해주세요."
            );
        }

        HeadquarterAdmin targetAdmin =
                findAdmin(targetAdminId);

        /*
         * 자기 계정 비활성화 방지
         */
        if (
                currentAdminId.equals(targetAdminId) &&
                request.getStatus()
                        == AdminStatus.INACTIVE
        ) {
            throw new IllegalArgumentException(
                    "현재 로그인 중인 계정은 비활성화할 수 없습니다."
            );
        }

        /*
         * 마지막 활성 SUPER_ADMIN 비활성화 방지
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
     * 비밀번호 초기화
     */
    @Transactional
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

        targetAdmin.changePassword(
                passwordEncoder.encode(
                        request.getNewPassword()
                )
        );
    }

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

        if (request.getPassword().length() < 4) {
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
     * 활성 상태 SUPER_ADMIN이 한 명뿐이면
     * 강등하거나 비활성화할 수 없습니다.
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