package com.campusmart.notification.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.campusmart.notification.entity.Notification;
import com.campusmart.user.entity.User;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByRecipientOrderByCreatedAtDesc(User recipient);
}
