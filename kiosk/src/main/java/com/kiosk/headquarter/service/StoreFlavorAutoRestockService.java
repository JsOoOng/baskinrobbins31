package com.kiosk.headquarter.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kiosk.entity.StoreFlavor;
import com.kiosk.entity.enums.AutoRestockMode;
import com.kiosk.headquarter.repository.StoreFlavorRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StoreFlavorAutoRestockService {


    private final StoreFlavorRepository storeFlavorRepository;



    /*
     * 정기 자동 보충
     */
    @Transactional
    public void processDailyRestock() {


        List<StoreFlavor> flavors =
                storeFlavorRepository
                .findByAutoRestockEnabledTrue();



        int processedCount = 0;
        int totalRestockQuantity = 0;



        for(StoreFlavor flavor : flavors){


            if(!needsDailyRestock(flavor)){
                continue;
            }


            Integer quantity =
                    flavor.autoRestock();


            if(quantity <= 0){
                continue;
            }


            processedCount++;

            totalRestockQuantity += quantity;


            log.info(
                "맛 정기 자동 보충 : "
                + "storeFlavorId={}, "
                + "flavor={}, "
                + "quantity={}, "
                + "currentContainer={}",

                flavor.getId(),
                flavor.getFlavor().getFlavorName(),
                quantity,
                flavor.getContainer()
            );

        }


        log.info(
            "맛 자동 보충 완료 : "
            + "processedCount={}, "
            + "totalQuantity={}",

            processedCount,
            totalRestockQuantity
        );

    }




    private boolean needsDailyRestock(
            StoreFlavor flavor
    ){

        if(
            !Boolean.TRUE.equals(
                    flavor.getAutoRestockEnabled()
            )
        ){
            return false;
        }


        if(
            flavor.getRestockMode()
            != AutoRestockMode.DAILY
            &&
            flavor.getRestockMode()
            != AutoRestockMode.BOTH
        ){
            return false;
        }


        return flavor.getContainer()
                <
                flavor.getTargetStock();

    }

}