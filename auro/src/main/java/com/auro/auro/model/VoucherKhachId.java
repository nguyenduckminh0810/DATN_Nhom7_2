package com.auro.auro.model;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Data @NoArgsConstructor @AllArgsConstructor
public class VoucherKhachId implements Serializable {
    private Long idVoucher;
    private Long idKhachHang;
}
