package com.auro.auro.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "voucher_khach")
@Data @NoArgsConstructor @AllArgsConstructor
public class VoucherKhach {

    @EmbeddedId
    private VoucherKhachId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("idVoucher")
    @JoinColumn(name = "id_voucher")
    @ToString.Exclude @EqualsAndHashCode.Exclude
    private Voucher voucher;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("idKhachHang")
    @JoinColumn(name = "id_khach_hang")
    @ToString.Exclude @EqualsAndHashCode.Exclude
    private KhachHang khachHang;

    @Column(name = "trang_thai")  // vd: USED / UNUSED
    private String trangThai;
}
