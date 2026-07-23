package com.kiosk.headquarter.dto;

import java.time.LocalDateTime;

import com.kiosk.entity.Event;
import com.kiosk.entity.enums.EventStatus;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * [코드 흐름 안내] EventResponseDto
 *
 * <p>역할: 이벤트 계층 사이에서 필요한 값만 전달하는 DTO다.</p>
 * <p>호출 흐름: Controller·Service·프론트 사이의 데이터 모양을 현재 필드 정의로 고정한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
@Getter
@Setter
@NoArgsConstructor
public class EventResponseDto {
    private Integer eventId;
    private String eventName;
    private String description;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private EventStatus eventStatus;
    private String targetType;
    private Integer discountValue;
    private String discountType;
    private Boolean isVisible;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public EventResponseDto(Event event) {
        this.eventId = event.getEventId();
        this.eventName = event.getEventName();
        this.description = event.getDescription();
        this.startDate = event.getStartDate();
        this.endDate = event.getEndDate();
        this.eventStatus = event.getEventStatus();
        this.targetType = event.getTargetType();
        this.discountValue = event.getDiscountValue();
        this.discountType = event.getDiscountType();
        this.isVisible = event.getIsVisible();
        this.createdAt = event.getCreatedAt();
        this.updatedAt = event.getUpdatedAt();
    }
}
