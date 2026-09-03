package com.kiosk.headquarter.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kiosk.entity.Employee;
import com.kiosk.entity.Store;
import com.kiosk.entity.enums.EmployeeStatus;
import com.kiosk.entity.enums.Role;
import com.kiosk.headquarter.dto.employee.HeadEmployeeCreateRequest;
import com.kiosk.headquarter.dto.employee.HeadEmployeeCreateResponse;
import com.kiosk.headquarter.repository.HeadEmployeeMapper;
import com.kiosk.headquarter.repository.HeadStoreMapper;

/**
 * [코드 흐름 안내] HeadEmployeeService
 *
 * <p>역할: 본사 관리의 직원 계정 업무 규칙과 상태 변경을 처리한다.</p>
 * <p>호출 흐름: Controller 호출 -> 이 서비스 -> HeadEmployeeMapper, HeadStoreMapper, PasswordEncoder -> Entity/DTO 변환 -> Controller 반환 순서로 동작한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
@Service
public class HeadEmployeeService {

    private final HeadEmployeeMapper headEmployeeMapper;
    private final HeadStoreMapper headStoreMapper;
    private final PasswordEncoder passwordEncoder;
    private final AdminLogService adminLogService;

    public HeadEmployeeService(
            HeadEmployeeMapper headEmployeeMapper,
            HeadStoreMapper headStoreMapper,
            PasswordEncoder passwordEncoder,
            AdminLogService adminLogService
    ) {
        this.headEmployeeMapper = headEmployeeMapper;
        this.headStoreMapper = headStoreMapper;
        this.passwordEncoder = passwordEncoder;
        this.adminLogService = adminLogService;
    }

    /*
     * 지점 관리자 계정 생성
     */
    @Transactional
    /**
     * [메서드 흐름] createStoreManager
     * Controller 또는 상위 서비스에서 호출되어 HeadEmployeeMapper, HeadStoreMapper, PasswordEncoder을 사용해 검증·조회·저장 등의 처리를 수행하고 결과를 반환한다.
     */
    public HeadEmployeeCreateResponse createStoreManager(
            HeadEmployeeCreateRequest request
    ) {

        // 1. 요청 객체 검사
        if (request == null) {
            throw new IllegalArgumentException(
                    "지점 관리자 정보를 입력해주세요."
            );
        }

        // 2. 지점 ID 검사
        if (request.getStoreId() == null) {
            throw new IllegalArgumentException(
                    "지점 ID를 입력해야 합니다."
            );
        }

        // 3. 로그인 ID 검사
        if (request.getLoginId() == null
                || request.getLoginId().isBlank()) {

            throw new IllegalArgumentException(
                    "로그인 ID를 입력해야 합니다."
            );
        }

        // 4. 비밀번호 검사
        if (request.getPassword() == null
                || request.getPassword().isBlank()) {

            throw new IllegalArgumentException(
                    "비밀번호를 입력해야 합니다."
            );
        }

        // 5. 담당자 이름 검사
        if (request.getName() == null
                || request.getName().isBlank()) {

            throw new IllegalArgumentException(
                    "담당자 이름을 입력해야 합니다."
            );
        }

        Integer storeId = request.getStoreId();
        String loginId = request.getLoginId().trim();
        String name = request.getName().trim();

        // 6. 실제 지점 조회
        Store store = headStoreMapper.findById(storeId)
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "존재하지 않는 지점입니다."
                        )
                );

        // 7. 로그인 ID 중복 검사
        if (headEmployeeMapper.existsByLoginId(loginId)) {
            throw new IllegalArgumentException(
                    "이미 사용 중인 로그인 ID입니다."
            );
        }

        // 8. 지점 관리자 엔티티 생성
        Employee employee = Employee.builder()
                .store(store)
                .loginId(loginId)

                // 비밀번호를 BCrypt로 암호화
                .password(
                        passwordEncoder.encode(
                                request.getPassword()
                        )
                )

                .name(name)
                .role(Role.MANAGER)
                .status(EmployeeStatus.EMPLOYED)
                .build();

        // 9. DB 저장
        Employee savedEmployee =
                headEmployeeMapper.save(employee);

        adminLogService.logAction("지점 계정",
                store.getStoreName() + " - " + savedEmployee.getName() + " 관리자 계정 생성");
        // 10. 응답 DTO 생성
        return HeadEmployeeCreateResponse.builder()
                .employeeId(savedEmployee.getId())
                .storeId(savedEmployee.getStore().getId())
                .loginId(savedEmployee.getLoginId())
                .name(savedEmployee.getName())
                .role(savedEmployee.getRole().name())
                .status(savedEmployee.getStatus().name())
                .build();
    }
}
