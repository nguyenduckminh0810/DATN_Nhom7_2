package com.auro.auro.service;

import com.auro.auro.dto.request.GHNShippingFeeRequest;
import com.auro.auro.dto.response.GHNShippingFeeResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
// DISABLED TEMPORARILY - Uncomment after GHN config
// import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

// @Service  // DISABLED - Enable after configuring GHN token and shop ID
@RequiredArgsConstructor
@Slf4j
public class GHNShippingService {

    private final RestTemplate restTemplate;

    @Value("${ghn.api.url:https://dev-online-gateway.ghn.vn/shiip/public-api}")
    private String ghnApiUrl;

    @Value("${ghn.api.token}")
    private String ghnToken;

    @Value("${ghn.shop.id}")
    private Integer shopId;

    @Value("${ghn.from.district.id:1542}") // Quận 1, TP.HCM (mặc định)
    private Integer fromDistrictId;

    @Value("${ghn.from.ward.code:21211}") // Phường Bến Nghé (mặc định)
    private String fromWardCode;

    /**
     * Tính phí vận chuyển GHN
     */
    public GHNShippingFeeResponse calculateShippingFee(GHNShippingFeeRequest request) {
        try {
            String url = ghnApiUrl + "/v2/shipping-order/fee";

            // Thiết lập header
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Token", ghnToken);
            headers.set("ShopId", shopId.toString());

            // Thiết lập giá trị mặc định nếu không có
            if (request.getFromDistrictId() == null) {
                request.setFromDistrictId(fromDistrictId);
            }
            if (request.getFromWardCode() == null) {
                request.setFromWardCode(fromWardCode);
            }
            if (request.getServiceId() == null) {
                request.setServiceId(53320); // Mã dịch vụ GHN Express
            }
            if (request.getWeight() == null) {
                request.setWeight(200); // Mặc định 200g
            }
            if (request.getLength() == null) {
                request.setLength(15);
            }
            if (request.getWidth() == null) {
                request.setWidth(15);
            }
            if (request.getHeight() == null) {
                request.setHeight(15);
            }

            HttpEntity<GHNShippingFeeRequest> entity = new HttpEntity<>(request, headers);

            log.info("Calling GHN API: {}", url);
            log.info("Request body: {}", request);

            ResponseEntity<GHNShippingFeeResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    GHNShippingFeeResponse.class);

            GHNShippingFeeResponse feeResponse = response.getBody();

            if (feeResponse != null && feeResponse.getCode() == 200) {
                log.info("GHN response: {}", feeResponse);
                return feeResponse;
            } else {
                log.error("GHN error response: {}", feeResponse);
                throw new RuntimeException("Lỗi khi tính phí vận chuyển: " +
                        (feeResponse != null ? feeResponse.getMessage() : "Unknown error"));
            }

        } catch (Exception e) {
            log.error("Error calling GHN API", e);
            throw new RuntimeException("Không thể tính phí vận chuyển: " + e.getMessage());
        }
    }

    /**
     * Lấy danh sách dịch vụ vận chuyển khả dụng
     */
    public Object getAvailableServices(Integer toDistrictId) {
        try {
            String url = ghnApiUrl + "/v2/shipping-order/available-services";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Token", ghnToken);

            String requestBody = String.format(
                    "{\"shop_id\": %d, \"from_district\": %d, \"to_district\": %d}",
                    shopId, fromDistrictId, toDistrictId);

            HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<Object> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    Object.class);

            return response.getBody();

        } catch (Exception e) {
            log.error("Error getting available services", e);
            throw new RuntimeException("Không thể lấy danh sách dịch vụ: " + e.getMessage());
        }
    }

    /**
     * Lấy danh sách tỉnh/thành phố
     */
    public Object getProvinces() {
        try {
            String url = ghnApiUrl + "/master-data/province";

            HttpHeaders headers = new HttpHeaders();
            headers.set("Token", ghnToken);

            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<Object> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    Object.class);

            return response.getBody();

        } catch (Exception e) {
            log.error("Error getting provinces", e);
            throw new RuntimeException("Không thể lấy danh sách tỉnh/thành phố");
        }
    }

    /**
     * Lấy danh sách quận/huyện theo tỉnh
     */
    public Object getDistricts(Integer provinceId) {
        try {
            String url = ghnApiUrl + "/master-data/district?province_id=" + provinceId;

            HttpHeaders headers = new HttpHeaders();
            headers.set("Token", ghnToken);

            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<Object> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    Object.class);

            return response.getBody();

        } catch (Exception e) {
            log.error("Error getting districts", e);
            throw new RuntimeException("Không thể lấy danh sách quận/huyện");
        }
    }

    /**
     * Lấy danh sách phường/xã theo quận
     */
    public Object getWards(Integer districtId) {
        try {
            String url = ghnApiUrl + "/master-data/ward?district_id=" + districtId;

            HttpHeaders headers = new HttpHeaders();
            headers.set("Token", ghnToken);

            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<Object> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    Object.class);

            return response.getBody();

        } catch (Exception e) {
            log.error("Error getting wards", e);
            throw new RuntimeException("Không thể lấy danh sách phường/xã");
        }
    }
}
