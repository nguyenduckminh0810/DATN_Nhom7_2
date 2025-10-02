package com.auro.auro.model;

import jakarta.persistence.*;
import lombok.*;

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

    @Column(name = "ho_ten", length = 150)
    private String hoTen;

    @Column(name = "so_dien_thoai", length = 20)
    private String soDienThoai;

    @Column(name = "dia_chi_1", length = 255)
    private String diaChi1;

    @Column(name = "tinh_thanh", length = 100)
    private String tinhThanh;

    @Column(name = "quan_huyen", length = 100)
    private String quanHuyen;

    @Column(name = "mac_dinh")
    private Boolean macDinh = false;
}
