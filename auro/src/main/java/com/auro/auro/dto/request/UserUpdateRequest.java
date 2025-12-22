package com.auro.auro.dto.request;

import lombok.Data;

@Data
public class UserUpdateRequest {
    private String email;
    private String soDienThoai;
    private Boolean trangThai;

    private String vaiTroMa;
}
