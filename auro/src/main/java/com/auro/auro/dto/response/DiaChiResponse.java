package com.auro.auro.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiaChiResponse {
    private Long id;
    private String hoTen;
    private String soDienThoai;
    private String diaChi1;
    private String phuongXa;
    private String quanHuyen;
    private String tinhThanh;
    private Boolean macDinh;
    private String diaChiDayDu;
}
