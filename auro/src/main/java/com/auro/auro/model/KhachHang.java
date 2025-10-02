package com.auro.auro.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "khach_hang")
@Data
@NoArgsConstructor
@AllArgsConstructor

public class KhachHang {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tai_khoan")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private TaiKhoan taiKhoan;

    @Column(name = "ho_ten", length = 150)
    private String hoTen;

    @Column(name = "email", length = 150)
    private String email;

    @Column(name = "so_dien_thoai", length = 20)
    private String soDienThoai;

    @Column(name = "kieu", length = 50)
    private String kieu;
}
