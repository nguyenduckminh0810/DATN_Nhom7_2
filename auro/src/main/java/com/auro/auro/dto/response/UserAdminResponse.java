package com.auro.auro.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UserAdminResponse {
    private Long id;
    private String email;
    private String soDienThoai;
    private String vaiTroMa;
    private String vaiTroTen;
    private Boolean trangThai;
    private LocalDateTime taoLuc;
    private LocalDateTime capNhatLuc;
}
