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

@Service
public class HeadEmployeeService {

    private final HeadEmployeeMapper headEmployeeMapper;
    private final HeadStoreMapper headStoreMapper;
    private final PasswordEncoder passwordEncoder;

    public HeadEmployeeService(
            HeadEmployeeMapper headEmployeeMapper,
            HeadStoreMapper headStoreMapper,
            PasswordEncoder passwordEncoder
    ) {
        this.headEmployeeMapper = headEmployeeMapper;
        this.headStoreMapper = headStoreMapper;
        this.passwordEncoder = passwordEncoder;
    }

    /*
     * 지점 관리자 계정 생성
     */
    @Transactional
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