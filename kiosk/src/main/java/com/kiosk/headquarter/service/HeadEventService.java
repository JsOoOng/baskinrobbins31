package com.kiosk.headquarter.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

import com.kiosk.entity.Event;
import com.kiosk.entity.enums.EventStatus;
import com.kiosk.entity.enums.NotificationCategory;
import com.kiosk.entity.enums.NotificationType;
import com.kiosk.headquarter.dto.EventRequestDto;
import com.kiosk.headquarter.dto.EventResponseDto;
import com.kiosk.headquarter.repository.HeadEventRepository;

import lombok.RequiredArgsConstructor;

/**
 * [코드 흐름 안내] HeadEventService
 *
 * <p>역할: 본사 관리의 이벤트 업무 규칙과 상태 변경을 처리한다.</p>
 * <p>호출 흐름: Controller 호출 -> 이 서비스 -> HeadEventRepository, AdminLogService -> Entity/DTO 변환 -> Controller 반환 순서로 동작한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
@Service
@RequiredArgsConstructor
public class HeadEventService {

    private final HeadEventRepository eventRepository;
    private final AdminLogService adminLogService;
    private final HeadNotificationService headNotificationService;

    @Transactional(readOnly = true)
    /**
     * [메서드 흐름] getAllEvents
     * Controller 또는 상위 서비스에서 호출되어 HeadEventRepository, AdminLogService을 사용해 검증·조회·저장 등의 처리를 수행하고 결과를 반환한다.
     */
    public List<EventResponseDto> getAllEvents() {
        return eventRepository.findAll()
                .stream()
                .map(EventResponseDto::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    /**
     * [메서드 흐름] getEvent
     * Controller 또는 상위 서비스에서 호출되어 HeadEventRepository, AdminLogService을 사용해 검증·조회·저장 등의 처리를 수행하고 결과를 반환한다.
     */
    public EventResponseDto getEvent(Integer eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("이벤트를 찾을 수 없습니다."));
        return new EventResponseDto(event);
    }

    @Transactional
    /**
     * [메서드 흐름] createEvent
     * Controller 또는 상위 서비스에서 호출되어 HeadEventRepository, AdminLogService을 사용해 검증·조회·저장 등의 처리를 수행하고 결과를 반환한다.
     */
    public EventResponseDto createEvent(EventRequestDto requestDto) {
        Event event = requestDto.toEntity();
        Event savedEvent = eventRepository.save(event);
        
        adminLogService.logAction("이벤트", savedEvent.getEventName() + " 신규 등록");
        
        return new EventResponseDto(savedEvent);
    }

    @Transactional
    /**
     * [메서드 흐름] updateEvent
     * Controller 또는 상위 서비스에서 호출되어 HeadEventRepository, AdminLogService을 사용해 검증·조회·저장 등의 처리를 수행하고 결과를 반환한다.
     */
    public EventResponseDto updateEvent(Integer eventId, EventRequestDto requestDto) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("이벤트를 찾을 수 없습니다."));

        event.setEventName(requestDto.getEventName());
        event.setDescription(requestDto.getDescription());
        event.setStartDate(requestDto.getStartDate());
        event.setEndDate(requestDto.getEndDate());
        
        if (requestDto.getEventStatus() != null) {
            event.setEventStatus(requestDto.getEventStatus());
        }
        
        event.setTargetType(requestDto.getTargetType());
        event.setDiscountValue(requestDto.getDiscountValue());
        
        if (requestDto.getDiscountType() != null) {
            event.setDiscountType(requestDto.getDiscountType());
        }
        
        if (requestDto.getIsVisible() != null) {
            event.setIsVisible(requestDto.getIsVisible());
        }

        adminLogService.logAction("이벤트", event.getEventName() + " 정보 수정");

        return new EventResponseDto(event);
    }

    @Transactional
    /**
     * [메서드 흐름] deleteEvent
     * Controller 또는 상위 서비스에서 호출되어 HeadEventRepository, AdminLogService을 사용해 검증·조회·저장 등의 처리를 수행하고 결과를 반환한다.
     */
    public void deleteEvent(Integer eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("이벤트를 찾을 수 없습니다."));
                
        eventRepository.deleteById(eventId);
        
        adminLogService.logAction("이벤트", event.getEventName() + " 삭제");
    }

    @Transactional
    /**
     * [메서드 흐름] updateVisibility
     * Controller 또는 상위 서비스에서 호출되어 HeadEventRepository, AdminLogService을 사용해 검증·조회·저장 등의 처리를 수행하고 결과를 반환한다.
     */
    public EventResponseDto updateVisibility(Integer eventId, Boolean isVisible) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("이벤트를 찾을 수 없습니다."));
        
        event.setIsVisible(isVisible);
        
        adminLogService.logAction("이벤트", event.getEventName() + " 상태 변경 (" + (isVisible ? "노출" : "숨김") + ")");
        
        return new EventResponseDto(event);
    }

    /**
     * 쉬운주석: 기존 종료일보다 뒤의 날짜만 허용하고, 이미 끝난 이벤트라면 다시 진행 상태로 돌린다.
     */
    @Transactional
    public EventResponseDto extendEvent(Integer eventId, LocalDateTime newEndDate) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("이벤트를 찾을 수 없습니다."));

        if (newEndDate == null
                || event.getEndDate() == null
                || !newEndDate.isAfter(event.getEndDate())
                || !newEndDate.isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException(
                    "새 종료일은 기존 종료일과 현재 시각보다 뒤여야 합니다."
            );
        }

        event.setEndDate(newEndDate);
        if (event.getStartDate() != null && !event.getStartDate().isAfter(LocalDateTime.now())) {
            event.setEventStatus(EventStatus.ACTIVE);
            event.setIsVisible(true);
        }
        adminLogService.logAction(
                "이벤트",
                event.getEventName() + " 종료일 연장 (" + newEndDate + ")"
        );
        /*
         * 쉬운주석: 연장 직후 본사 알림 목록에서도 새 종료 시각을 바로 확인할 수 있게 알림을 만든다.
         * 종료 시각 전체를 구분값으로 사용해 같은 날 여러 번 연장해도 각각 기록된다.
         */
        headNotificationService.createNotificationOnce(
                NotificationCategory.EVENT,
                NotificationType.EVENT_UPDATED,
                "이벤트 기간 연장",
                event.getEventName() + " 이벤트가 " + newEndDate + "까지 연장되었습니다.",
                "head-events",
                event.getEventId() + ":" + newEndDate
        );
        return new EventResponseDto(event);
    }

    /**
     * 쉬운주석: 매분 이벤트 종료일을 확인해 짧은 시간으로 시험해도 1분 안에 상태가 바뀐다.
     * 7일·1일 전에는 한 번만 알리고, 종료 시 상태를 ENDED로 바꾸고 키오스크에서 숨긴다.
     */
    @Scheduled(cron = "0 * * * * *")
    @Transactional
    public void synchronizeEventPeriods() {
        LocalDateTime now = LocalDateTime.now();
        LocalDate today = now.toLocalDate();

        for (Event event : eventRepository.findAll()) {
            if (event.getEndDate() == null) {
                continue;
            }

            long remainingDays = ChronoUnit.DAYS.between(
                    today,
                    event.getEndDate().toLocalDate()
            );
            if (remainingDays == 7 || remainingDays == 1) {
                String remainingText = remainingDays == 1 ? "하루" : "일주일";
                String title = "이벤트 종료 " + remainingText + " 전";
                headNotificationService.createNotificationOnce(
                        NotificationCategory.EVENT,
                        NotificationType.EVENT_EXPIRING,
                        title,
                        event.getEventName() + " 이벤트가 " + remainingText + " 남았습니다.",
                        "head-events",
                        event.getEventId() + ":" + event.getEndDate()
                );
            }

            if (!event.getEndDate().isAfter(now)
                    && event.getEventStatus() != EventStatus.ENDED) {
                event.setEventStatus(EventStatus.ENDED);
                event.setIsVisible(false);
                headNotificationService.createNotificationOnce(
                        NotificationCategory.EVENT,
                        NotificationType.EVENT_ENDED,
                        "이벤트 종료",
                        event.getEventName() + " 이벤트가 종료되어 자동으로 숨김 처리되었습니다.",
                        "head-events",
                        event.getEventId() + ":" + event.getEndDate()
                );
                adminLogService.logAction(
                        "이벤트",
                        event.getEventName() + " 자동 종료 및 숨김"
                );
            }
        }
    }
}
