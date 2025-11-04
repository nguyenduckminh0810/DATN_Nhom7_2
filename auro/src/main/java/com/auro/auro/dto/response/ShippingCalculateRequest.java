package com.auro.auro.dto.response;

import lombok.Data;

/**
 * Request để tính phí vận chuyển
 */
@Data
public class ShippingCalculateRequest {
    private Integer toDistrictId; // ID quận/huyện đích
    private String toWardCode; // Mã phường/xã đích
    private Integer totalWeight; // Tổng khối lượng (gram)
    private Long insuranceValue; // Giá trị bảo hiểm (VNĐ)
    private Integer serviceId; // Mã dịch vụ vận chuyển (optional)
}
