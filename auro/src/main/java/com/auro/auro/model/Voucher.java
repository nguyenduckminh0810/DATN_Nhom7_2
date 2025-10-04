package com.auro.auro.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "voucher")
@Data 
@NoArgsConstructor 
@AllArgsConstructor
public class Voucher {
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ma", nullable = false, unique = true, length = 50)
    private String ma;

    @Column(name = "loai", length = 50)
    private String loai;

    @Column(name = "gia_tri", precision = 18, scale = 2)
    private BigDecimal giaTri;

    @Column(name = "giam_toi_da", precision = 18, scale = 2)
    private BigDecimal giamToiDa;

    @Column(name = "don_toi_thieu", precision = 18, scale = 2)
    private BigDecimal donToiThieu;

    @Column(name = "bat_dau_luc")
    private LocalDateTime batDauLuc;

    @Column(name = "ket_thuc_luc")
    private LocalDateTime ketThucLuc;

    @Column(name = "gioi_han_su_dung")
    private Integer gioiHanSuDung;

    @Column(name = "dieu_kien_json", columnDefinition = "NVARCHAR(MAX)")
    private String dieuKienJson;

    @Column(name = "tao_luc")
    private LocalDateTime taoLuc;

    @Column(name = "cap_nhat_luc")
    private LocalDateTime capNhatLuc;
}