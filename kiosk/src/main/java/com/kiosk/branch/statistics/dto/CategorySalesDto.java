package com.kiosk.branch.statistics.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;


@Getter
@AllArgsConstructor
public class CategorySalesDto {


    private String categoryName;

    private Integer sales;

}