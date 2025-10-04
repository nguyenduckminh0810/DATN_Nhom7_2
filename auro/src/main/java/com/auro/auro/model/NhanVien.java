package com.auro.auro.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "nhan_vien")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NhanVien {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tai_khoan", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private TaiKhoan taiKhoan;

    @Column(name = "ho_ten", length = 150)
    private String hoTen;

    @Column(name = "so_dien_thoai", length = 20)
    private String soDienThoai;
}
