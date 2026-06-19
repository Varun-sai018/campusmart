package com.campusmart.exception;

public class ProductAttributeNotFoundException extends RuntimeException {

    public ProductAttributeNotFoundException(Long id) {
        super("Product attribute not found with id: " + id);
    }
}
