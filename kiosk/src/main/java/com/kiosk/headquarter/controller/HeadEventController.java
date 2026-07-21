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

@RestController
@RequestMapping("/head/events")
@RequiredArgsConstructor
public class HeadEventController {

    private final HeadEventService eventService;

    @GetMapping
    public ResponseEntity<List<EventResponseDto>> getAllEvents() {
        return ResponseEntity.ok(eventService.getAllEvents());
    }

    @GetMapping("/{eventId}")
    public ResponseEntity<EventResponseDto> getEvent(@PathVariable("eventId") Integer eventId) {
        return ResponseEntity.ok(eventService.getEvent(eventId));
    }

    @PostMapping
    public ResponseEntity<EventResponseDto> createEvent(@RequestBody EventRequestDto requestDto) {
        return ResponseEntity.ok(eventService.createEvent(requestDto));
    }

    @PutMapping("/{eventId}")
    public ResponseEntity<EventResponseDto> updateEvent(
            @PathVariable("eventId") Integer eventId,
            @RequestBody EventRequestDto requestDto) {
        return ResponseEntity.ok(eventService.updateEvent(eventId, requestDto));
    }

    @DeleteMapping("/{eventId}")
    public ResponseEntity<Void> deleteEvent(@PathVariable("eventId") Integer eventId) {
        eventService.deleteEvent(eventId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{eventId}/visibility")
    public ResponseEntity<EventResponseDto> updateVisibility(
            @PathVariable("eventId") Integer eventId,
            @RequestBody Map<String, Boolean> request) {
        Boolean isVisible = request.get("isVisible");
        return ResponseEntity.ok(eventService.updateVisibility(eventId, isVisible));
    }
}
