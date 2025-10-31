package com.auro.auro.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GHNShippingFeeResponse {
    private Integer code;
    private String message;
    private FeeData data;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FeeData {
        private Integer total; // Tổng phí
        private Integer serviceFee; // Phí dịch vụ
        private Integer insuranceFee; // Phí bảo hiểm
        private Integer pickStationFee; // Phí lấy tại bưu cục
        private Integer couponValue; // Giá trị giảm giá
        private Integer r2sFee; // Phí hoàn trả
        private String deliveryType; // Loại giao hàng (xteam, ecom, none)
        private String expectedDeliveryTime; // Thời gian giao hàng dự kiến
    }
}
