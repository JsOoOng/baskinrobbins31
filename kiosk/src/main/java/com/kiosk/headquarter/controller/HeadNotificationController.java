package com.kiosk.headquarter.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kiosk.headquarter.dto.notification.HeadNotificationResponse;
import com.kiosk.headquarter.dto.notification.HeadNotificationUnreadCountResponse;
import com.kiosk.headquarter.service.HeadNotificationService;

import lombok.RequiredArgsConstructor;

/**
 * [코드 흐름 안내] HeadNotificationController
 *
 * <p>역할: 본사 관리의 알림 HTTP 요청을 받는 진입점이다.</p>
 * <p>호출 흐름: Vue/API 요청 -> 이 컨트롤러(/head/notifications) -> HeadNotificationService -> 응답 DTO 또는 JSON -> 화면 갱신 순서로 이동한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
@RestController
@RequestMapping("/head/notifications")
@RequiredArgsConstructor
public class HeadNotificationController {

    private final HeadNotificationService
            headNotificationService;

    /*
     * 전체 알림 목록 조회
     *
     * GET /head/notifications
     */
    /**
     * [요청 흐름] GET /head/notifications
     * 프론트 요청을 받아 markAsRead() 메서드가 입력을 받고 HeadNotificationService 호출 후 결과를 응답한다.
     */
    @GetMapping
    public ResponseEntity<
            List<HeadNotificationResponse>
    > getNotifications(
            Authentication authentication
    ) {

        Integer adminId =
                extractAdminId(authentication);

        List<HeadNotificationResponse> notifications =
                headNotificationService
                        .getNotifications(
                                adminId
                        );

        return ResponseEntity.ok(
                notifications
        );
    }

    /*
     * 읽지 않은 알림 개수 조회
     *
     * GET /head/notifications/unread-count
     */
    /**
     * [요청 흐름] GET /head/notifications/unread-count
     * 프론트 요청을 받아 markAsRead() 메서드가 입력을 받고 HeadNotificationService 호출 후 결과를 응답한다.
     */
    @GetMapping("/unread-count")
    public ResponseEntity<
            HeadNotificationUnreadCountResponse
    > getUnreadCount(
            Authentication authentication
    ) {

        Integer adminId =
                extractAdminId(authentication);

        HeadNotificationUnreadCountResponse response =
                headNotificationService
                        .getUnreadCount(
                                adminId
                        );

        return ResponseEntity.ok(
                response
        );
    }

    /*
     * 개별 알림 읽음 처리
     *
     * PATCH
     * /head/notifications/{notificationId}/read
     */
    /**
     * [요청 흐름] PATCH /head/notifications/{notificationId}/read
     * 프론트 요청을 받아 markAsRead() 메서드가 입력을 받고 HeadNotificationService 호출 후 결과를 응답한다.
     */
    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<HeadNotificationResponse>
            markAsRead(
                    @PathVariable
                    Integer notificationId,

                    Authentication authentication
            ) {

        Integer adminId =
                extractAdminId(authentication);

        HeadNotificationResponse notification =
                headNotificationService
                        .markAsRead(
                                notificationId,
                                adminId
                        );

        return ResponseEntity.ok(
                notification
        );
    }

    /*
     * 전체 알림 읽음 처리
     *
     * PATCH /head/notifications/read-all
     */
    @PatchMapping("/read-all")
    public ResponseEntity<
            HeadNotificationUnreadCountResponse
    > markAllAsRead(
            Authentication authentication
    ) {

        Integer adminId =
                extractAdminId(authentication);

        HeadNotificationUnreadCountResponse response =
                headNotificationService
                        .markAllAsRead(
                                adminId
                        );

        return ResponseEntity.ok(
                response
        );
    }

    /*
     * JWT 인증 정보에서
     * 본사 관리자 번호 추출
     *
     * Authentication.getName()에는
     * JWT subject가 들어옵니다.
     */
    private Integer extractAdminId(
            Authentication authentication
    ) {

        if (
                authentication == null ||
                !authentication.isAuthenticated()
        ) {
            throw new IllegalArgumentException(
                    "로그인 정보가 없습니다."
            );
        }

        String principal =
                authentication.getName();

        if (
                principal == null ||
                principal.isBlank()
        ) {
            throw new IllegalArgumentException(
                    "관리자 인증 정보가 없습니다."
            );
        }

        try {
            return Integer.valueOf(
                    principal.trim()
            );

        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(
                    "관리자 번호 형식이 올바르지 않습니다."
            );
        }
    }
}