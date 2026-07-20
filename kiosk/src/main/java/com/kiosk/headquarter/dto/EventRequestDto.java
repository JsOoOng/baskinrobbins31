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
public class EventRequestDto {
    private String eventName;
    private String description;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private EventStatus eventStatus;
    private String targetType;
    private Integer discountValue;
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
                .isVisible(this.isVisible != null ? this.isVisible : false)
                .build();
    }
}
