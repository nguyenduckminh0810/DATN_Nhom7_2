package com.auro.auro.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "gia")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Gia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_bien_the", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private BienTheSanPham bienThe;

    @Column(name = "tien_te", columnDefinition = "NVARCHAR(10)")
    private String tienTe = "VND";

    @Column(name = "gia_niem_yet", precision = 18, scale = 2)
    private BigDecimal giaNiemYet;

    @Column(name = "gia_khuyen_mai", precision = 18, scale = 2)
    private BigDecimal giaKhuyenMai;

    @Column(name = "bat_dau_luc")
    private LocalDateTime batDauLuc;

    @Column(name = "ket_thuc_luc")
    private LocalDateTime ketThucLuc;

    @Column(name = "tao_luc")
    private LocalDateTime taoLuc;

    @Column(name = "cap_nhat_luc")
    private LocalDateTime capNhatLuc;
}