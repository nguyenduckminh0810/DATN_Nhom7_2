package com.auro.auro.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/phieu-giam-gia")
@CrossOrigin(origins = "*")
public class VoucherController {

    @GetMapping("/co-san")
    public ResponseEntity<List<Map<String, Object>>> getCoSan() {
        // Mock data cho voucher có sẵn
        List<Map<String, Object>> vouchers = Arrays.asList(
            Map.of(
                "id", 1L,
                "ten", "Giảm 10% cho đơn hàng đầu tiên",
                "ma", "WELCOME10",
                "giaTri", 10,
                "loai", "PHAN_TRAM",
                "trangThai", "HOAT_DONG",
                "ngayBatDau", "2025-01-01",
                "ngayKetThuc", "2025-12-31"
            ),
            Map.of(
                "id", 2L,
                "ten", "Giảm 50k cho đơn hàng từ 500k",
                "ma", "SAVE50K",
                "giaTri", 50000,
                "loai", "SO_TIEN",
                "trangThai", "HOAT_DONG",
                "ngayBatDau", "2025-01-01",
                "ngayKetThuc", "2025-12-31"
            )
        );
        
        return ResponseEntity.ok(vouchers);
    }

    @PostMapping("/ap-dung")
    public ResponseEntity<Map<String, Object>> apDung(@RequestBody Map<String, String> request) {
        String maVoucher = request.get("maVoucher");
        
        // Mock logic kiểm tra voucher
        if ("WELCOME10".equals(maVoucher)) {
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Áp dụng voucher thành công",
                "giamGia", 10,
                "loai", "PHAN_TRAM"
            ));
        } else if ("SAVE50K".equals(maVoucher)) {
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Áp dụng voucher thành công",
                "giamGia", 50000,
                "loai", "SO_TIEN"
            ));
        } else {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Mã voucher không hợp lệ hoặc đã hết hạn"
            ));
        }
    }

    @PostMapping("/kiem-tra")
    public ResponseEntity<Map<String, Object>> kiemTra(@RequestBody Map<String, String> request) {
        String maVoucher = request.get("maVoucher");
        
        // Mock logic kiểm tra voucher
        if ("WELCOME10".equals(maVoucher)) {
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Voucher hợp lệ",
                "giamGia", 10,
                "loai", "PHAN_TRAM",
                "moTa", "Giảm 10% cho đơn hàng đầu tiên"
            ));
        } else if ("SAVE50K".equals(maVoucher)) {
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Voucher hợp lệ",
                "giamGia", 50000,
                "loai", "SO_TIEN",
                "moTa", "Giảm 50k cho đơn hàng từ 500k"
            ));
        } else {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Mã voucher không hợp lệ hoặc đã hết hạn"
            ));
        }
    }
}