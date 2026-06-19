package com.campusmart.product.repository;

import com.campusmart.category.entity.Category;
import com.campusmart.product.entity.Product;
import com.campusmart.product.entity.ProductStatus;
import com.campusmart.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {

    Page<Product> findByCategory(Category category, Pageable pageable);

    Page<Product> findBySeller(User seller, Pageable pageable);

    Page<Product> findByStatus(ProductStatus status, Pageable pageable);

    Page<Product> findByIsActiveTrue(Pageable pageable);

    Page<Product> findByCategoryAndIsActiveTrue(Category category, Pageable pageable);

    Page<Product> findBySellerAndIsActiveTrue(User seller, Pageable pageable);
}

