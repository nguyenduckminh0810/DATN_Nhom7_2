package com.auro.auro.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "don_hang_chi_tiet")
@Data @NoArgsConstructor @AllArgsConstructor
public class DonHangChiTiet {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_don_hang", nullable = false)
    @ToString.Exclude @EqualsAndHashCode.Exclude
    private DonHang donHang;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_bien_the", nullable = false)
    @ToString.Exclude @EqualsAndHashCode.Exclude
    private BienTheSanPham bienThe;

    @Column(name = "so_luong", nullable = false)
    private Integer soLuong;

    @Column(name = "gia_tai_thoi_diem", precision = 18, scale = 2)
    private BigDecimal giaTaiThoiDiem;
}
