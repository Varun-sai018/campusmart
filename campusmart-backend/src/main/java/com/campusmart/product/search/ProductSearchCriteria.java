package com.campusmart.product.search;

import java.math.BigDecimal;

import com.campusmart.product.entity.ProductCondition;
import com.campusmart.product.entity.ProductStatus;

public record ProductSearchCriteria(
        String keyword,
        Long categoryId,
        Long sellerId,
        ProductCondition condition,
        ProductStatus status,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        Integer page,
        Integer size,
        String sort
) {

    public int pageOrDefault() {
        return page == null || page < 0 ? 0 : page;
    }

    public int sizeOrDefault() {
        return size == null || size <= 0 ? 20 : size;
    }

    public String sortOrDefault() {
        return sort == null || sort.isBlank() ? "createdAt,desc" : sort;
    }
}
