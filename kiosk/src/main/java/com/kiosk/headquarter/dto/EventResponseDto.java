package com.kiosk.headquarter.dto;

import java.time.LocalDateTime;

import com.kiosk.entity.Event;
import com.kiosk.entity.enums.EventStatus;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
        this.isVisible = event.getIsVisible();
        this.createdAt = event.getCreatedAt();
        this.updatedAt = event.getUpdatedAt();
    }
}
