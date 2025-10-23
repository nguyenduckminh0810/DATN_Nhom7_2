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
     * Xóa màu
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteColor(@PathVariable Long id) {
        mauSacRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
