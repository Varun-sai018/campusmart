package com.campusmart.product.search;

import java.math.BigDecimal;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.campusmart.product.dto.ProductResponseDto;
import com.campusmart.product.entity.ProductCondition;
import com.campusmart.product.entity.ProductStatus;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Product Search", description = "Search and filter products")
@RestController
@RequestMapping("/api/products")
public class ProductSearchController {

    private final ProductSearchService productSearchService;

    public ProductSearchController(ProductSearchService productSearchService) {
        this.productSearchService = productSearchService;
    }

    @Operation(summary = "Search active products with filters")
    @GetMapping("/search")
    public ResponseEntity<Page<ProductResponseDto>> searchProducts(
            @Parameter(description = "Search keyword against title and description")
            @RequestParam(required = false) String keyword,
            @Parameter(description = "Filter by category id")
            @RequestParam(required = false) Long categoryId,
            @Parameter(description = "Filter by seller id")
            @RequestParam(required = false) Long sellerId,
            @Parameter(description = "Filter by product condition")
            @RequestParam(required = false) ProductCondition condition,
            @Parameter(description = "Filter by product status")
            @RequestParam(required = false) ProductStatus status,
            @Parameter(description = "Minimum price filter")
            @RequestParam(required = false) BigDecimal minPrice,
            @Parameter(description = "Maximum price filter")
            @RequestParam(required = false) BigDecimal maxPrice,
            @Parameter(description = "Page index")
            @RequestParam(required = false) Integer page,
            @Parameter(description = "Page size")
            @RequestParam(required = false) Integer size,
            @Parameter(description = "Sort by field and direction, e.g. price,asc")
            @RequestParam(required = false) String sort
    ) {
        ProductSearchCriteria criteria = new ProductSearchCriteria(
                keyword,
                categoryId,
                sellerId,
                condition,
                status,
                minPrice,
                maxPrice,
                page,
                size,
                sort
        );
        Page<ProductResponseDto> results = productSearchService.searchProducts(criteria);
        return ResponseEntity.ok(results);
    }
}
