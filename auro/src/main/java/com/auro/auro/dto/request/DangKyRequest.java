package com.auro.auro.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.AssertTrue;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DangKyRequest {

    @Email(message = "Email không hợp lệ")
    @Size(max = 150, message = "Email không được vượt quá 150 ký tự")
    private String email;

    @Pattern(regexp = "^(0|\\+84)[0-9]{9,10}$", 
             message = "Số điện thoại không hợp lệ (VD: 0912345678 hoặc +84912345678)")
    private String soDienThoai;

    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(min = 6, max = 50, message = "Mật khẩu phải có từ 6 - 50 ký tự")
    private String matKhau;

    @NotBlank(message = "Họ tên không được để trống")
    @Size(min = 2, max = 50, message = "Họ tên phải có từ 2 - 50 ký tự")
    private String hoTen;

    @AssertTrue(message = "Phải cung cấp ít nhất email hoặc số điện thoại")
    private boolean isContactInfoValid() {
        return (email != null && !email.trim().isEmpty()) || 
               (soDienThoai != null && !soDienThoai.trim().isEmpty());
    }

    private String loaiTaiKhoan = "CUSTOMER";
}
