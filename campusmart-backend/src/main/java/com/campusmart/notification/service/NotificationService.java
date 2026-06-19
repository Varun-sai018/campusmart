package com.campusmart.notification.service;

import com.campusmart.exception.ResourceNotFoundException;
import com.campusmart.notification.NotificationType;
import com.campusmart.notification.dto.NotificationResponseDto;
import com.campusmart.notification.entity.Notification;
import com.campusmart.notification.repository.NotificationRepository;
import com.campusmart.user.entity.User;
import com.campusmart.user.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Transactional
    public NotificationResponseDto createNotification(User recipient, NotificationType type, String message) {
        Notification notification = Notification.builder()
                .recipient(recipient)
                .type(type)
                .message(message)
                .read(false)
                .build();
        return toDto(notificationRepository.save(notification));
    }

    @Transactional(readOnly = true)
    public List<NotificationResponseDto> getNotifications(Long currentUserId) {
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + currentUserId));
        return notificationRepository.findByRecipientOrderByCreatedAtDesc(user).stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public void markAsRead(Long notificationId, Long currentUserId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " + notificationId));
        if (!notification.getRecipient().getId().equals(currentUserId)) {
            throw new AccessDeniedException("Cannot mark notifications that do not belong to the authenticated user");
        }
        notification.setRead(true);
        notificationRepository.save(notification);
    }

    private NotificationResponseDto toDto(Notification notification) {
        return new NotificationResponseDto(
                notification.getId(),
                notification.getRecipient().getId(),
                notification.getType(),
                notification.getMessage(),
                notification.getRead(),
                notification.getCreatedAt()
        );
    }
}
