package com.auro.auro.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VariantUpsertRequest {
    private String material; // optional
    private List<VariantItem> variants;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VariantItem {
        private String size;
        private String color;
        private String colorHex;
        private String sku; // optional
        private Integer stock;
    }
}
