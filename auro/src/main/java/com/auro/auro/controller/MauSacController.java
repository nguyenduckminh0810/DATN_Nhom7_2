package com.auro.auro.controller;

import com.auro.auro.model.MauSac;
import com.auro.auro.repository.MauSacRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mau-sac")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class MauSacController {

    private final MauSacRepository mauSacRepository;

    /**
     * Lấy tất cả màu sắc
     */
    @GetMapping
    public ResponseEntity<List<MauSac>> getAllColors() {
        List<MauSac> colors = mauSacRepository.findAll();
        return ResponseEntity.ok(colors);
    }

    /**
     * Thêm màu mới
     */
    @PostMapping
    public ResponseEntity<MauSac> createColor(@RequestBody MauSac mauSac) {
        // Kiểm tra xem màu đã tồn tại chưa (theo mã hex)
        if (mauSac.getMa() != null && !mauSac.getMa().isEmpty()) {
            var existing = mauSacRepository.findByMa(mauSac.getMa());
            if (existing.isPresent()) {
                return ResponseEntity.ok(existing.get()); // Trả về màu đã có
            }
        }

        // Nếu chưa có, tạo mới
        MauSac saved = mauSacRepository.save(mauSac);
        return ResponseEntity.ok(saved);
    }

    /**
     * Cập nhật màu
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateColor(@PathVariable Long id, @RequestBody MauSac mauSac) {
        return mauSacRepository.findById(id)
                .map(existing -> {
                    // Kiểm tra tên mới có trùng với màu khác không
                    if (mauSac.getTen() != null && !mauSac.getTen().trim().isEmpty()) {
                        if (!existing.getTen().equals(mauSac.getTen().trim())) {
                            if (mauSacRepository.existsByTen(mauSac.getTen().trim())) {
                                return ResponseEntity.badRequest()
                                        .body(java.util.Map.of("message", "Màu sắc đã tồn tại", "error", "Color already exists"));
                            }
                        }
                        existing.setTen(mauSac.getTen().trim());
                    }
                    
                    // Cập nhật mã màu nếu có
                    if (mauSac.getMa() != null) {
                        // Kiểm tra mã mới có trùng với màu khác không
                        if (!existing.getMa().equals(mauSac.getMa())) {
                            var existingByMa = mauSacRepository.findByMa(mauSac.getMa());
                            if (existingByMa.isPresent() && !existingByMa.get().getId().equals(id)) {
                                return ResponseEntity.badRequest()
                                        .body(java.util.Map.of("message", "Mã màu đã tồn tại", "error", "Color code already exists"));
                            }
                        }
                        existing.setMa(mauSac.getMa());
                    }
                    
                    MauSac updated = mauSacRepository.save(existing);
                    return ResponseEntity.ok(updated);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Xóa màu
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteColor(@PathVariable Long id) {
        mauSacRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
