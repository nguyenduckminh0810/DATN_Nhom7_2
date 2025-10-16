package com.auro.auro.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonIgnore;

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
    @JsonIgnore
    private SanPham sanPham;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_bien_the")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @JsonIgnore
    private BienTheSanPham bienThe;

    @Column(name = "url", nullable = false, columnDefinition = "NVARCHAR(MAX)")
    private String url;

    @Column(name = "thu_tu")
    private Integer thuTu;

    @Column(name = "tao_luc")
    private LocalDateTime taoLuc;

    @Column(name = "la_dai_dien") // tên cột DB
    private Boolean laDaiDien;
}
