package com.kiosk.branch.notification.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kiosk.branch.notification.dto.BranchNotificationResponse;
import com.kiosk.branch.notification.service.BranchNotificationService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/branch/stores/{storeId}/notifications")
@RequiredArgsConstructor
public class BranchNotificationController {
    private final BranchNotificationService service;

    @GetMapping("/unread")
    public ResponseEntity<List<BranchNotificationResponse>> unread(@PathVariable Integer storeId) {
        return ResponseEntity.ok(service.getUnread(storeId));
    }

    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<Void> read(@PathVariable Integer storeId,
                                     @PathVariable Integer notificationId) {
        service.markRead(storeId, notificationId);
        return ResponseEntity.noContent().build();
    }
}
