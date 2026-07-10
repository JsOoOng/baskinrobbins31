package com.kiosk.headquarter.dto.inquiry;

import java.time.LocalDateTime;

import com.kiosk.entity.enums.InquiryStatus;
import com.kiosk.entity.enums.InquiryType;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class HeadInquiryDetailResponseDTO {

    private Integer inquiryId;
    private InquiryType inquiryType;

    private Integer storeId;
    private String storeName;

    private String title;
    private String content;

    private String answer;
    private InquiryStatus status;

    private Integer adminId;
    private String adminName;

    private LocalDateTime createdAt;
}