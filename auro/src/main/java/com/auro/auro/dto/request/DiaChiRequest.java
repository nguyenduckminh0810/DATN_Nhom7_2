package com.auro.auro.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DiaChiRequest {
    @NotBlank(message = "Họ tên không được để trống")
    private String hoTen;

    @NotBlank(message = "Số điện thoại không được để trống")
    private String soDienThoai;

    @NotBlank(message = "Địa chỉ không được để trống")
    private String diaChi1;

    @NotBlank(message = "Phường xã không được để trống")
    private String phuongXa;

    private String quanHuyen; // Không bắt buộc - để N/A

    @NotBlank(message = "Tỉnh thành không được để trống")
    private String tinhThanh;

    private Boolean macDinh = false;
}
