package com.auro.auro.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "dia_chi")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DiaChi {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_khach_hang", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private KhachHang khachHang;

    @Column(name = "ho_ten", columnDefinition = "NVARCHAR(150)")
    private String hoTen;

    @Column(name = "so_dien_thoai", columnDefinition = "NVARCHAR(20)")
    private String soDienThoai;

    @Column(name = "dia_chi_1", columnDefinition = "NVARCHAR(255)")
    private String diaChi1;

    @Column(name = "phuong_xa", columnDefinition = "NVARCHAR(100)")
    private String phuongXa;

    @Column(name = "quan_huyen", columnDefinition = "NVARCHAR(100)")
    private String quanHuyen;

    @Column(name = "tinh_thanh", columnDefinition = "NVARCHAR(100)")
    private String tinhThanh;

    @Column(name = "mac_dinh")
    private Boolean macDinh = false;
}