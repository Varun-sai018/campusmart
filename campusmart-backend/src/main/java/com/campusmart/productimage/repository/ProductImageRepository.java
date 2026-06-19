package com.campusmart.productimage.repository;

import com.campusmart.product.entity.Product;
import com.campusmart.productimage.entity.ProductImage;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {

    List<ProductImage> findByProductOrderByCreatedAtAsc(Product product);

    long countByProduct(Product product);
}
