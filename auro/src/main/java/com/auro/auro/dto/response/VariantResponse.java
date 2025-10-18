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
}
