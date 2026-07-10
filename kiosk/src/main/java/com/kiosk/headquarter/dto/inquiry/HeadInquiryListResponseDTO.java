package com.kiosk.headquarter.dto.inquiry;

import java.time.LocalDateTime;

import com.kiosk.entity.enums.InquiryStatus;
import com.kiosk.entity.enums.InquiryType;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class HeadInquiryListResponseDTO {

    private Integer inquiryId;
    private InquiryType inquiryType;
    private Integer storeId;
    private String storeName;
    private String title;
    private InquiryStatus status;
    private LocalDateTime createdAt;
}