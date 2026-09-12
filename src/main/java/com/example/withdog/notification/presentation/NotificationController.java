package com.example.withdog.notification.presentation;

import com.example.withdog.notification.application.NotificationService;
import com.example.withdog.notification.application.dto.NotificationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    //내 알림 목록 조회
    @GetMapping
    public ResponseEntity<Page<NotificationResponse>> getNotifications(@PageableDefault(size = 20) Pageable pageable, @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(notificationService.getNotifications(userId, pageable));
    }

    //알림 읽음 처리
    @PatchMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id, @AuthenticationPrincipal Long userId) {
        notificationService.markAsRead(id, userId);
        return ResponseEntity.noContent().build();
    }
}
