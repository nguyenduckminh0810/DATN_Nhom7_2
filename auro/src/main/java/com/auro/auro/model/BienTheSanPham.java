package com.auro.auro.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "bien_the_san_pham")
@Data @NoArgsConstructor @AllArgsConstructor
public class BienTheSanPham {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Thuộc về sản phẩm nào
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_san_pham", nullable = false)
    @ToString.Exclude @EqualsAndHashCode.Exclude
    private SanPham sanPham;

    @Column(name = "sku", length = 100, unique = true)
    private String sku;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_mau_sac")
    @ToString.Exclude @EqualsAndHashCode.Exclude
    private MauSac mauSac;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_kich_co")
    @ToString.Exclude @EqualsAndHashCode.Exclude
    private KichCo kichCo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_chat_lieu")
    @ToString.Exclude @EqualsAndHashCode.Exclude
    private ChatLieu chatLieu;

    @Column(name = "thuoc_tinh_bo_sung_json", columnDefinition = "NVARCHAR(MAX)")
    private String thuocTinhBoSungJson; // JSON tuỳ biến

    @Column(name = "so_luong_ton")
    private Integer soLuongTon = 0;
}
