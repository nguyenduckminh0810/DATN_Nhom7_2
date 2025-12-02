package com.auro.auro.dto.request;

import lombok.Data;

@Data
public class UserUpdateRequest {
    private String email;
    private String soDienThoai;
    private Boolean trangThai; // optional; admin can change, staff only for customers

    private String vaiTroMa;
}
