package com.auro.auro.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "don_hang")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DonHang {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "so_don_hang", unique = true, columnDefinition = "NVARCHAR(50)")
    private String soDonHang;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_khach_hang", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private KhachHang khachHang;

    @Column(name = "trang_thai", columnDefinition = "NVARCHAR(30)")
    private String trangThai;

    @Column(name = "tam_tinh", precision = 18, scale = 2)
    private BigDecimal tamTinh;

    @Column(name = "giam_gia_tong", precision = 18, scale = 2)
    private BigDecimal giamGiaTong;

    @Column(name = "phi_van_chuyen", precision = 18, scale = 2)
    private BigDecimal phiVanChuyen;

    @Column(name = "thue_suat", precision = 5, scale = 2)
    private BigDecimal thueSuat;

    @Column(name = "tong_thue", precision = 18, scale = 2)
    private BigDecimal tongThue;

    @Column(name = "tong_thanh_toan", precision = 18, scale = 2, insertable = false, updatable = false)
    private BigDecimal tongThanhToan;

    @Column(name = "tien_te", columnDefinition = "NVARCHAR(10)")
    private String tienTe = "VND";

    @Column(name = "dat_luc")
    private LocalDateTime datLuc = LocalDateTime.now();

    // @Column(name = "kenh_ban", length = 50)
    // private String kenhBan = "online";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_voucher", referencedColumnName = "ma")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Voucher voucher;

    @Column(name = "dia_chi_giao_snapshot", columnDefinition = "NVARCHAR(MAX)")
    private String diaChiGiaoSnapshot;

    @Column(name = "ghi_chu", columnDefinition = "NVARCHAR(500)")
    private String ghiChu;

    @Column(name = "tao_luc")
    private LocalDateTime taoLuc;

    @Column(name = "cap_nhat_luc")
    private LocalDateTime capNhatLuc;

    @OneToMany(mappedBy = "donHang", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<DonHangChiTiet> chiTietList;

}