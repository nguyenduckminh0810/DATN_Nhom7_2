package com.auro.auro.service;

import com.auro.auro.dto.request.GHNShippingFeeRequest;
import com.auro.auro.dto.response.GHNShippingFeeResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
@Slf4j
public class GHNShippingService {

    private final String ghnApiUrl;
    private final String ghnToken;
    private final Integer shopId;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${ghn.from.district.id:1542}")
    private Integer fromDistrictId;

    @Value("${ghn.from.ward.code:21211}")
    private String fromWardCode;

    // Constructor injection
    public GHNShippingService(
            @Value("${ghn.api.url:https://dev-online-gateway.ghn.vn/shiip/public-api}") String ghnApiUrl,
            @Value("${ghn.api.token}") String ghnToken,
            @Value("${ghn.shop.id}") Integer shopId,
            RestTemplate restTemplate) {
        this.ghnApiUrl = ghnApiUrl;
        this.ghnToken = ghnToken;
        this.shopId = shopId;
        this.restTemplate = restTemplate;

        log.info("🚀 GHNShippingService initialized");
        log.info("🌐 GHN API URL: {}", ghnApiUrl);
        log.info("🏪 GHN Shop ID: {}", shopId);
        log.info("🔑 GHN Token: {}... (length: {})",
                ghnToken != null ? ghnToken.substring(0, Math.min(10, ghnToken.length())) : "NULL",
                ghnToken != null ? ghnToken.length() : 0);
    }

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
     * Lấy danh sách tỉnh/thành phố
     */
    public List<Map<String, Object>> getProvinces() {
        try {
            String url = ghnApiUrl + "/master-data/province";
            log.info("🌐 Calling GHN API: {}", url);

            HttpHeaders headers = new HttpHeaders();
            headers.set("Token", ghnToken);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, String.class);

            log.info("✅ GHN API Response Status: {}", response.getStatusCode());

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
            log.error("❌ Error loading provinces from GHN: {}", e.getMessage(), e);
            throw new RuntimeException("Không thể lấy danh sách tỉnh/thành phố");
        }
    }

    /**
     * Lấy danh sách quận/huyện theo tỉnh
     */
    public List<Map<String, Object>> getDistricts(Integer provinceId) {
        try {
            String url = ghnApiUrl + "/master-data/district";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Token", ghnToken);

            String requestBody = String.format("{\"province_id\": %d}", provinceId);
            HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

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

            return districts;

        } catch (Exception e) {
            log.error("Error getting districts", e);
            throw new RuntimeException("Không thể lấy danh sách quận/huyện");
        }
    }

    /**
     * Lấy danh sách phường/xã theo quận
     */
    public List<Map<String, Object>> getWards(Integer districtId) {
        try {
            String url = ghnApiUrl + "/master-data/ward?district_id=" + districtId;

            HttpHeaders headers = new HttpHeaders();
            headers.set("Token", ghnToken);
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

            return wards;

        } catch (Exception e) {
            log.error("Error getting wards", e);
            throw new RuntimeException("Không thể lấy danh sách phường/xã");
        }
    }

    /**
     * Lấy danh sách dịch vụ vận chuyển
     */
    public List<Map<String, Object>> getServices(Integer toDistrictId) {
        try {
            String url = ghnApiUrl + "/v2/shipping-order/available-services";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Token", ghnToken);

            String requestBody = String.format(
                    "{\"shop_id\": %d, \"from_district\": %d, \"to_district\": %d}",
                    shopId, fromDistrictId, toDistrictId);

            HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

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

            return services;

        } catch (Exception e) {
            log.error("Error getting services", e);
            throw new RuntimeException("Không thể lấy danh sách dịch vụ");
        }
    }
}
