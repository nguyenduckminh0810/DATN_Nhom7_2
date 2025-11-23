package com.auro.auro.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

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
    @JoinColumn(name = "id_tai_khoan") // Revert back to original
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private TaiKhoan taiKhoan;

    @Column(name = "ho_ten", columnDefinition = "NVARCHAR(255)")
    private String hoTen;

    @Column(name = "email", columnDefinition = "NVARCHAR(255)")
    private String email;

    @Column(name = "so_dien_thoai", columnDefinition = "NVARCHAR(20)")
    private String soDienThoai;

    @Column(name = "kieu", columnDefinition = "NVARCHAR(50)")
    private String kieu;

    @Column(name = "avatar", columnDefinition = "NVARCHAR(500)")
    private String avatar;

    @Column(name = "tao_luc")
    private LocalDateTime taoLuc;
}
