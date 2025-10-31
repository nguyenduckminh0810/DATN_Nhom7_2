package com.auro.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Service tích hợp GHN (Giao Hàng Nhanh) API
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GHNShippingService {

    @Value("${ghn.api.url:https://dev-online-gateway.ghn.vn/shiip/public-api}")
    private String ghnApiUrl;

    @Value("${ghn.api.token}")
    private String ghnToken;

    @Value("${ghn.shop.id}")
    private Integer ghnShopId;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Lấy danh sách tỉnh/thành phố
     */
    public List<Map<String, Object>> getProvinces() {
        try {
            String url = ghnApiUrl + "/master-data/province";

            HttpHeaders headers = createHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, String.class);

            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode data = root.get("data");

            List<Map<String, Object>> provinces = new ArrayList<>();
            if (data != null && data.isArray()) {
                for (JsonNode node : data) {
                    Map<String, Object> province = new HashMap<>();
                    province.put("ProvinceID", node.get("ProvinceID").asInt());
                    province.put("ProvinceName", node.get("ProvinceName").asText());
                    province.put("Code", node.has("Code") ? node.get("Code").asText() : "");
                    provinces.add(province);
                }
            }

            log.info("✅ Loaded {} provinces from GHN", provinces.size());
            return provinces;

        } catch (Exception e) {
            log.error("❌ Error loading provinces from GHN: {}", e.getMessage());
            throw new RuntimeException("Không thể tải danh sách tỉnh/thành phố", e);
        }
    }

    /**
     * Lấy danh sách quận/huyện theo tỉnh
     */
    public List<Map<String, Object>> getDistricts(Integer provinceId) {
        try {
            String url = ghnApiUrl + "/master-data/district";

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("province_id", provinceId);

            HttpHeaders headers = createHeaders();
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.POST, entity, String.class);

            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode data = root.get("data");

            List<Map<String, Object>> districts = new ArrayList<>();
            if (data != null && data.isArray()) {
                for (JsonNode node : data) {
                    Map<String, Object> district = new HashMap<>();
                    district.put("DistrictID", node.get("DistrictID").asInt());
                    district.put("DistrictName", node.get("DistrictName").asText());
                    district.put("Code", node.has("Code") ? node.get("Code").asText() : "");
                    districts.add(district);
                }
            }

            log.info("✅ Loaded {} districts for province {}", districts.size(), provinceId);
            return districts;

        } catch (Exception e) {
            log.error("❌ Error loading districts for province {}: {}", provinceId, e.getMessage());
            throw new RuntimeException("Không thể tải danh sách quận/huyện", e);
        }
    }

    /**
     * Lấy danh sách phường/xã theo quận
     */
    public List<Map<String, Object>> getWards(Integer districtId) {
        try {
            String url = ghnApiUrl + "/master-data/ward?district_id=" + districtId;

            HttpHeaders headers = createHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, String.class);

            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode data = root.get("data");

            List<Map<String, Object>> wards = new ArrayList<>();
            if (data != null && data.isArray()) {
                for (JsonNode node : data) {
                    Map<String, Object> ward = new HashMap<>();
                    ward.put("WardCode", node.get("WardCode").asText());
                    ward.put("WardName", node.get("WardName").asText());
                    wards.add(ward);
                }
            }

            log.info("✅ Loaded {} wards for district {}", wards.size(), districtId);
            return wards;

        } catch (Exception e) {
            log.error("❌ Error loading wards for district {}: {}", districtId, e.getMessage());
            throw new RuntimeException("Không thể tải danh sách phường/xã", e);
        }
    }

    /**
     * Lấy danh sách dịch vụ vận chuyển
     */
    public List<Map<String, Object>> getServices(Integer toDistrictId) {
        try {
            String url = ghnApiUrl + "/v2/shipping-order/available-services";

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("shop_id", ghnShopId);
            requestBody.put("from_district", 1542); // Default: Quận 1, TP.HCM
            requestBody.put("to_district", toDistrictId);

            HttpHeaders headers = createHeaders();
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.POST, entity, String.class);

            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode data = root.get("data");

            List<Map<String, Object>> services = new ArrayList<>();
            if (data != null && data.isArray()) {
                for (JsonNode node : data) {
                    Map<String, Object> service = new HashMap<>();
                    service.put("service_id", node.get("service_id").asInt());
                    service.put("short_name", node.get("short_name").asText());
                    service.put("service_type_id", node.get("service_type_id").asInt());
                    services.add(service);
                }
            }

            log.info("✅ Loaded {} services for district {}", services.size(), toDistrictId);
            return services;

        } catch (Exception e) {
            log.error("❌ Error loading services for district {}: {}", toDistrictId, e.getMessage());
            throw new RuntimeException("Không thể tải danh sách dịch vụ", e);
        }
    }

    /**
     * Tính phí vận chuyển
     */
    public Map<String, Object> calculateShippingFee(
            Integer toDistrictId,
            String toWardCode,
            Integer totalWeight,
            Long insuranceValue,
            Integer serviceId) {
        try {
            String url = ghnApiUrl + "/v2/shipping-order/fee";

            // Get service if not provided
            if (serviceId == null) {
                List<Map<String, Object>> services = getServices(toDistrictId);
                if (!services.isEmpty()) {
                    serviceId = (Integer) services.get(0).get("service_id");
                } else {
                    serviceId = 53320; // Default: Express service
                }
            }

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("service_id", serviceId);
            requestBody.put("insurance_value", insuranceValue);
            requestBody.put("coupon", null);
            requestBody.put("from_district_id", 1542); // Default: Quận 1, TP.HCM
            requestBody.put("to_district_id", toDistrictId);
            requestBody.put("to_ward_code", toWardCode);
            requestBody.put("height", 15); // Default height cm
            requestBody.put("length", 20); // Default length cm
            requestBody.put("weight", totalWeight);
            requestBody.put("width", 10); // Default width cm

            HttpHeaders headers = createHeaders();
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.POST, entity, String.class);

            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode data = root.get("data");

            Map<String, Object> result = new HashMap<>();
            if (data != null) {
                long shippingFee = data.get("total").asLong();
                result.put("shippingFee", shippingFee);
                result.put("serviceFee", data.has("service_fee") ? data.get("service_fee").asLong() : shippingFee);
                result.put("insuranceFee", data.has("insurance_fee") ? data.get("insurance_fee").asLong() : 0);

                // Calculate expected delivery time (2-3 days from now)
                LocalDateTime deliveryTime = LocalDateTime.now().plusDays(2);
                result.put("expectedDeliveryTime", deliveryTime.format(DateTimeFormatter.ISO_DATE_TIME));
            }

            log.info("✅ Calculated shipping fee: {} VND for district {}, ward {}",
                    result.get("shippingFee"), toDistrictId, toWardCode);
            return result;

        } catch (Exception e) {
            log.error("❌ Error calculating shipping fee: {}", e.getMessage());
            throw new RuntimeException("Không thể tính phí vận chuyển", e);
        }
    }

    /**
     * Tạo HTTP headers cho GHN API
     */
    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Token", ghnToken);
        if (ghnShopId != null) {
            headers.set("ShopId", String.valueOf(ghnShopId));
        }
        return headers;
    }
}
