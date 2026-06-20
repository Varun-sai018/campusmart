package com.campusmart.config;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.campusmart.cart.entity.CartItem;
import com.campusmart.cart.repository.CartItemRepository;
import com.campusmart.category.entity.Category;
import com.campusmart.category.repository.CategoryRepository;
import com.campusmart.notification.NotificationType;
import com.campusmart.notification.entity.Notification;
import com.campusmart.notification.repository.NotificationRepository;
import com.campusmart.order.entity.Order;
import com.campusmart.order.entity.OrderItem;
import com.campusmart.order.entity.OrderStatus;
import com.campusmart.order.repository.OrderRepository;
import com.campusmart.product.entity.Product;
import com.campusmart.product.entity.ProductCondition;
import com.campusmart.product.entity.ProductStatus;
import com.campusmart.product.repository.ProductRepository;
import com.campusmart.productattribute.entity.ProductAttribute;
import com.campusmart.productattribute.repository.ProductAttributeRepository;
import com.campusmart.productimage.entity.ProductImage;
import com.campusmart.productimage.repository.ProductImageRepository;
import com.campusmart.review.entity.Review;
import com.campusmart.review.repository.ReviewRepository;
import com.campusmart.role.entity.Role;
import com.campusmart.role.entity.RoleName;
import com.campusmart.role.repository.RoleRepository;
import com.campusmart.user.entity.User;
import com.campusmart.user.repository.UserRepository;
import com.campusmart.wishlist.entity.WishlistItem;
import com.campusmart.wishlist.repository.WishlistRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DemoDataService {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final ProductAttributeRepository productAttributeRepository;
    private final CartItemRepository cartItemRepository;
    private final WishlistRepository wishlistRepository;
    private final OrderRepository orderRepository;
    private final ReviewRepository reviewRepository;
    private final NotificationRepository notificationRepository;
    private final PasswordEncoder passwordEncoder;

    private final Random rnd = new Random(12345);

    @Transactional
    public String seed(boolean force) {
        // Do not skip seeding solely because users already exist.

        // Ensure roles exist (idempotent)
        Role adminRole = roleRepository.findByName(RoleName.ADMIN).orElseGet(() -> roleRepository.save(Role.builder().name(RoleName.ADMIN).build()));
        Role buyerRole = roleRepository.findByName(RoleName.BUYER).orElseGet(() -> roleRepository.save(Role.builder().name(RoleName.BUYER).build()));
        Role sellerRole = roleRepository.findByName(RoleName.SELLER).orElseGet(() -> roleRepository.save(Role.builder().name(RoleName.SELLER).build()));

        // Ensure admin user exists
        if (!userRepository.existsByEmail("admin@campusmart.com")) {
            User admin = User.builder()
                    .firstName("Admin")
                    .lastName("User")
                    .email("admin@campusmart.com")
                    .password(passwordEncoder.encode("Admin123"))
                    .phoneNumber("000-000-0000")
                    .roles(Set.of(adminRole))
                    .isActive(true)
                    .build();
            userRepository.save(admin);
        }

        // Ensure seller users exist
        for (int i = 1; i <= 3; i++) {
            String email = "seller" + i + "@campusmart.com";
            if (!userRepository.existsByEmail(email)) {
                User s = User.builder()
                        .firstName("Seller" + i)
                        .lastName("Demo")
                        .email(email)
                        .password(passwordEncoder.encode("Seller123"))
                        .phoneNumber("900-000-000" + i)
                        .roles(Set.of(sellerRole))
                        .isActive(true)
                        .build();
                userRepository.save(s);
            }
        }

        // Ensure buyer users exist
        for (int i = 1; i <= 3; i++) {
            String email = "buyer" + i + "@campusmart.com";
            if (!userRepository.existsByEmail(email)) {
                User b = User.builder()
                        .firstName("Buyer" + i)
                        .lastName("Demo")
                        .email(email)
                        .password(passwordEncoder.encode("Buyer123"))
                        .phoneNumber("800-000-000" + i)
                        .roles(Set.of(buyerRole))
                        .isActive(true)
                        .build();
                userRepository.save(b);
            }
        }

        // Ensure categories exist (use canonical names required by demo)
        String[] catNames = new String[]{
            "BOOKS",
            "ELECTRONICS",
            "CALCULATORS",
            "HOSTEL_ESSENTIALS",
            "LAB_EQUIPMENT",
            "CYCLES",
            "SPORTS",
            "FURNITURE"
        };
        Map<String, Category> categories = new HashMap<>();
        for (String n : catNames) {
            String lookup = n.replaceAll("_", " ");
            Category c = categoryRepository.findByNameIgnoreCase(n).orElseGet(() ->
                categoryRepository.save(Category.builder().name(n).description(lookup + " demo category").isActive(true).build())
            );
            categories.put(n, c);
        }

        // Ensure products exist
        if (productRepository.count() == 0) {
            String[] titles = new String[]{
                    "Java Programming Book",
                    "Data Structures Book",
                    "HP Calculator",
                    "Dell Monitor",
                    "Gaming Mouse",
                    "Study Table",
                    "Office Chair",
                    "Mountain Cycle",
                    "Cricket Bat",
                    "Lab Coat",
                    "Algorithms Book",
                    "Discrete Math Book",
                    "Lenovo Laptop",
                    "Wireless Keyboard",
                    "Desk Lamp",
                    "Graphing Calculator",
                    "Bicycle Helmet",
                    "Football",
                    "Notebook Pack",
                    "Pen Set",
                    "Printer",
                    "USB Drive 64GB",
                    "External HDD 1TB",
                    "Office Desk",
                    "Bookshelf",
                    "Chemistry Kit",
                    "Microscope",
                    "Whiteboard",
                    "Study Chair",
                    "Bluetooth Speaker"
            };

            List<User> sellerPool = userRepository.findAll().stream().filter(u -> u.getRoles().stream().anyMatch(r -> r.getName() == RoleName.SELLER)).toList();
            for (int i = 0; i < titles.length; i++) {
                String t = titles[i];
                Category cat = guessCategory(t, categories);
                User seller = sellerPool.get(i % sellerPool.size());
                BigDecimal price = BigDecimal.valueOf(10 + rnd.nextInt(490) + (rnd.nextInt(100) / 100.0)).setScale(2, BigDecimal.ROUND_HALF_UP);

                Product p = Product.builder()
                        .title(t)
                        .description(t + " — gently used, good condition. Great for students.")
                        .price(price)
                        .condition(randomCondition())
                        .status(ProductStatus.AVAILABLE)
                        .seller(seller)
                        .category(cat)
                        .isActive(true)
                        .build();
                productRepository.save(p);
            }
        }

        // Ensure product images exist
        if (productImageRepository.count() == 0) {
            List<Product> allProducts = productRepository.findAll();
            for (Product p : allProducts) {
                for (int im = 1; im <= 2; im++) {
                    ProductImage img = ProductImage.builder()
                            .imageUrl(createPlaceholderImageDataUrl(p.getTitle(), im))
                            .storageKey(p.getId() + "-" + im)
                            .primaryImage(im == 1)
                            .product(p)
                            .build();
                    productImageRepository.save(img);
                }
            }
        }

        // Ensure product attributes exist
        if (productAttributeRepository.count() == 0) {
            for (Product p : productRepository.findAll()) {
                List<ProductAttribute> attrs = generateAttributesForTitle(p);
                for (ProductAttribute a : attrs) {
                    a.setProduct(p);
                    productAttributeRepository.save(a);
                }
            }
        }

        // Create orders (10) if none exist
        if (orderRepository.count() == 0) {
            List<User> allBuyers = userRepository.findAll().stream().filter(u -> u.getRoles().stream().anyMatch(r -> r.getName() == RoleName.BUYER)).toList();
            List<Product> allProducts = productRepository.findAll();
            for (int i = 0; i < 10 && !allBuyers.isEmpty(); i++) {
                User buyer = allBuyers.get(i % allBuyers.size());
                Order ord = Order.builder().buyer(buyer).totalAmount(BigDecimal.ZERO).orderStatus(OrderStatus.COMPLETED).build();
                List<OrderItem> items = new ArrayList<>();
                int itemsCount = 1 + rnd.nextInt(3);
                BigDecimal total = BigDecimal.ZERO;
                for (int k = 0; k < itemsCount; k++) {
                    Product p = allProducts.get((i * 3 + k) % allProducts.size());
                    if (p.getSeller().getId().equals(buyer.getId())) continue;
                    OrderItem oi = OrderItem.builder()
                            .order(ord)
                            .product(p)
                            .seller(p.getSeller())
                            .priceAtPurchase(p.getPrice())
                            .quantity(1)
                            .build();
                    items.add(oi);
                    total = total.add(p.getPrice());
                }
                if (items.isEmpty()) continue;
                ord.setOrderItems(items);
                ord.setTotalAmount(total);
                orderRepository.save(ord);
            }
        }

        // Create reviews (20) if none exist — only for buyers who purchased the product and not their own product
        if (reviewRepository.count() == 0) {
            List<User> buyers = userRepository.findAll().stream().filter(u -> u.getRoles().stream().anyMatch(r -> r.getName() == RoleName.BUYER)).toList();
            List<Product> products = productRepository.findAll();
            int reviewsToCreate = 20;
            int created = 0;
            for (User b : buyers) {
                if (created >= reviewsToCreate) break;
                for (Product p : products) {
                    if (created >= reviewsToCreate) break;
                    if (p.getSeller().getId().equals(b.getId())) continue;
                    if (!orderRepository.existsByOrderItemsProductAndBuyer(p, b)) continue;
                    if (reviewRepository.findAll().stream().anyMatch(r -> r.getBuyer().getId().equals(b.getId()) && r.getProduct().getId().equals(p.getId()))) continue;
                    Review rv = Review.builder().buyer(b).product(p).rating(3 + rnd.nextInt(3)).comment("Good product - demo review").build();
                    reviewRepository.save(rv);
                    created++;
                }
            }
        }

        // Wishlist and cart entries for buyers (idempotent)
        List<User> buyersAll = userRepository.findAll().stream().filter(u -> u.getRoles().stream().anyMatch(r -> r.getName() == RoleName.BUYER)).toList();
        List<Product> availableProducts = productRepository.findAll();
        for (User b : buyersAll) {
            List<WishlistItem> existing = wishlistRepository.findAll().stream().filter(w -> w.getUser().getId().equals(b.getId())).toList();
            if (existing.isEmpty()) {
                for (int x = 0; x < 2 && x < availableProducts.size(); x++) {
                    Product p = availableProducts.get((b.getId().intValue() + x) % availableProducts.size());
                    if (p.getSeller().getId().equals(b.getId())) continue;
                    WishlistItem w = WishlistItem.builder().user(b).product(p).build();
                    wishlistRepository.save(w);
                }
            }

            List<CartItem> carts = cartItemRepository.findAll().stream().filter(c -> c.getUser().getId().equals(b.getId())).toList();
            if (carts.isEmpty()) {
                Product p = availableProducts.get((b.getId().intValue()) % availableProducts.size());
                if (!p.getSeller().getId().equals(b.getId())) {
                    CartItem ci = CartItem.builder().user(b).product(p).quantity(1).build();
                    cartItemRepository.save(ci);
                }
            }

            List<Notification> notifs = notificationRepository.findByRecipientOrderByCreatedAtDesc(b);
            if (notifs.isEmpty()) {
                notificationRepository.save(Notification.builder().recipient(b).type(NotificationType.ORDER_PLACED).message("Welcome demo user: your demo account is ready.").build());
                notificationRepository.save(Notification.builder().recipient(b).type(NotificationType.WISHLIST_ADDED).message("Check out new demo products in the catalog.").build());
            }
        }

        if (notificationRepository.count() == 0) {
            List<User> userList = userRepository.findAll();
            if (userList.size() >= 2) {
                notificationRepository.save(Notification.builder().recipient(userList.get(0)).type(NotificationType.ORDER_PLACED).message("Your order has been placed successfully.").build());
            }
        }

        long rolesCount = roleRepository.count();
        long usersCount = userRepository.count();
        long categoriesCount = categoryRepository.count();
        long productsCount = productRepository.count();
        long imagesCount = productImageRepository.count();
        long attributesCount = productAttributeRepository.count();
        long ordersCount = orderRepository.count();
        long reviewsCount = reviewRepository.count();
        long notificationsCount = notificationRepository.count();

        String summary = "Demo seeding complete. Roles: " + rolesCount +
                ", Users: " + usersCount +
                ", Categories: " + categoriesCount +
                ", Products: " + productsCount +
                ", Images: " + imagesCount +
                ", Attributes: " + attributesCount +
                ", Orders: " + ordersCount +
                ", Reviews: " + reviewsCount +
                ", Notifications: " + notificationsCount;

        System.out.println("DEMO DATA CREATED SUCCESSFULLY — Roles:" + rolesCount + ", Users:" + usersCount + ", Categories:" + categoriesCount + ", Products:" + productsCount + ", Images:" + imagesCount + ", Attributes:" + attributesCount + ", Orders:" + ordersCount + ", Reviews:" + reviewsCount + ", Notifications:" + notificationsCount);

        return summary;
    }

    private ProductCondition randomCondition() {
        ProductCondition[] vals = ProductCondition.values();
        return vals[rnd.nextInt(vals.length)];
    }

    private Category guessCategory(String title, Map<String, Category> categories) {
        String t = title.toLowerCase();
        if (t.contains("book") || t.contains("notebook")) return categories.get("BOOKS");
        if (t.contains("calculator") || t.contains("graphing") || t.contains("microscope") || t.contains("chemistry")) return categories.get("LAB_EQUIPMENT");
        if (t.contains("dell") || t.contains("lenovo") || t.contains("printer") || t.contains("usb") || t.contains("hdd") || t.contains("keyboard") || t.contains("mouse") || t.contains("monitor") || t.contains("speaker")) return categories.get("ELECTRONICS");
        if (t.contains("desk") || t.contains("chair") || t.contains("bookshelf") || t.contains("table")) return categories.get("FURNITURE");
        if (t.contains("cycle") || t.contains("bicycle") || t.contains("helmet")) return categories.get("CYCLES");
        if (t.contains("cricket") || t.contains("football")) return categories.get("SPORTS");
        if (t.contains("pen") || t.contains("stationery") || t.contains("notebook")) return categories.get("HOSTEL_ESSENTIALS");
        return categories.getOrDefault("HOSTEL_ESSENTIALS", categories.values().stream().findFirst().orElse(null));
    }

    private String encodeForUrl(String s) {
        return s.replaceAll("\\s+","+").replaceAll("[^A-Za-z0-9+\\-]","");
    }

    private String createPlaceholderImageDataUrl(String title, int imageIndex) {
        String background = imageIndex % 2 == 0 ? "%23f0f0f0" : "%23e8f2ff";
        String text = title == null ? "CampusMart" : title.length() > 18 ? title.substring(0, 18) + "…" : title;
        String svg = "<svg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 180 90'>"
                + "<rect width='180' height='90' fill='" + background + "'/>"
                + "<text x='90' y='50' text-anchor='middle' fill='#555' font-size='14' font-family='Arial,sans-serif'>"
                + escapeXml(text)
                + "</text>"
                + "</svg>";
        String encodedSvg = URLEncoder.encode(svg, StandardCharsets.UTF_8).replace("+", "%20");
        return "data:image/svg+xml;charset=UTF-8," + encodedSvg;
    }

    private String escapeXml(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&#39;");
    }

    private List<ProductAttribute> generateAttributesForTitle(Product p) {
        List<ProductAttribute> out = new ArrayList<>();
        String t = p.getTitle().toLowerCase();
        if (t.contains("book")) {
            out.add(ProductAttribute.builder().attributeName("Author").attributeValue("Various Authors").build());
            out.add(ProductAttribute.builder().attributeName("Edition").attributeValue("3rd").build());
        } else if (t.contains("laptop") || t.contains("dell") || t.contains("lenovo")) {
            out.add(ProductAttribute.builder().attributeName("Brand").attributeValue("Generic").build());
            out.add(ProductAttribute.builder().attributeName("RAM").attributeValue("8GB").build());
        } else if (t.contains("calculator") || t.contains("graphing")) {
            out.add(ProductAttribute.builder().attributeName("Brand").attributeValue("HP").build());
        } else if (t.contains("cycle") || t.contains("bicycle")) {
            out.add(ProductAttribute.builder().attributeName("Color").attributeValue("Black").build());
            out.add(ProductAttribute.builder().attributeName("Weight").attributeValue("12kg").build());
        } else {
            out.add(ProductAttribute.builder().attributeName("Condition Notes").attributeValue("Used, in good condition").build());
        }
        return out;
    }
}
