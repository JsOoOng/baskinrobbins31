package com.kiosk.branch.statistics.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProductStatisticsResponse {


    private String productName;


    private Integer quantity;


    private Integer sales;

}