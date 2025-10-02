package com.auro.auro.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "gio_hang")
@Data @NoArgsConstructor @AllArgsConstructor
public class GioHang {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Gắn với khách hàng (đã đăng ký). Guest có thể để null, dùng id_phien
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_khach_hang")
    @ToString.Exclude @EqualsAndHashCode.Exclude
    private KhachHang khachHang;

    @Column(name = "id_phien", length = 100) // session id cho khách vãng lai
    private String idPhien;

    @Column(name = "tien_te", length = 10)
    private String tienTe;
}
