package com.auro.auro.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DangNhapRequest {

    @NotBlank(message = "Email hoặc số điện thoại không được để trống")
    private String login;

    @NotBlank(message = "Mật khẩu không được để trống")
    private String matKhau;

    private boolean ghiNho = false;

}
