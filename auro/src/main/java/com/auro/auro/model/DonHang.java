package com.auro.auro.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "don_hang")
@Data @NoArgsConstructor @AllArgsConstructor
public class DonHang {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "so_don_hang", unique = true, length = 100)
    private String soDonHang;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_khach_hang", nullable = false)
    @ToString.Exclude @EqualsAndHashCode.Exclude
    private KhachHang khachHang;

    @Column(name = "tam_tinh", precision = 18, scale = 2)
    private BigDecimal tamTinh;

    @Column(name = "giam_gia_tong", precision = 18, scale = 2)
    private BigDecimal giamGiaTong;

    @Column(name = "phi_van_chuyen", precision = 18, scale = 2)
    private BigDecimal phiVanChuyen;

    @Column(name = "tong_thanh_toan", precision = 18, scale = 2)
    private BigDecimal tongThanhToan;

    @Column(name = "tien_te", length = 10)
    private String tienTe;

    // Liên kết tới voucher qua cột mã (tham chiếu column "ma" của bảng voucher)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_voucher", referencedColumnName = "ma")
    @ToString.Exclude @EqualsAndHashCode.Exclude
    private Voucher voucher;

    @Column(name = "dia_chi_giao_snapshot", columnDefinition = "NVARCHAR(MAX)")
    private String diaChiGiaoSnapshot;

    @Column(name = "trang_thai", length = 50)
    private String trangThai;

    @Column(name = "ghi_chu", length = 500)
    private String ghiChu;

    @Column(name = "dat_luc")
    private LocalDateTime datLuc;

    @Column(name = "tao_luc")
    private LocalDateTime taoLuc;

    @Column(name = "cap_nhat_luc")
    private LocalDateTime capNhatLuc;
}
