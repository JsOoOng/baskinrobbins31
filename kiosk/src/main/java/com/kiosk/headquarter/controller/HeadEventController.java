package com.kiosk.headquarter.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kiosk.headquarter.dto.EventRequestDto;
import com.kiosk.headquarter.dto.EventResponseDto;
import com.kiosk.headquarter.service.HeadEventService;

import lombok.RequiredArgsConstructor;

/**
 * [코드 흐름 안내] HeadEventController
 *
 * <p>역할: 본사 관리의 이벤트 HTTP 요청을 받는 진입점이다.</p>
 * <p>호출 흐름: Vue/API 요청 -> 이 컨트롤러(/head/events) -> HeadEventService -> 응답 DTO 또는 JSON -> 화면 갱신 순서로 이동한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
@RestController
@RequestMapping("/head/events")
@RequiredArgsConstructor
public class HeadEventController {

    private final HeadEventService eventService;

    /**
     * [요청 흐름] GET /head/events
     * 프론트 요청을 받아 getAllEvents() 메서드가 입력을 받고 HeadEventService 호출 후 결과를 응답한다.
     */
    @GetMapping
    public ResponseEntity<List<EventResponseDto>> getAllEvents() {
        return ResponseEntity.ok(eventService.getAllEvents());
    }

    /**
     * [요청 흐름] GET /head/events/{eventId}
     * 프론트 요청을 받아 getEvent() 메서드가 입력을 받고 HeadEventService 호출 후 결과를 응답한다.
     */
    @GetMapping("/{eventId}")
    public ResponseEntity<EventResponseDto> getEvent(@PathVariable("eventId") Integer eventId) {
        return ResponseEntity.ok(eventService.getEvent(eventId));
    }

    /**
     * [요청 흐름] POST /head/events
     * 프론트 요청을 받아 createEvent() 메서드가 입력을 받고 HeadEventService 호출 후 결과를 응답한다.
     */
    @PostMapping
    public ResponseEntity<EventResponseDto> createEvent(@RequestBody EventRequestDto requestDto) {
        return ResponseEntity.ok(eventService.createEvent(requestDto));
    }

    /**
     * [요청 흐름] PUT /head/events/{eventId}
     * 프론트 요청을 받아 updateEvent() 메서드가 입력을 받고 HeadEventService 호출 후 결과를 응답한다.
     */
    @PutMapping("/{eventId}")
    public ResponseEntity<EventResponseDto> updateEvent(
            @PathVariable("eventId") Integer eventId,
            @RequestBody EventRequestDto requestDto) {
        return ResponseEntity.ok(eventService.updateEvent(eventId, requestDto));
    }

    /**
     * [요청 흐름] DELETE /head/events/{eventId}
     * 프론트 요청을 받아 deleteEvent() 메서드가 입력을 받고 HeadEventService 호출 후 결과를 응답한다.
     */
    @DeleteMapping("/{eventId}")
    public ResponseEntity<Void> deleteEvent(@PathVariable("eventId") Integer eventId) {
        eventService.deleteEvent(eventId);
        return ResponseEntity.noContent().build();
    }

    /**
     * [요청 흐름] PATCH /head/events/{eventId}/visibility
     * 프론트 요청을 받아 updateVisibility() 메서드가 입력을 받고 HeadEventService 호출 후 결과를 응답한다.
     */
    @PatchMapping("/{eventId}/visibility")
    public ResponseEntity<EventResponseDto> updateVisibility(
            @PathVariable("eventId") Integer eventId,
            @RequestBody Map<String, Boolean> request) {
        Boolean isVisible = request.get("isVisible");
        return ResponseEntity.ok(eventService.updateVisibility(eventId, isVisible));
    }
}
