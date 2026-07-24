package com.kiosk.headquarter.service;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kiosk.entity.HeadquarterAdmin;
import com.kiosk.entity.enums.AdminRole;
import com.kiosk.entity.enums.AdminStatus;
import com.kiosk.headquarter.dto.admin.HeadAdminCreateRequestDTO;
import com.kiosk.headquarter.dto.admin.HeadAdminPasswordUpdateRequestDTO;
import com.kiosk.headquarter.dto.admin.HeadAdminResponseDTO;
import com.kiosk.headquarter.dto.admin.HeadAdminRoleUpdateRequestDTO;
import com.kiosk.headquarter.dto.admin.HeadAdminStatusUpdateRequestDTO;
import com.kiosk.headquarter.repository.HeadquarterAdminMapper;

import lombok.RequiredArgsConstructor;

/**
 * [코드 흐름 안내] HeadAdminSecurityService
 *
 * <p>역할: 본사 관리의 본사 관리자 계정 업무 규칙과 상태 변경을 처리한다.</p>
 * <p>호출 흐름: Controller 호출 -> 이 서비스 -> HeadquarterAdminMapper, PasswordEncoder -> Entity/DTO 변환 -> Controller 반환 순서로 동작한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HeadAdminSecurityService {

    private final HeadquarterAdminMapper headquarterAdminMapper;
    private final PasswordEncoder passwordEncoder;
    private final AdminLogService adminLogService;

    /**
     * [메서드 흐름] getAdminList
     * Controller 또는 상위 서비스에서 호출되어 HeadquarterAdminMapper, PasswordEncoder을 사용해 검증·조회·저장 등의 처리를 수행하고 결과를 반환한다.
     */
    public List<HeadAdminResponseDTO> getAdminList() {
        getCurrentSuperAdmin();

        return headquarterAdminMapper.findAllByOrderByIdDesc()
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    /**
     * [메서드 흐름] getAdminDetail
     * Controller 또는 상위 서비스에서 호출되어 HeadquarterAdminMapper, PasswordEncoder을 사용해 검증·조회·저장 등의 처리를 수행하고 결과를 반환한다.
     */
    public HeadAdminResponseDTO getAdminDetail(Integer adminId) {
        getCurrentSuperAdmin();

        HeadquarterAdmin admin = headquarterAdminMapper.findById(adminId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 본사 관리자입니다."));

        return toResponseDTO(admin);
    }

    @Transactional
    /**
     * [메서드 흐름] createAdmin
     * Controller 또는 상위 서비스에서 호출되어 HeadquarterAdminMapper, PasswordEncoder을 사용해 검증·조회·저장 등의 처리를 수행하고 결과를 반환한다.
     */
    public String createAdmin(HeadAdminCreateRequestDTO requestDTO) {
        getCurrentSuperAdmin();

        validateCreateRequest(requestDTO);

        if (headquarterAdminMapper.existsByLoginId(requestDTO.getLoginId())) {
            throw new IllegalArgumentException("이미 사용 중인 본사 관리자 로그인 ID입니다.");
        }

        AdminRole role = requestDTO.getRole() != null
                ? requestDTO.getRole()
                : AdminRole.ADMIN;

        AdminStatus status = requestDTO.getStatus() != null
                ? requestDTO.getStatus()
                : AdminStatus.ACTIVE;

        HeadquarterAdmin admin = HeadquarterAdmin.builder()
                .loginId(requestDTO.getLoginId())
                .password(passwordEncoder.encode(requestDTO.getPassword()))
                .name(requestDTO.getName())
                .department(requestDTO.getDepartment())
                .role(role)
                .status(status)
                .build();

        headquarterAdminMapper.save(admin);

        adminLogService.logAction("관리자 계정", admin.getName() + " 본사 관리자 생성");
        return "본사 관리자 생성 성공";
    }

    @Transactional
    /**
     * [메서드 흐름] updateAdminRole
     * Controller 또는 상위 서비스에서 호출되어 HeadquarterAdminMapper, PasswordEncoder을 사용해 검증·조회·저장 등의 처리를 수행하고 결과를 반환한다.
     */
    public String updateAdminRole(Integer adminId, HeadAdminRoleUpdateRequestDTO requestDTO) {
        getCurrentSuperAdmin();

        if (requestDTO.getRole() == null) {
            throw new IllegalArgumentException("변경할 권한이 필요합니다.");
        }

        HeadquarterAdmin targetAdmin = headquarterAdminMapper.findById(adminId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 본사 관리자입니다."));

        preventLastSuperAdminRoleChange(targetAdmin, requestDTO.getRole());

        targetAdmin.changeRole(requestDTO.getRole());

        adminLogService.logAction("관리자 계정",
                targetAdmin.getName() + " 권한 변경 (" + requestDTO.getRole() + ")");
        return "본사 관리자 권한 변경 성공";
    }

    @Transactional
    /**
     * [메서드 흐름] updateAdminStatus
     * Controller 또는 상위 서비스에서 호출되어 HeadquarterAdminMapper, PasswordEncoder을 사용해 검증·조회·저장 등의 처리를 수행하고 결과를 반환한다.
     */
    public String updateAdminStatus(Integer adminId, HeadAdminStatusUpdateRequestDTO requestDTO) {
        HeadquarterAdmin requester = getCurrentSuperAdmin();

        if (requestDTO.getStatus() == null) {
            throw new IllegalArgumentException("변경할 상태가 필요합니다.");
        }

        HeadquarterAdmin targetAdmin = headquarterAdminMapper.findById(adminId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 본사 관리자입니다."));

        if (requester.getId().equals(targetAdmin.getId())
                && requestDTO.getStatus() == AdminStatus.INACTIVE) {
            throw new IllegalStateException("자기 자신의 계정은 비활성화할 수 없습니다.");
        }

        preventLastSuperAdminDeactivate(targetAdmin, requestDTO.getStatus());

        targetAdmin.changeStatus(requestDTO.getStatus());

        adminLogService.logAction("관리자 계정",
                targetAdmin.getName() + " 상태 변경 (" + requestDTO.getStatus() + ")");
        return "본사 관리자 상태 변경 성공";
    }

    @Transactional
    /**
     * [메서드 흐름] updateAdminPassword
     * Controller 또는 상위 서비스에서 호출되어 HeadquarterAdminMapper, PasswordEncoder을 사용해 검증·조회·저장 등의 처리를 수행하고 결과를 반환한다.
     */
    public String updateAdminPassword(Integer adminId, HeadAdminPasswordUpdateRequestDTO requestDTO) {
        HeadquarterAdmin requester = getCurrentActiveAdmin();

        HeadquarterAdmin targetAdmin = headquarterAdminMapper.findById(adminId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 본사 관리자입니다."));

        boolean isSelf = requester.getId().equals(targetAdmin.getId());
        boolean isSuperAdmin = requester.isSuperAdmin();

        if (!isSelf && !isSuperAdmin) {
            throw new IllegalStateException("본인 또는 최고 관리자만 비밀번호를 변경할 수 있습니다.");
        }

        if (requestDTO.getNewPassword() == null || requestDTO.getNewPassword().isBlank()) {
            throw new IllegalArgumentException("새 비밀번호를 입력해주세요.");
        }

        if (requestDTO.getNewPassword().length() < 4) {
            throw new IllegalArgumentException("비밀번호는 최소 4자 이상이어야 합니다.");
        }

        targetAdmin.changePassword(passwordEncoder.encode(requestDTO.getNewPassword()));

        adminLogService.logAction("관리자 계정", targetAdmin.getName() + " 비밀번호 변경");
        return "본사 관리자 비밀번호 변경 성공";
    }

    private HeadquarterAdmin getCurrentActiveAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication.getName() == null
                || "anonymousUser".equals(authentication.getName())) {
            throw new IllegalStateException("로그인이 필요합니다.");
        }

        String principal = authentication.getName();

        HeadquarterAdmin admin;

        try {
            Integer adminId = Integer.parseInt(principal);

            admin = headquarterAdminMapper.findById(adminId)
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 본사 관리자입니다."));
        } catch (NumberFormatException e) {
            admin = headquarterAdminMapper.findByLoginId(principal)
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 본사 관리자입니다."));
        }

        if (!admin.isActiveAdmin()) {
            throw new IllegalStateException("비활성화된 본사 관리자는 요청을 처리할 수 없습니다.");
        }

        return admin;
    }

    private HeadquarterAdmin getCurrentSuperAdmin() {
        HeadquarterAdmin admin = getCurrentActiveAdmin();

        if (!admin.isSuperAdmin()) {
            throw new IllegalStateException("최고 관리자만 처리할 수 있는 기능입니다.");
        }

        return admin;
    }

    private void validateCreateRequest(HeadAdminCreateRequestDTO requestDTO) {
        if (requestDTO.getLoginId() == null || requestDTO.getLoginId().isBlank()) {
            throw new IllegalArgumentException("로그인 ID를 입력해주세요.");
        }

        if (requestDTO.getPassword() == null || requestDTO.getPassword().isBlank()) {
            throw new IllegalArgumentException("비밀번호를 입력해주세요.");
        }

        if (requestDTO.getPassword().length() < 4) {
            throw new IllegalArgumentException("비밀번호는 최소 4자 이상이어야 합니다.");
        }

        if (requestDTO.getName() == null || requestDTO.getName().isBlank()) {
            throw new IllegalArgumentException("관리자 이름을 입력해주세요.");
        }
    }

    private void preventLastSuperAdminRoleChange(HeadquarterAdmin targetAdmin, AdminRole nextRole) {
        if (targetAdmin.getRole() == AdminRole.SUPER_ADMIN
                && nextRole != AdminRole.SUPER_ADMIN
                && targetAdmin.getStatus() == AdminStatus.ACTIVE) {

            long activeSuperAdminCount = headquarterAdminMapper.countByRoleAndStatus(
                    AdminRole.SUPER_ADMIN,
                    AdminStatus.ACTIVE
            );

            if (activeSuperAdminCount <= 1) {
                throw new IllegalStateException("마지막 활성 최고 관리자의 권한은 변경할 수 없습니다.");
            }
        }
    }

    private void preventLastSuperAdminDeactivate(HeadquarterAdmin targetAdmin, AdminStatus nextStatus) {
        if (targetAdmin.getRole() == AdminRole.SUPER_ADMIN
                && targetAdmin.getStatus() == AdminStatus.ACTIVE
                && nextStatus == AdminStatus.INACTIVE) {

            long activeSuperAdminCount = headquarterAdminMapper.countByRoleAndStatus(
                    AdminRole.SUPER_ADMIN,
                    AdminStatus.ACTIVE
            );

            if (activeSuperAdminCount <= 1) {
                throw new IllegalStateException("마지막 활성 최고 관리자는 비활성화할 수 없습니다.");
            }
        }
    }

    private HeadAdminResponseDTO toResponseDTO(HeadquarterAdmin admin) {
        return HeadAdminResponseDTO.builder()
                .adminId(admin.getId())
                .loginId(admin.getLoginId())
                .name(admin.getName())
                .department(admin.getDepartment())
                .role(admin.getRole())
                .status(admin.getStatus())
                .createdAt(admin.getCreatedAt())
                .build();
    }
}
