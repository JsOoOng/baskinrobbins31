package com.kiosk.headquarter.inventory;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kiosk.entity.StoreFlavor;
import com.kiosk.entity.enums.AutoRestockMode;
import com.kiosk.headquarter.repository.StoreFlavorRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


/**
 * [코드 흐름 안내] FlavorRestockService
 *
 * <p>역할: 본사 관리의 재입고·발주 업무 규칙과 상태 변경을 처리한다.</p>
 * <p>호출 흐름: Controller 호출 -> 이 서비스 -> StoreFlavorRepository -> Entity/DTO 변환 -> Controller 반환 순서로 동작한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class FlavorRestockService {


    private final StoreFlavorRepository repository;



    /**
     * [메서드 흐름] processFlavorRestock
     * Controller 또는 상위 서비스에서 호출되어 StoreFlavorRepository을 사용해 검증·조회·저장 등의 처리를 수행하고 결과를 반환한다.
     */
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