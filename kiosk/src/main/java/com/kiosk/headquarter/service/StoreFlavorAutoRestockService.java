package com.kiosk.headquarter.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kiosk.entity.StoreFlavor;
import com.kiosk.entity.enums.AutoRestockMode;
import com.kiosk.headquarter.repository.StoreFlavorRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


/**
 * [코드 흐름 안내] StoreFlavorAutoRestockService
 *
 * <p>역할: 본사 관리의 재입고·발주 업무 규칙과 상태 변경을 처리한다.</p>
 * <p>호출 흐름: Controller 호출 -> 이 서비스 -> StoreFlavorRepository -> Entity/DTO 변환 -> Controller 반환 순서로 동작한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
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
    /**
     * [메서드 흐름] processDailyRestock
     * Controller 또는 상위 서비스에서 호출되어 StoreFlavorRepository을 사용해 검증·조회·저장 등의 처리를 수행하고 결과를 반환한다.
     */
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