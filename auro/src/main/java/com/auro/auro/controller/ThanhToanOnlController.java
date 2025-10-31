package com.auro.auro.controller;

import com.auro.auro.dto.request.ThanhToanOnlRequest;
import com.auro.auro.dto.response.ThanhToanOnlResponse;
import com.auro.auro.service.ThanhToanOnlService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class ThanhToanOnlController {

    private final ThanhToanOnlService ttOnlService;

    // tạo url thanh toán
    @PostMapping("/create")
    public ResponseEntity<ThanhToanOnlResponse> taoUrlThanhToan(@Valid @RequestBody ThanhToanOnlRequest request, HttpServletRequest httpRequest) {
        String ipAddress = layIpAddress(httpRequest);

        ThanhToanOnlResponse response = ttOnlService.taoUrlThanhToan(request, ipAddress);

        if("success".equals(response.getStatus())) {
            return ResponseEntity.ok(response);
        }else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    // xử lí callback
    @GetMapping("/vnpay-return")
    public ResponseEntity<Map<String, Object>> xuLyKetQuaVNPay(@RequestParam Map<String, String> allParams) {
        Map<String, Object> result = ttOnlService.xuLyCallback(allParams);

        if((Boolean) result.get("success")) {
            return ResponseEntity.ok(result);
        }else{
            return ResponseEntity.badRequest().body(result);
        }
  
    }

    // check trạng thái thanh toán
    @GetMapping("/status/{donHangId}")
    public ResponseEntity<Map<String, Object>> kiemTraTrangThai(@PathVariable Long donHangId) {
        Map<String, Object> result = ttOnlService.kiemTraTrangThaiThanhToan(donHangId);

        if((Boolean) result.get("success")) {
            return ResponseEntity.ok(result);
        }else{
            return ResponseEntity.badRequest().body(result);
        }
  
    }

    // lấy địa chỉ ip
    private String layIpAddress(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-FORWARDED-FOR");
        
        if (ipAddress == null || ipAddress.isEmpty()) {
            ipAddress = request.getHeader("X-Real-IP");
        }
        
        if (ipAddress == null || ipAddress.isEmpty()) {
            ipAddress = request.getRemoteAddr();
        }
        
        if ("0:0:0:0:0:0:0:1".equals(ipAddress)) {
            ipAddress = "127.0.0.1";
        }
        
        return ipAddress;
    }


}
