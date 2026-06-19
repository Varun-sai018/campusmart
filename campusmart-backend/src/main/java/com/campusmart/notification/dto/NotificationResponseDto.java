package com.campusmart.notification.dto;

import com.campusmart.notification.NotificationType;
import java.time.LocalDateTime;

public record NotificationResponseDto(
        Long id,
        Long recipientId,
        NotificationType type,
        String message,
        Boolean read,
        LocalDateTime createdAt
) {
}
