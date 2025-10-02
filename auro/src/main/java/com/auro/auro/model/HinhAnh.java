package com.auro.auro.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "hinh_anh")
@Data @NoArgsConstructor @AllArgsConstructor
public class HinhAnh {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_san_pham", nullable = false)
    @ToString.Exclude @EqualsAndHashCode.Exclude
    private SanPham sanPham;

    @Column(name = "url", nullable = false, length = 500)
    private String url;

    @Column(name = "la_dai_dien")
    private Boolean laDaiDien = false;
}
