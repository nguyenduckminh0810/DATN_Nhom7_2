package com.auro.auro.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "voucher")
@Data @NoArgsConstructor @AllArgsConstructor
public class Voucher {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ma", nullable = false, unique = true, length = 50)
    private String ma;

    @Column(name = "loai", length = 50)          // percent/fixed
    private String loai;

    @Column(name = "gia_tri", precision = 18, scale = 2)
    private BigDecimal giaTri;

    @Column(name = "giam_toi_da", precision = 18, scale = 2)
    private BigDecimal giamToiDa;

    @Column(name = "don_toi_thieu", precision = 18, scale = 2)
    private BigDecimal donToiThieu;

    @Column(name = "ngay_bat_dau")
    private LocalDateTime ngayBatDau;

    @Column(name = "ngay_ket_thuc")
    private LocalDateTime ngayKetThuc;

    @Column(name = "so_luong")
    private Integer soLuong;

    @Column(name = "so_luong_da_dung")
    private Integer soLuongDaDung = 0;

    @Column(name = "tao_luc")
    private LocalDateTime taoLuc;

    @Column(name = "cap_nhat_luc")
    private LocalDateTime capNhatLuc;
}
