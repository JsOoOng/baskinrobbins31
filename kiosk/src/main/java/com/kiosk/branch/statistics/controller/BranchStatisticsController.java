package com.kiosk.branch.statistics.controller;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kiosk.branch.statistics.dto.BranchStatisticsResponse;
import com.kiosk.branch.statistics.service.BranchStatisticsService;

import lombok.RequiredArgsConstructor;



@RestController
@RequestMapping("/branch/statistics")
@RequiredArgsConstructor
public class BranchStatisticsController {



    private final BranchStatisticsService statisticsService;




    /*
     * 지점 통계 조회
     *
     * GET
     * /branch/statistics/{storeId}
     *
     */
    @GetMapping("/{storeId}")
    public BranchStatisticsResponse getStatistics(
            @PathVariable Integer storeId
    ){


        return statisticsService.getStatistics(storeId);

    }


}