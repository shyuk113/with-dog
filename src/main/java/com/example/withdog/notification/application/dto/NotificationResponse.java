package com.example.withdog.notification.application.dto;

import com.example.withdog.notification.domain.Notification;
import com.example.withdog.notification.domain.NotificationType;

import java.time.LocalDateTime;

public record NotificationResponse(
        Long id,
        NotificationType type,
        String message,
        Long postId,
        Long commentId,
        boolean read,
        LocalDateTime createdAt
) {
    public static NotificationResponse from(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getType(),
                notification.getMessage(),
                notification.getPost().getId(),
                notification.getComment().getId(),
                notification.isRead(),
                notification.getCreatedAt()
        );
    }
}
