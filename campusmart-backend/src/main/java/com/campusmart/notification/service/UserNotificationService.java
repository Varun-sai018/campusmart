package com.campusmart.notification.service;

import com.campusmart.notification.NotificationType;
import com.campusmart.notification.entity.Notification;
import com.campusmart.notification.repository.NotificationRepository;
import com.campusmart.product.entity.Product;
import com.campusmart.user.entity.User;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserNotificationService {

    private final NotificationRepository notificationRepository;

    @Transactional
    public void sendProductSoldNotifications(Product product, List<User> wishlistUsers) {
        wishlistUsers.forEach(user -> {
            Notification notification = Notification.builder()
                    .recipient(user)
                    .type(NotificationType.WISHLIST_PRODUCT_SOLD)
                    .message("A product from your wishlist, '" + product.getTitle() + "', has been sold.")
                    .read(false)
                    .build();
            notificationRepository.save(notification);
        });
    }
}
