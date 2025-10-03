package com.auro.auro.model;

import jakarta.persistence.*;
import lombok.*;

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

    @Column(name = "ten", nullable = false, length = 200)
    private String ten;

    @Column(name = "slug", nullable = false, length = 200, unique = true)
    private String slug;

    @Column(name = "thuong_hieu", length = 100)
    private String thuongHieu;

    @Column(name = "mo_ta", columnDefinition = "NVARCHAR(MAX)")
    private String moTa;

    @Column(name = "huong_dan_bao_quan", columnDefinition = "NVARCHAR(MAX)")
    private String huongDanBaoQuan;

    @Column(name = "bang_size_json", columnDefinition = "NVARCHAR(MAX)")
    private String bangSizeJson;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_danh_muc", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private DanhMuc danhMuc;

    @Column(name = "trang_thai")
    private Boolean trangThai = true;

    @Column(name = "tao_luc")
    private LocalDateTime taoLuc;

    @Column(name = "cap_nhat_luc")
    private LocalDateTime capNhatLuc;
}