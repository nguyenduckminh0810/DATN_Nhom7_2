package com.auro.auro.controller;

import com.auro.auro.dto.request.ThanhToanOnlRequest;
import com.auro.auro.dto.response.ThanhToanOnlResponse;
import com.auro.auro.service.ThanhToanOnlService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.servlet.view.RedirectView;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import java.util.Map;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class ThanhToanOnlController {

    private final ThanhToanOnlService ttOnlService;

    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;

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
     public RedirectView xuLyKetQuaVNPay(@RequestParam Map<String, String> allParams) {
         Map<String, Object> result = ttOnlService.xuLyCallback(allParams);

         StringBuilder redirectUrl = new StringBuilder(frontendUrl);
         redirectUrl.append("/payment/vnpay-return");

         boolean first = true;
         for (Map.Entry<String, String> entry : allParams.entrySet()) {
             redirectUrl.append(first ? "?" : "&");
             redirectUrl.append(entry.getKey())
                        .append("=")
                        .append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
             first = false;
         }

         redirectUrl.append(first ? "?" : "&")
                    .append("verified_success=").append(result.get("success"));

         if (result.get("donHangId") != null) {
             redirectUrl.append("&verified_donHangId=").append(result.get("donHangId"));
         }
         if (result.get("soDonHang") != null) {
             redirectUrl.append("&verified_soDonHang=")
                        .append(URLEncoder.encode(String.valueOf(result.get("soDonHang")), StandardCharsets.UTF_8));
         }
         if (result.get("message") != null) {
             redirectUrl.append("&verified_message=")
                        .append(URLEncoder.encode(String.valueOf(result.get("message")), StandardCharsets.UTF_8));
         }

         RedirectView redirectView = new RedirectView(redirectUrl.toString());
         redirectView.setStatusCode(org.springframework.http.HttpStatus.FOUND);
         return redirectView;
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
