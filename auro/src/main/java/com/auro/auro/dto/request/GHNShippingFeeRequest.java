package com.auro.auro.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GHNShippingFeeRequest {
    private Integer fromDistrictId; // Mã quận/huyện gửi hàng
    private String fromWardCode; // Mã phường/xã gửi hàng
    private Integer toDistrictId; // Mã quận/huyện nhận hàng
    private String toWardCode; // Mã phường/xã nhận hàng
    private Integer weight; // Khối lượng (gram)
    private Integer length; // Chiều dài (cm)
    private Integer width; // Chiều rộng (cm)
    private Integer height; // Chiều cao (cm)
    private Integer insuranceValue; // Giá trị bảo hiểm
    private String coupon; // Mã giảm giá (nếu có)
    private Integer serviceId; // ID dịch vụ vận chuyển (1: Express, 2: Standard)
    private Integer serviceTypeId; // Loại dịch vụ (1: Hàng nhẹ, 2: Hàng nặng)
}
