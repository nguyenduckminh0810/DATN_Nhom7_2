package com.auro.auro.controller;

import com.auro.auro.model.KichCo;
import com.auro.auro.repository.KichCoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/kich-co")
public class KichCoController {

    @Autowired
    private KichCoRepository kichCoRepository;

    /**
     * Test endpoint để kiểm tra controller hoạt động
     * GET /api/kich-co/test
     */
    @GetMapping("/test")
    public ResponseEntity<?> testEndpoint() {
        return ResponseEntity.ok("KichCo Controller is working!");
    }

    /**
     * Lấy tất cả kích cỡ, sắp xếp theo thứ tự
     * GET /api/kich-co
     */
    @GetMapping
    public ResponseEntity<?> getAllKichCo() {
        try {
            System.out.println("=== DEBUG: Đang gọi getAllKichCo() ===");
            List<KichCo> kichCos = kichCoRepository.findAll();
            System.out.println("=== DEBUG: Số lượng kích cỡ tìm được: " + kichCos.size() + " ===");

            // Sắp xếp theo thuTu
            List<KichCo> sortedList = kichCos.stream()
                    .sorted((a, b) -> {
                        Integer thuTuA = a.getThuTu() != null ? a.getThuTu() : Integer.MAX_VALUE;
                        Integer thuTuB = b.getThuTu() != null ? b.getThuTu() : Integer.MAX_VALUE;
                        return thuTuA.compareTo(thuTuB);
                    })
                    .collect(Collectors.toList());

            System.out.println("=== DEBUG: Đã sắp xếp xong ===");
            return ResponseEntity.ok(sortedList);
        } catch (Exception e) {
            System.err.println("=== ERROR: " + e.getMessage() + " ===");
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Lỗi khi lấy danh sách kích cỡ: " + e.getMessage());
        }
    }

    /**
     * Lấy chỉ tên kích cỡ (dùng cho filter)
     * GET /api/kich-co/ten
     */
    @GetMapping("/ten")
    public ResponseEntity<?> getAllKichCoNames() {
        try {
            System.out.println("=== DEBUG: Đang gọi getAllKichCoNames() ===");
            List<KichCo> kichCos = kichCoRepository.findAll();
            System.out.println("=== DEBUG: Số lượng kích cỡ tìm được: " + kichCos.size() + " ===");

            // Sắp xếp và lấy chỉ tên
            List<String> names = kichCos.stream()
                    .sorted((a, b) -> {
                        Integer thuTuA = a.getThuTu() != null ? a.getThuTu() : Integer.MAX_VALUE;
                        Integer thuTuB = b.getThuTu() != null ? b.getThuTu() : Integer.MAX_VALUE;
                        return thuTuA.compareTo(thuTuB);
                    })
                    .map(KichCo::getTen)
                    .collect(Collectors.toList());

            System.out.println("=== DEBUG: Danh sách tên: " + names + " ===");
            return ResponseEntity.ok(names);
        } catch (Exception e) {
            System.err.println("=== ERROR: " + e.getMessage() + " ===");
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Lỗi khi lấy danh sách tên kích cỡ: " + e.getMessage());
        }
    }
}
