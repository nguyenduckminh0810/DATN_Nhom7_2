package com.auro.controller;

import com.auro.dto.ShippingCalculateRequest;
import com.auro.dto.ShippingResponse;
import com.auro.service.GHNShippingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controller xử lý API vận chuyển GHN (Giao Hàng Nhanh)
 */
@RestController
@RequestMapping("/api/shipping")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ShippingController {

    private final GHNShippingService shippingService;

    /**
     * Lấy danh sách tỉnh/thành phố
     */
    @GetMapping("/provinces")
    public ResponseEntity<ShippingResponse<List<Map<String, Object>>>> getProvinces() {
        try {
            List<Map<String, Object>> provinces = shippingService.getProvinces();
            return ResponseEntity.ok(ShippingResponse.success(provinces, "Lấy danh sách tỉnh/thành phố thành công"));
        } catch (Exception e) {
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
            Map<String, Object> result = shippingService.calculateShippingFee(
                    request.getToDistrictId(),
                    request.getToWardCode(),
                    request.getTotalWeight(),
                    request.getInsuranceValue(),
                    request.getServiceId());
            return ResponseEntity.ok(ShippingResponse.success(result, "Tính phí vận chuyển thành công"));
        } catch (Exception e) {
            return ResponseEntity.ok(ShippingResponse.error("Lỗi khi tính phí vận chuyển: " + e.getMessage()));
        }
    }
}
