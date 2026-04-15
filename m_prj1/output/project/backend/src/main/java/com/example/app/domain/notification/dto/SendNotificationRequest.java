package com.example.app.domain.notification.dto;

import jakarta.validation.constraints.NotBlank;

public record SendNotificationRequest(
        @NotBlank(message = "사용자 ID는 필수입니다")
        String userId,

        @NotBlank(message = "채널은 필수입니다")
        String channel,       // EMAIL | SMS

        String phone,         // SMS 발송 시 필수

        @NotBlank(message = "제목은 필수입니다")
        String title,

        @NotBlank(message = "내용은 필수입니다")
        String content
) {}
