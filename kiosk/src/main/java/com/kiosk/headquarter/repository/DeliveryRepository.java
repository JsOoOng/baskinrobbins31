package com.kiosk.headquarter.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.kiosk.entity.Delivery;

public interface DeliveryRepository
        extends JpaRepository<Delivery, Integer> {


    boolean existsByRestockRequestId(
            Integer requestId
    );



    @Query("""
            select d
            from Delivery d

            join fetch d.restockRequest r

            left join fetch r.storeInventory si
            left join fetch si.item i
            left join fetch si.store s

            left join fetch r.storeFlavor sf
            left join fetch sf.flavor f

            order by d.id desc
    """)
    List<Delivery> findAllDelivery();

}