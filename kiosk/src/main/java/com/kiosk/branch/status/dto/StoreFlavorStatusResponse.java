package com.kiosk.branch.status.dto;

import com.kiosk.entity.StoreFlavor;

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


    public static StoreFlavorStatusResponse from(StoreFlavor sf) {

        return StoreFlavorStatusResponse.builder()
                .storeFlavorId(sf.getId())
                .flavorId(sf.getFlavor().getId())
                .flavorName(sf.getFlavor().getFlavorName())
                .soldOut(sf.getIsSoldOut())
                .container(sf.getContainer())
                .build();
    }

}