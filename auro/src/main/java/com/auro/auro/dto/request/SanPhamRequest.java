package com.auro.auro.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SanPhamRequest {
    @NotBlank(message = "Tên sản phẩm là bắt buộc")
    private String ten;

    private String moTa;

    @NotNull(message = "Danh mục là bắt buộc")
    private Long danhMucId;

    @NotNull(message = "Giá là bắt buộc")
    private BigDecimal gia;

    // active / inactive / out-of-stock
    private String trangThai;
    // Optional: allow client-provided slug, otherwise we will generate
    private String slug;
}
