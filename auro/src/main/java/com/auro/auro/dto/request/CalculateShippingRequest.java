package com.auro.auro.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CalculateShippingRequest {
    private Integer toDistrictId; // Mã quận/huyện nhận hàng
    private String toWardCode; // Mã phường/xã nhận hàng
    private Integer totalWeight; // Tổng khối lượng đơn hàng (gram)
    private Integer insuranceValue; // Giá trị đơn hàng (để tính bảo hiểm)
    private Integer serviceId; // 1: Express (nhanh), 2: Standard (tiêu chuẩn)
}
