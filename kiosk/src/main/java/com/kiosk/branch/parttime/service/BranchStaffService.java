package com.kiosk.branch.parttime.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kiosk.branch.kiosk.repository.StoreRepository;
import com.kiosk.branch.parttime.dto.StaffCreateRequestDTO;
import com.kiosk.branch.parttime.dto.StaffDetailResponseDTO;
import com.kiosk.branch.parttime.dto.StaffListResponseDTO;
import com.kiosk.branch.parttime.dto.StaffUpdateRequestDTO;
import com.kiosk.branch.parttime.repository.StaffRepository;
import com.kiosk.entity.Staff;
import com.kiosk.entity.Store;
import com.kiosk.entity.enums.StaffStatus;

import lombok.RequiredArgsConstructor;

/**
 * [코드 흐름 안내] BranchStaffService
 *
 * <p>역할: 지점 운영의 직원 업무 규칙과 상태 변경을 처리한다.</p>
 * <p>호출 흐름: Controller 호출 -> 이 서비스 -> StaffRepository, StoreRepository -> Entity/DTO 변환 -> Controller 반환 순서로 동작한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BranchStaffService {

    private final StaffRepository staffRepository;
    private final StoreRepository storeRepository;

    
    @Transactional
    /**
     * [메서드 흐름] createStaff
     * Controller 또는 상위 서비스에서 호출되어 StaffRepository, StoreRepository을 사용해 검증·조회·저장 등의 처리를 수행하고 결과를 반환한다.
     */
    public void createStaff(
            Integer storeId,
            StaffCreateRequestDTO requestDTO
    ) {

        Store store = storeRepository.findById(storeId)
                .orElseThrow(() ->
                        new IllegalArgumentException("지점을 찾을 수 없습니다."));

        Staff staff = Staff.builder()
                .store(store)
                .name(requestDTO.getName())
                .hp(requestDTO.getHp())
                .email(requestDTO.getEmail())
                .address(requestDTO.getAddress())
                .birthDate(requestDTO.getBirthDate())
                .healthCertEndDate(requestDTO.getHealthCertEndDate())
                .hourlyWage(requestDTO.getHourlyWage())
                .build();

        staffRepository.save(staff);

    }
    
    /**
     * [메서드 흐름] getStaffList
     * Controller 또는 상위 서비스에서 호출되어 StaffRepository, StoreRepository을 사용해 검증·조회·저장 등의 처리를 수행하고 결과를 반환한다.
     */
    public List<StaffListResponseDTO> getStaffList(
            Integer storeId
    ) {

        Store store = storeRepository.findById(storeId)
                .orElseThrow(() ->
                        new IllegalArgumentException("지점을 찾을 수 없습니다."));

        return staffRepository
                .findByStoreOrderByIdAsc(store)
                .stream()
                .filter(staff ->
                staff.getStatus() != StaffStatus.TERMINATED
                )
                .map(staff -> StaffListResponseDTO.builder()

                        .staffId(staff.getId())

                        .name(staff.getName())

                        .hp(staff.getHp())

                        .email(staff.getEmail())

                        .address(staff.getAddress())

                        .birthDate(
                                staff.getBirthDate() != null
                                ?
                                staff.getBirthDate().toString()
                                :
                                null
                        )

                        .healthCertEndDate(
                                staff.getHealthCertEndDate() != null
                                ?
                                staff.getHealthCertEndDate().toString()
                                :
                                null
                        )

                        .hourlyWage(
                                staff.getHourlyWage()
                        )

                        .status(
                                staff.getStatus()
                        )

                        .build()
                )
                .toList();

    }
    
    /**
     * [메서드 흐름] getStaff
     * Controller 또는 상위 서비스에서 호출되어 StaffRepository, StoreRepository을 사용해 검증·조회·저장 등의 처리를 수행하고 결과를 반환한다.
     */
    public StaffDetailResponseDTO getStaff(
            Integer staffId
    ) {

        Staff staff = staffRepository.findById(staffId)
                .orElseThrow(() ->
                        new IllegalArgumentException("직원을 찾을 수 없습니다."));

        return StaffDetailResponseDTO.builder()
                .staffId(staff.getId())
                .name(staff.getName())
                .hp(staff.getHp())
                .email(staff.getEmail())
                .address(staff.getAddress())
                .birthDate(staff.getBirthDate())
                .healthCertEndDate(staff.getHealthCertEndDate())
                .hourlyWage(staff.getHourlyWage())
                .status(staff.getStatus())
                .build();

    }
    
    @Transactional
    /**
     * [메서드 흐름] updateStaff
     * Controller 또는 상위 서비스에서 호출되어 StaffRepository, StoreRepository을 사용해 검증·조회·저장 등의 처리를 수행하고 결과를 반환한다.
     */
    public void updateStaff(
            Integer staffId,
            StaffUpdateRequestDTO requestDTO
    ) {

        Staff staff = staffRepository.findById(staffId)
                .orElseThrow(() ->
                        new IllegalArgumentException("직원을 찾을 수 없습니다."));

        staff.update(
                requestDTO.getName(),
                requestDTO.getHp(),
                requestDTO.getEmail(),
                requestDTO.getAddress(),
                requestDTO.getBirthDate(),
                requestDTO.getHealthCertEndDate(),
                requestDTO.getHourlyWage(),
                requestDTO.getStatus()
        );

    }
    
    @Transactional
    /**
     * [메서드 흐름] changeStatus
     * Controller 또는 상위 서비스에서 호출되어 StaffRepository, StoreRepository을 사용해 검증·조회·저장 등의 처리를 수행하고 결과를 반환한다.
     */
    public void changeStatus(
            Integer staffId,
            StaffStatus status
    ) {

        Staff staff = staffRepository.findById(staffId)
                .orElseThrow(() ->
                        new IllegalArgumentException("직원을 찾을 수 없습니다."));

        staff.changeStatus(status);

    }
    
}