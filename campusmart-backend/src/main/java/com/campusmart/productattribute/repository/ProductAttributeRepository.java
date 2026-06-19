package com.campusmart.productattribute.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.campusmart.product.entity.Product;
import com.campusmart.productattribute.entity.ProductAttribute;

public interface ProductAttributeRepository extends JpaRepository<ProductAttribute, Long> {

    List<ProductAttribute> findByProductOrderByCreatedAtAsc(Product product);

    long countByProduct(Product product);
}
