package com.auro.auro.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "san_pham")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SanPham {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ten", nullable = false, columnDefinition = "NVARCHAR(200)")
    private String ten;

    // Some databases may require a non-null, unique slug for products
    @Column(name = "slug", nullable = true, columnDefinition = "NVARCHAR(200)", unique = true)
    private String slug;

    @Column(name = "mo_ta", columnDefinition = "NVARCHAR(MAX)")
    private String moTa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_danh_muc", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private DanhMuc danhMuc;

    @Column(name = "gia", nullable = false, precision = 12, scale = 2)
    private BigDecimal gia;

    @Column(name = "trang_thai", nullable = false, length = 50)
    private String trangThai = "active"; // active/inactive/out-of-stock

    @Column(name = "tao_luc")
    private LocalDateTime taoLuc;

    @Column(name = "cap_nhat_luc")
    private LocalDateTime capNhatLuc;
}