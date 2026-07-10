package com.kiosk.headquarter.dto.statistics;

import java.time.LocalDate;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class HeadDailySalesResponseDTO {

    private LocalDate salesDate;
    private Long orderCount;
    private Long totalPaidAmount;
}