package com.auro.auro.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "don_hang_chi_tiet")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DonHangChiTiet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_don_hang", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private DonHang donHang;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_bien_the", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private BienTheSanPham bienThe;

    @Column(name = "ten_hien_thi", length = 200)
    private String tenHienThi;

    @Column(name = "thuoc_tinh_json", columnDefinition = "NVARCHAR(MAX)")
    private String thuocTinhJson;

    @Column(name = "so_luong", nullable = false)
    private Integer soLuong;

    @Column(name = "don_gia", precision = 18, scale = 2)
    private BigDecimal donGia;

    @Column(name = "thanh_tien", precision = 18, scale = 2)
    private BigDecimal thanhTien;
}