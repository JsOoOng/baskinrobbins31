package com.kiosk.headquarter.dto;

import java.time.LocalDateTime;

import com.kiosk.entity.Event;
import com.kiosk.entity.enums.EventStatus;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * [코드 흐름 안내] EventRequestDto
 *
 * <p>역할: 이벤트 계층 사이에서 필요한 값만 전달하는 DTO다.</p>
 * <p>호출 흐름: Controller·Service·프론트 사이의 데이터 모양을 현재 필드 정의로 고정한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
@Getter
@Setter
@NoArgsConstructor
public class EventRequestDto {
    private String eventName;
    private String description;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private EventStatus eventStatus;
    private String targetType;
    private Integer discountValue;
    private String discountType;
    private Boolean isVisible;

    public Event toEntity() {
        return Event.builder()
                .eventName(this.eventName)
                .description(this.description)
                .startDate(this.startDate)
                .endDate(this.endDate)
                .eventStatus(this.eventStatus != null ? this.eventStatus : EventStatus.SCHEDULED)
                .targetType(this.targetType)
                .discountValue(this.discountValue != null ? this.discountValue : 0)
                .discountType(this.discountType != null ? this.discountType : "AMOUNT")
                .isVisible(this.isVisible != null ? this.isVisible : false)
                .build();
    }
}
