package com.campusmart.notification.service;

import com.campusmart.notification.NotificationType;
import com.campusmart.notification.entity.Notification;
import com.campusmart.notification.repository.NotificationRepository;
import com.campusmart.product.entity.Product;
import com.campusmart.user.entity.User;
import com.campusmart.wishlist.repository.WishlistRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WishlistNotificationService {

    private final WishlistRepository wishlistRepository;
    private final NotificationService notificationService;

    @Transactional
    public void notifyWishlistUsersProductSold(Product product) {
        List<User> users = wishlistRepository.findByProduct(product).stream()
                .map(item -> item.getUser())
                .toList();
        users.forEach(user -> notificationService.createNotification(user, NotificationType.WISHLIST_PRODUCT_SOLD,
                "A product from your wishlist, '" + product.getTitle() + "', has been sold."));
    }
}
