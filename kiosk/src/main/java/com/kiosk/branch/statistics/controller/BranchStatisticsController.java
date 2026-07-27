package com.kiosk.branch.statistics.controller;

import java.time.LocalDate;
import java.time.LocalTime;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kiosk.branch.statistics.dto.BranchStatisticsResponse;
import com.kiosk.branch.statistics.service.BranchStatisticsService;

import lombok.RequiredArgsConstructor;



/**
 * [코드 흐름 안내] BranchStatisticsController
 *
 * <p>역할: 지점 운영의 통계 HTTP 요청을 받는 진입점이다.</p>
 * <p>호출 흐름: Vue/API 요청 -> 이 컨트롤러(/branch/statistics) -> BranchStatisticsService -> 응답 DTO 또는 JSON -> 화면 갱신 순서로 이동한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
@RestController
@RequestMapping("/branch/statistics")
@RequiredArgsConstructor
public class BranchStatisticsController {


    private final BranchStatisticsService statisticsService;




    /**
     * [요청 흐름] GET /branch/statistics/{storeId}
     * 프론트 요청을 받아 getStatistics() 메서드가 입력을 받고 BranchStatisticsService 호출 후 결과를 응답한다.
     */
    @GetMapping("/{storeId}")
    public BranchStatisticsResponse getStatistics(


            @PathVariable
            Integer storeId,


            @RequestParam(required = false)
            LocalDate startDate,


            @RequestParam(required = false)
            LocalDate endDate,


            @RequestParam(required = false)
            LocalTime startTime,


            @RequestParam(required = false)
            LocalTime endTime


    ){



        /*
         * 날짜 기본값
         */
        if(startDate == null){

            startDate =
                    LocalDate.now()
                    .minusMonths(1);

        }



        if(endDate == null){

            endDate =
                    LocalDate.now();

        }




        /*
         * 시간 기본값
         */
        if(startTime == null){

            startTime =
                    LocalTime.of(0,0,0);

        }



        if(endTime == null){

            endTime =
                    LocalTime.of(23,59,59);

        }





        return statisticsService.getStatistics(

                storeId,

                startDate,

                endDate,

                startTime,

                endTime

        );

    }


}