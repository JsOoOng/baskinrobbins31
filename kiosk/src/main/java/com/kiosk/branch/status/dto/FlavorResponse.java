package com.kiosk.branch.status.dto;

import com.kiosk.entity.IcecreamFlavor;

import lombok.Builder;
import lombok.Getter;


@Getter
@Builder
public class FlavorResponse {


    private Integer flavorId;

    private String flavorName;



    public static FlavorResponse from(IcecreamFlavor flavor){

        return FlavorResponse.builder()
                .flavorId(flavor.getId())
                .flavorName(flavor.getFlavorName())
                .build();

    }

}