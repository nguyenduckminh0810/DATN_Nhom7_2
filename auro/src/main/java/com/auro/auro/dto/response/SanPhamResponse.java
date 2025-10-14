package com.auro.auro.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SanPhamResponse {
    private Long id;
    private String ten;
    private String slug;
    private String moTa;
    private Long danhMucId;
    private String danhMucTen;
    private BigDecimal gia;
    private String trangThai;
    private LocalDateTime taoLuc;
    private LocalDateTime capNhatLuc;
    private String anhDaiDien;
}
