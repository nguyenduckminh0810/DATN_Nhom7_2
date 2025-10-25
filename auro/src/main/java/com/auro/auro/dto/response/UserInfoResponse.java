package com.auro.auro.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserInfoResponse {

    private Long id;
    private String email;
    private String soDienThoai;
    private String hoTen;
    private String vaiTro;
    private Boolean trangThai;
    private LocalDateTime taoLuc;
    private LocalDateTime lanDangNhapCuoi;

    private String kieu;

}
