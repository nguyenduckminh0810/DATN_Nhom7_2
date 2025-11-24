package com.auro.auro.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryItemResponse {
    // Variant info
    private Long id;
    private String variantSku;
    private String size;
    private String color;
    private String colorHex;
    private String material;
    private Integer stock;
    private BigDecimal price; // Giá của biến thể (nếu null thì dùng giá sản phẩm)
    private String imageUrl;
    private LocalDateTime lastUpdated;
    
    // Product info
    private Long productId;
    private String productName;
    private String productSku; // SKU của sản phẩm (nếu có)
    private String productImage; // Ảnh đại diện của sản phẩm
    private String categoryName;
    private BigDecimal productPrice; // Giá gốc của sản phẩm
}

