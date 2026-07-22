package com.kiosk.branch.status.dto;

import com.kiosk.entity.StoreFlavor;
import com.kiosk.entity.enums.AutoRestockMode;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StoreFlavorStatusResponse {


    private Integer storeFlavorId;

    private Integer flavorId;


    private String flavorName;


    private Boolean soldOut;


    private Integer container;


    // 자동 보충 설정

    private Boolean autoRestockEnabled;


    private AutoRestockMode restockMode;


    private Integer minStock;


    private Integer targetStock;



    public static StoreFlavorStatusResponse from(StoreFlavor sf) {


        return StoreFlavorStatusResponse.builder()

                .storeFlavorId(
                    sf.getId()
                )

                .flavorId(
                    sf.getFlavor().getId()
                )

                .flavorName(
                    sf.getFlavor().getFlavorName()
                )

                .soldOut(
                    sf.getIsSoldOut()
                )

                .container(
                    sf.getContainer()
                )

                .autoRestockEnabled(
                    sf.getAutoRestockEnabled()
                )

                .restockMode(
                    sf.getRestockMode()
                )

                .minStock(
                    sf.getMinStock()
                )

                .targetStock(
                    sf.getTargetStock()
                )

                .build();

    }

}