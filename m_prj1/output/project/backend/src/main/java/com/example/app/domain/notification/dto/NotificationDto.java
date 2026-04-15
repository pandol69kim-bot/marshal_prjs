package com.example.app.domain.notification.dto;

import com.example.app.domain.notification.entity.NotificationHistory;

import java.time.LocalDateTime;

public record NotificationDto(
        Long id,
        String channel,
        String title,
        String content,
        String status,
        String errorMsg,
        LocalDateTime sentAt,
        LocalDateTime createdAt
) {
    public static NotificationDto from(NotificationHistory h) {
        return new NotificationDto(
                h.getId(),
                h.getChannel(),
                h.getTitle(),
                h.getContent(),
                h.getStatus(),
                h.getErrorMsg(),
                h.getSentAt(),
                h.getCreatedAt()
        );
    }
}
