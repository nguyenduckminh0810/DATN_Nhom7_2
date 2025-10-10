package com.auro.auro.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "tai_khoan", uniqueConstraints = {
    @UniqueConstraint(columnNames = "email"),
    @UniqueConstraint(columnNames = "so_dien_thoai")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaiKhoan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "email", nullable = true, length = 150)
    private String email;

    @Column(name = "so_dien_thoai", nullable = true, length = 20)
    private String soDienThoai;

    @Column(name = "mat_khau_hash", nullable = false, length = 255)
    private String matKhauHash;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_vai_tro", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private VaiTro vaiTro;

    @Column(name = "trang_thai")
    private Boolean trangThai = true;

    @Column(name = "tao_luc")
    private LocalDateTime taoLuc;

    @Column(name = "cap_nhat_luc")
    private LocalDateTime capNhatLuc;
}