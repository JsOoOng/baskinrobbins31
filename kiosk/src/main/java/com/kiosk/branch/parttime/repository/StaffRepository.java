package com.kiosk.branch.parttime.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kiosk.entity.Staff;
import com.kiosk.entity.Store;
import com.kiosk.entity.enums.StaffStatus;

/**
 * [코드 흐름 안내] StaffRepository
 *
 * <p>역할: 지점 운영의 직원 데이터를 조회하거나 저장하는 DB 접근 계층이다.</p>
 * <p>호출 흐름: Service -> 이 Repository(STAFFS) -> MySQL -> 조회/변경 결과 반환 순서로 동작한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
public interface StaffRepository extends JpaRepository<Staff, Integer> {

    List<Staff> findByStoreOrderByIdAsc(Store store);

    List<Staff> findByStoreAndStatusOrderByIdAsc(
            Store store,
            StaffStatus status
    );
    
    List<Staff> findByStoreIdAndStatusNot(
            Integer storeId,
            StaffStatus status
    );
    
    

}