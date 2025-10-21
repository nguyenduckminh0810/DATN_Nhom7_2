package com.auro.auro.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SanPhamDetailResponse {
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
    private String sku;

    // Extended fields for detail page
    private List<HinhAnhInfo> hinhAnhs;
    private List<BienTheInfo> bienThes;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HinhAnhInfo {
        private Long id;
        private String url;
        private Boolean laDaiDien;
        private Integer thuTu;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BienTheInfo {
        private Long id;
        private String mauSac;
        private String colorHex;
        private String kichThuoc;
        private Integer tonKho;
        private BigDecimal gia;
        private String hinhAnh;
    }
}
