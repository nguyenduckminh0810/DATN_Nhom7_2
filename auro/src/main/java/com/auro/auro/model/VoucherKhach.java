package com.auro.auro.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "voucher_khach")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VoucherKhach {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_voucher", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Voucher voucher;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_khach_hang", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private KhachHang khachHang;

    @Column(name = "trang_thai", columnDefinition = "NVARCHAR(20)") 
    private String trangThai;
}
