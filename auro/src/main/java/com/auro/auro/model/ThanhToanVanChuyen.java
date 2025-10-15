package com.auro.auro.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "thanh_toan_van_chuyen")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ThanhToanVanChuyen {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_don_hang", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private DonHang donHang;

    @Column(name = "pt_thanh_toan", columnDefinition = "NVARCHAR(50)")
    private String ptThanhToan;

    @Column(name = "tt_thanh_toan", columnDefinition = "NVARCHAR(50)")
    private String ttThanhToan;

    @Column(name = "so_tien", precision = 18, scale = 2)
    private BigDecimal soTien;

    @Column(name = "ma_giao_dich", columnDefinition = "NVARCHAR(100)")
    private String maGiaoDich;

    @Column(name = "thanh_toan_luc")
    private LocalDateTime thanhToanLuc;

    @Column(name = "hang_van_chuyen", columnDefinition = "NVARCHAR(100)")
    private String hangVanChuyen;

    @Column(name = "dich_vu_vc", columnDefinition = "NVARCHAR(100)")
    private String dichVuVc;

    @Column(name = "ma_van_don", columnDefinition = "NVARCHAR(100)")
    private String maVanDon;

    @Column(name = "tt_van_chuyen", columnDefinition = "NVARCHAR(50)")
    private String ttVanChuyen;

    @Column(name = "giao_di_luc")
    private LocalDateTime giaoDiLuc;

    @Column(name = "giao_thanh_cong_luc")
    private LocalDateTime giaoThanhCongLuc;

    @Column(name = "du_lieu_json", columnDefinition = "NVARCHAR(MAX)")
    private String duLieuJson;

    @Column(name = "tao_luc")
    private LocalDateTime taoLuc;
}