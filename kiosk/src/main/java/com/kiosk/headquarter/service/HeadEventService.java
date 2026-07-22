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

@Service
@RequiredArgsConstructor
public class HeadEventService {

    private final HeadEventRepository eventRepository;

    @Transactional(readOnly = true)
    public List<EventResponseDto> getAllEvents() {
        return eventRepository.findAll()
                .stream()
                .map(EventResponseDto::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public EventResponseDto getEvent(Integer eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("이벤트를 찾을 수 없습니다."));
        return new EventResponseDto(event);
    }

    @Transactional
    public EventResponseDto createEvent(EventRequestDto requestDto) {
        Event event = requestDto.toEntity();
        Event savedEvent = eventRepository.save(event);
        return new EventResponseDto(savedEvent);
    }

    @Transactional
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

        return new EventResponseDto(event);
    }

    @Transactional
    public void deleteEvent(Integer eventId) {
        if (!eventRepository.existsById(eventId)) {
            throw new IllegalArgumentException("이벤트를 찾을 수 없습니다.");
        }
        eventRepository.deleteById(eventId);
    }

    @Transactional
    public EventResponseDto updateVisibility(Integer eventId, Boolean isVisible) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("이벤트를 찾을 수 없습니다."));
        
        event.setIsVisible(isVisible);
        return new EventResponseDto(event);
    }
}
