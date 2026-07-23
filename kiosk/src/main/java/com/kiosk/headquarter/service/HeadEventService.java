package com.kiosk.headquarter.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kiosk.entity.Event;
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
}
