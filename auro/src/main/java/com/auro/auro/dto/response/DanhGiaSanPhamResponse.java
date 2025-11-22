package com.auro.auro.dto.response;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DanhGiaSanPhamResponse {
    private Long id;
    private Long sanPhamId;
    private String sanPhamTen;
    private Long khachHangId;
    private String khachHangTen;
    private String khachHangAvatar;
    private Integer soSao;
    private String noiDung;
    private LocalDateTime taoLuc;
    private LocalDateTime capNhatLuc;
}

