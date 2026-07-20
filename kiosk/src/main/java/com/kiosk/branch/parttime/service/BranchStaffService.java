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

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BranchStaffService {

    private final StaffRepository staffRepository;
    private final StoreRepository storeRepository;

    
    @Transactional
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