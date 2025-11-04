package com.auro.auro.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GHNShippingFeeRequest {
    @JsonProperty("from_district_id")
    private Integer fromDistrictId; // Mã quận/huyện gửi hàng

    @JsonProperty("from_ward_code")
    private String fromWardCode; // Mã phường/xã gửi hàng

    @JsonProperty("to_district_id")
    private Integer toDistrictId; // Mã quận/huyện nhận hàng

    @JsonProperty("to_ward_code")
    private String toWardCode; // Mã phường/xã nhận hàng

    @JsonProperty("weight")
    private Integer weight; // Khối lượng (gram)

    @JsonProperty("length")
    private Integer length; // Chiều dài (cm)

    @JsonProperty("width")
    private Integer width; // Chiều rộng (cm)

    @JsonProperty("height")
    private Integer height; // Chiều cao (cm)

    @JsonProperty("insurance_value")
    private Integer insuranceValue; // Giá trị bảo hiểm

    @JsonProperty("coupon")
    private String coupon; // Mã giảm giá (nếu có)

    @JsonProperty("service_id")
    private Integer serviceId; // ID dịch vụ vận chuyển (1: Express, 2: Standard)

    @JsonProperty("service_type_id")
    private Integer serviceTypeId; // Loại dịch vụ (1: Hàng nhẹ, 2: Hàng nặng)
}
