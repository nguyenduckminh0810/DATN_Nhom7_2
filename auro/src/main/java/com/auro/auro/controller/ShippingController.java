package com.auro.auro.controller;

import com.auro.auro.dto.request.GHNShippingFeeRequest;
import com.auro.auro.dto.response.GHNShippingFeeResponse;
import com.auro.auro.dto.response.ShippingCalculateRequest;
import com.auro.auro.dto.response.ShippingResponse;
import com.auro.auro.service.GHNShippingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller xử lý API vận chuyển GHN (Giao Hàng Nhanh)
 */
@RestController
@RequestMapping("/api/shipping")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Slf4j
public class ShippingController {

    private final GHNShippingService shippingService;

    /**
     * Lấy danh sách tỉnh/thành phố
     */
    @GetMapping("/provinces")
    public ResponseEntity<ShippingResponse<List<Map<String, Object>>>> getProvinces() {
        try {
            log.info("🌐 API /provinces called");
            List<Map<String, Object>> provinces = shippingService.getProvinces();
            log.info("✅ Returned {} provinces", provinces.size());
            return ResponseEntity.ok(ShippingResponse.success(provinces, "Lấy danh sách tỉnh/thành phố thành công"));
        } catch (Exception e) {
            log.error("❌ Error in /provinces: {}", e.getMessage(), e);
            return ResponseEntity.ok(ShippingResponse.error("Lỗi khi lấy danh sách tỉnh/thành phố: " + e.getMessage()));
        }
    }

    /**
     * Lấy danh sách quận/huyện theo tỉnh
     */
    @GetMapping("/districts")
    public ResponseEntity<ShippingResponse<List<Map<String, Object>>>> getDistricts(
            @RequestParam("provinceId") Integer provinceId) {
        try {
            List<Map<String, Object>> districts = shippingService.getDistricts(provinceId);
            return ResponseEntity.ok(ShippingResponse.success(districts, "Lấy danh sách quận/huyện thành công"));
        } catch (Exception e) {
            return ResponseEntity.ok(ShippingResponse.error("Lỗi khi lấy danh sách quận/huyện: " + e.getMessage()));
        }
    }

    /**
     * Lấy danh sách phường/xã theo quận
     */
    @GetMapping("/wards")
    public ResponseEntity<ShippingResponse<List<Map<String, Object>>>> getWards(
            @RequestParam("districtId") Integer districtId) {
        try {
            List<Map<String, Object>> wards = shippingService.getWards(districtId);
            return ResponseEntity.ok(ShippingResponse.success(wards, "Lấy danh sách phường/xã thành công"));
        } catch (Exception e) {
            return ResponseEntity.ok(ShippingResponse.error("Lỗi khi lấy danh sách phường/xã: " + e.getMessage()));
        }
    }

    /**
     * Lấy danh sách dịch vụ vận chuyển
     */
    @GetMapping("/services")
    public ResponseEntity<ShippingResponse<List<Map<String, Object>>>> getServices(
            @RequestParam("toDistrictId") Integer toDistrictId) {
        try {
            List<Map<String, Object>> services = shippingService.getServices(toDistrictId);
            return ResponseEntity.ok(ShippingResponse.success(services, "Lấy danh sách dịch vụ thành công"));
        } catch (Exception e) {
            return ResponseEntity.ok(ShippingResponse.error("Lỗi khi lấy danh sách dịch vụ: " + e.getMessage()));
        }
    }

    /**
     * Tính phí vận chuyển
     */
    @PostMapping("/calculate")
    public ResponseEntity<ShippingResponse<Map<String, Object>>> calculateShippingFee(
            @RequestBody ShippingCalculateRequest request) {
        try {
            log.info("📥 Received request: {}", request);
            log.info("🔍 toDistrictId: {} (type: {})", request.getToDistrictId(),
                    request.getToDistrictId() != null ? request.getToDistrictId().getClass().getSimpleName() : "null");
            log.info("🔍 toWardCode: {} (type: {})", request.getToWardCode(),
                    request.getToWardCode() != null ? request.getToWardCode().getClass().getSimpleName() : "null");

            // Tạo GHNShippingFeeRequest từ ShippingCalculateRequest
            GHNShippingFeeRequest ghnRequest = new GHNShippingFeeRequest();
            ghnRequest.setToDistrictId(request.getToDistrictId());
            ghnRequest.setToWardCode(request.getToWardCode());
            ghnRequest.setWeight(request.getTotalWeight());
            ghnRequest.setInsuranceValue(
                    request.getInsuranceValue() != null ? request.getInsuranceValue().intValue() : null);
            ghnRequest.setServiceId(request.getServiceId());

            GHNShippingFeeResponse ghnResponse = shippingService.calculateShippingFee(ghnRequest);

            // Convert response to Map
            Map<String, Object> result = new HashMap<>();
            result.put("total", ghnResponse.getData().getTotal());
            result.put("service_fee", ghnResponse.getData().getServiceFee());
            result.put("insurance_fee", ghnResponse.getData().getInsuranceFee());

            return ResponseEntity.ok(ShippingResponse.success(result, "Tính phí vận chuyển thành công"));
        } catch (Exception e) {
            return ResponseEntity.ok(ShippingResponse.error("Lỗi khi tính phí vận chuyển: " + e.getMessage()));
        }
    }
}
