package com.kiosk.branch.parttime.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kiosk.entity.Staff;
import com.kiosk.entity.Store;
import com.kiosk.entity.enums.StaffStatus;

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