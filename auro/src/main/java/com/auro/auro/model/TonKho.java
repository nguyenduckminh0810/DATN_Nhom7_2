package com.auro.auro.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "ton_kho")
@Data 
@NoArgsConstructor 
@AllArgsConstructor
public class TonKho {
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_bien_the", nullable = false)
    @ToString.Exclude 
    @EqualsAndHashCode.Exclude
    private BienTheSanPham bienThe;

    @Column(name = "so_luong_ton")
    private Integer soLuongTon = 0;

    @Column(name = "so_luong_giu")
    private Integer soLuongGiu = 0;

    @Column(name = "ton_an_toan")
    private Integer tonAnToan = 0;

    @Column(name = "cap_nhat_luc")
    private LocalDateTime capNhatLuc;
}