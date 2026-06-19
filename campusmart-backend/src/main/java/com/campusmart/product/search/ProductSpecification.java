package com.campusmart.product.search;

import org.springframework.data.jpa.domain.Specification;

import com.campusmart.product.entity.Product;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

public class ProductSpecification {

    public static Specification<Product> withCriteria(ProductSearchCriteria criteria) {
        return (Root<Product> root, CriteriaQuery<?> query, CriteriaBuilder builder) -> {
            Predicate predicate = builder.equal(root.get("isActive"), true);

            if (criteria.keyword() != null && !criteria.keyword().isBlank()) {
                String term = "%" + criteria.keyword().trim().toLowerCase() + "%";
                Predicate titlePredicate = builder.like(builder.lower(root.get("title")), term);
                Predicate descriptionPredicate = builder.like(builder.lower(root.get("description")), term);
                predicate = builder.and(predicate, builder.or(titlePredicate, descriptionPredicate));
            }

            if (criteria.categoryId() != null) {
                predicate = builder.and(predicate, builder.equal(root.get("category").get("id"), criteria.categoryId()));
            }

            if (criteria.sellerId() != null) {
                predicate = builder.and(predicate, builder.equal(root.get("seller").get("id"), criteria.sellerId()));
            }

            if (criteria.condition() != null) {
                predicate = builder.and(predicate, builder.equal(root.get("condition"), criteria.condition()));
            }

            if (criteria.status() != null) {
                predicate = builder.and(predicate, builder.equal(root.get("status"), criteria.status()));
            }

            if (criteria.minPrice() != null) {
                predicate = builder.and(predicate, builder.greaterThanOrEqualTo(root.get("price"), criteria.minPrice()));
            }

            if (criteria.maxPrice() != null) {
                predicate = builder.and(predicate, builder.lessThanOrEqualTo(root.get("price"), criteria.maxPrice()));
            }

            return predicate;
        };
    }
}
