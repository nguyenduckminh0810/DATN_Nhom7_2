package com.auro.auro.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "bien_the_san_pham")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BienTheSanPham {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_san_pham", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private SanPham sanPham;

    @Column(name = "sku", columnDefinition = "NVARCHAR(100)", unique = true)
    private String sku;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_mau_sac")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private MauSac mauSac;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_kich_co")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private KichCo kichCo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_chat_lieu")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private ChatLieu chatLieu;

    @Column(name = "so_luong_ton", nullable = false)
    private Integer soLuongTon;

    @Column(name = "gia", precision = 12, scale = 2)
    private BigDecimal gia; // nếu null thì dùng giá của SanPham
}