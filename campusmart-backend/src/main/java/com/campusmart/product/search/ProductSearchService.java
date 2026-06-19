package com.campusmart.product.search;

import com.campusmart.product.dto.ProductResponseDto;
import com.campusmart.product.entity.Product;
import com.campusmart.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductSearchService {

    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public Page<ProductResponseDto> searchProducts(ProductSearchCriteria criteria) {
        Pageable pageable = PageRequest.of(
                criteria.pageOrDefault(),
                criteria.sizeOrDefault(),
                parseSort(criteria.sortOrDefault())
        );
        return productRepository.findAll(ProductSpecification.withCriteria(criteria), pageable)
                .map(this::toResponse);
    }

    private Sort parseSort(String sortValue) {
        if (sortValue == null || sortValue.isBlank()) {
            return Sort.by(Sort.Direction.DESC, "createdAt");
        }

        String[] parts = sortValue.split(",");
        String property = parts[0].trim();
        Sort.Direction direction = Sort.Direction.DESC;
        if (parts.length > 1 && parts[1].trim().equalsIgnoreCase("asc")) {
            direction = Sort.Direction.ASC;
        }

        return switch (property) {
            case "price", "createdAt", "title" -> Sort.by(direction, property);
            default -> Sort.by(Sort.Direction.DESC, "createdAt");
        };
    }

    private ProductResponseDto toResponse(Product product) {
        return new ProductResponseDto(
                product.getId(),
                product.getTitle(),
                product.getDescription(),
                product.getPrice(),
                product.getCondition(),
                product.getStatus(),
                product.getSeller().getId(),
                product.getSeller().getFirstName() + " " + product.getSeller().getLastName(),
                product.getCategory().getId(),
                product.getCategory().getName(),
                product.getIsActive(),
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }
}
