package com.auro.auro.controller;

import com.auro.auro.model.SanPham;
import com.auro.auro.service.SanPhamService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/san-pham")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SanPhamController {

    private final SanPhamService sanPhamService;

    @GetMapping
    public ResponseEntity<Page<SanPham>> getAllSanPham(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long danhMucId) {
        
        Page<SanPham> sanPhams = sanPhamService.getAllSanPham(page, size, search, danhMucId);
        return ResponseEntity.ok(sanPhams);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SanPham> getSanPhamById(@PathVariable Long id) {
        Optional<SanPham> sanPham = sanPhamService.getSanPhamById(id);
        return sanPham.map(ResponseEntity::ok)
                     .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<SanPham> getSanPhamBySlug(@PathVariable String slug) {
        Optional<SanPham> sanPham = sanPhamService.getSanPhamBySlug(slug);
        return sanPham.map(ResponseEntity::ok)
                     .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/featured")
    public ResponseEntity<List<SanPham>> getFeaturedSanPham() {
        List<SanPham> featuredProducts = sanPhamService.getFeaturedSanPham();
        return ResponseEntity.ok(featuredProducts);
    }

    @GetMapping("/category/{danhMucId}")
    public ResponseEntity<List<SanPham>> getSanPhamByDanhMuc(@PathVariable Long danhMucId) {
        List<SanPham> sanPhams = sanPhamService.getSanPhamByDanhMuc(danhMucId);
        return ResponseEntity.ok(sanPhams);
    }

    @PostMapping
    public ResponseEntity<SanPham> createSanPham(@RequestBody SanPham sanPham) {
        SanPham createdSanPham = sanPhamService.createSanPham(sanPham);
        return ResponseEntity.ok(createdSanPham);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SanPham> updateSanPham(@PathVariable Long id, @RequestBody SanPham sanPham) {
        Optional<SanPham> updatedSanPham = sanPhamService.updateSanPham(id, sanPham);
        return updatedSanPham.map(ResponseEntity::ok)
                            .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSanPham(@PathVariable Long id) {
        boolean deleted = sanPhamService.deleteSanPham(id);
        return deleted ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }
}
