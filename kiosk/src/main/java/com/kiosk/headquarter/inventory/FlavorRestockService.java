package com.kiosk.headquarter.inventory;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kiosk.entity.StoreFlavor;
import com.kiosk.entity.enums.AutoRestockMode;
import com.kiosk.headquarter.repository.StoreFlavorRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class FlavorRestockService {


    private final StoreFlavorRepository repository;



    public void processFlavorRestock(){


        List<StoreFlavor> flavors =
                repository.findByAutoRestockEnabledTrue();


        int count = 0;
        int total = 0;


        for(StoreFlavor flavor : flavors){


            // 자동보충 OFF
            if(!Boolean.TRUE.equals(
                    flavor.getAutoRestockEnabled()
            )){
                continue;
            }


            AutoRestockMode mode =
                    flavor.getRestockMode();



            boolean need = false;



            if(mode == AutoRestockMode.THRESHOLD
                    || mode == AutoRestockMode.BOTH){

                need = flavor.getContainer()
                        <= flavor.getMinStock();

            }



            if(mode == AutoRestockMode.DAILY
                    || mode == AutoRestockMode.BOTH){

                need = true;

            }



            if(!need){
                continue;
            }



            int quantity =
                    flavor.getTargetStock()
                    - flavor.getContainer();



            if(quantity <= 0){
                continue;
            }



            flavor.changeContainer(quantity);



            count++;
            total += quantity;



            log.info(
                "맛 자동 보충 storeFlavorId={}, flavor={}, +{}",
                flavor.getId(),
                flavor.getFlavor().getFlavorName(),
                quantity
            );

        }



        log.info(
            "맛 자동 보충 완료 count={}, total={}",
            count,
            total
        );

    }

}