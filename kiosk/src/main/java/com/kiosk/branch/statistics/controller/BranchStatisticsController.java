package com.kiosk.branch.statistics.controller;


import java.time.LocalDate;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kiosk.branch.statistics.dto.BranchStatisticsResponse;
import com.kiosk.branch.statistics.service.BranchStatisticsService;

import lombok.RequiredArgsConstructor;



@RestController
@RequestMapping("/branch/statistics")
@RequiredArgsConstructor
public class BranchStatisticsController {



    private final BranchStatisticsService statisticsService;




    @GetMapping("/{storeId}")
    public BranchStatisticsResponse getStatistics(

            @PathVariable Integer storeId,


            @RequestParam(required = false) LocalDate startDate,


            @RequestParam(required = false) LocalDate endDate

    ){


        if(startDate == null){

            startDate =
                LocalDate.now()
                .minusMonths(1);

        }


        if(endDate == null){

            endDate =
                LocalDate.now();

        }



        return statisticsService.getStatistics(
                storeId,
                startDate,
                endDate
        );


    }



}