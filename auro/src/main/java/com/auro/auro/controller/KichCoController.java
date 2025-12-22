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
     */
    @GetMapping("/test")
    public ResponseEntity<?> testEndpoint() {
        return ResponseEntity.ok("KichCo Controller is working!");
    }

    /**
     * Lấy tất cả kích cỡ, sắp xếp theo thứ tự
     */
    @GetMapping
    public ResponseEntity<?> getAllKichCo() {
        try {
            List<KichCo> kichCos = kichCoRepository.findAll();

            // Sắp xếp theo thuTu
            List<KichCo> sortedList = kichCos.stream()
                    .sorted((a, b) -> {
                        Integer thuTuA = a.getThuTu() != null ? a.getThuTu() : Integer.MAX_VALUE;
                        Integer thuTuB = b.getThuTu() != null ? b.getThuTu() : Integer.MAX_VALUE;
                        return thuTuA.compareTo(thuTuB);
                    })
                    .collect(Collectors.toList());
            return ResponseEntity.ok(sortedList);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Lỗi khi lấy danh sách kích cỡ: " + e.getMessage());
        }
    }

    /**
     * Lấy chỉ tên kích cỡ (dùng cho filter)
     */
    @GetMapping("/ten")
    public ResponseEntity<?> getAllKichCoNames() {
        try {
            List<KichCo> kichCos = kichCoRepository.findAll();

            // Sắp xếp và lấy chỉ tên
            List<String> names = kichCos.stream()
                    .sorted((a, b) -> {
                        Integer thuTuA = a.getThuTu() != null ? a.getThuTu() : Integer.MAX_VALUE;
                        Integer thuTuB = b.getThuTu() != null ? b.getThuTu() : Integer.MAX_VALUE;
                        return thuTuA.compareTo(thuTuB);
                    })
                    .map(KichCo::getTen)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(names);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Lỗi khi lấy danh sách tên kích cỡ: " + e.getMessage());
        }
    }

    /**
     * Lấy kích cỡ theo ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getKichCoById(@PathVariable Long id) {
        return kichCoRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Thêm kích cỡ mới
     */
    @PostMapping
    public ResponseEntity<?> createKichCo(@RequestBody KichCo kichCo) {
        try {
            // Kiểm tra xem kích cỡ đã tồn tại chưa
            if (kichCo.getTen() != null && !kichCo.getTen().trim().isEmpty()) {
                if (kichCoRepository.existsByTen(kichCo.getTen().trim())) {
                    return ResponseEntity.badRequest()
                            .body(java.util.Map.of("message", "Kích cỡ đã tồn tại", "error", "Size already exists"));
                }
            } else {
                return ResponseEntity.badRequest()
                        .body(java.util.Map.of("message", "Tên kích cỡ không được để trống", "error",
                                "Size name is required"));
            }

            // Tạo mới kích cỡ
            KichCo newKichCo = new KichCo();
            newKichCo.setTen(kichCo.getTen().trim());
            newKichCo.setThuTu(kichCo.getThuTu() != null ? kichCo.getThuTu() : 0);
            KichCo saved = kichCoRepository.save(newKichCo);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(java.util.Map.of("message", "Lỗi khi tạo kích cỡ: " + e.getMessage(), "error",
                            e.getMessage()));
        }
    }

    /**
     * Cập nhật kích cỡ
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateKichCo(@PathVariable Long id, @RequestBody KichCo kichCo) {
        try {
            return kichCoRepository.findById(id)
                    .map(existing -> {
                        // Kiểm tra tên mới có trùng với kích cỡ khác không
                        if (kichCo.getTen() != null && !kichCo.getTen().trim().isEmpty()) {
                            if (!existing.getTen().equals(kichCo.getTen().trim())) {
                                if (kichCoRepository.existsByTen(kichCo.getTen().trim())) {
                                    return ResponseEntity.badRequest()
                                            .body(java.util.Map.of("message", "Kích cỡ đã tồn tại", "error",
                                                    "Size already exists"));
                                }
                            }
                            existing.setTen(kichCo.getTen().trim());
                        } else {
                            return ResponseEntity.badRequest()
                                    .body(java.util.Map.of("message", "Tên kích cỡ không được để trống", "error",
                                            "Size name is required"));
                        }

                        // Cập nhật thứ tự
                        if (kichCo.getThuTu() != null) {
                            existing.setThuTu(kichCo.getThuTu());
                        }

                        KichCo updated = kichCoRepository.save(existing);
                        return ResponseEntity.ok(updated);
                    })
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(java.util.Map.of("message", "Lỗi khi cập nhật kích cỡ: " + e.getMessage(), "error",
                            e.getMessage()));
        }
    }

    /**
     * Xóa kích cỡ
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteKichCo(@PathVariable Long id) {
        try {
            if (!kichCoRepository.existsById(id)) {
                return ResponseEntity.notFound().build();
            }

            kichCoRepository.deleteById(id);
            return ResponseEntity.ok(java.util.Map.of("message", "Đã xóa kích cỡ thành công"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(java.util.Map.of("message", "Lỗi khi xóa kích cỡ: " + e.getMessage(), "error",
                            e.getMessage()));
        }
    }
}
