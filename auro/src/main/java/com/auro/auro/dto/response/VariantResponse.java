package com.auro.auro.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VariantResponse {
    private Long id;
    private String sku;
    private String size;
    private String color;
    private String colorHex;
    private String material;
    private Integer stock;
    private java.math.BigDecimal price; // giá riêng của biến thể
    private String imageUrl; // ảnh của biến thể
    private Boolean available; // có còn hàng không
}
