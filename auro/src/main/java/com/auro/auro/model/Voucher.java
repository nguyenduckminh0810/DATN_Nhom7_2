package com.auro.auro.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "voucher")
@Data
@NoArgsConstructor
@AllArgsConstructor

public class Voucher {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ma", nullable = false, unique = true, length = 50)
    private String ma;

    @Column(name = "loai", columnDefinition = "NVARCHAR(50)")
    private String loai;

    @Column(name = "gia_tri", precision = 18, scale = 2)
    private BigDecimal giaTri;

    @Column(name = "giam_toi_da", precision = 18, scale = 2)
    private BigDecimal giamToiDa;

    @Column(name = "don_toi_thieu", precision = 18, scale = 2)
    private BigDecimal donToiThieu;

    @Column(name = "ngay_bat_dau")
    private LocalDateTime batDauLuc;

    @Column(name = "ngay_ket_thuc")
    private LocalDateTime ketThucLuc;

    @Column(name = "so_luong", nullable = true)
    private Integer gioiHanSuDung;

    @Column(name = "dieu_kien_json", columnDefinition = "NVARCHAR(MAX)")
    private String dieuKienJson;

    @Column(name = "ngay_tao")
    private LocalDateTime taoLuc;

    @Column(name = "ngay_cap_nhat")
    private LocalDateTime capNhatLuc;

    @Column(name = "trang_thai", nullable = false)
    private Integer trangThai = 1; // 0 = Đã hủy, 1 = Active
}