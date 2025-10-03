package com.auro.auro.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "hinh_anh")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HinhAnh {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_san_pham")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private SanPham sanPham;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_bien_the")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private BienTheSanPham bienThe;

    @Column(name = "url", nullable = false, length = 500)
    private String url;

    @Column(name = "thu_tu")
    private Integer thuTu;

    @Column(name = "tao_luc")
    private LocalDateTime taoLuc;
}