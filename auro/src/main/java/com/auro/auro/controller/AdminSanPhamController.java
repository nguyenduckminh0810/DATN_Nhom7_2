package com.auro.auro.controller;

import com.auro.auro.model.SanPham;
import com.auro.auro.service.SanPhamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin/products")
// TODO: thêm @PreAuthorize khi tích hợp auth
public class AdminSanPhamController {

    @Autowired
    private SanPhamService sanPhamService;

    @GetMapping
    public ResponseEntity<List<SanPham>> listAll() {
        return ResponseEntity.ok(sanPhamService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getOne(@PathVariable Long id) {
        Optional<SanPham> sp = sanPhamService.findById(id);
        return sp.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody SanPham sanPham) {
        SanPham saved = sanPhamService.save(sanPham);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody SanPham payload) {
        Optional<SanPham> opt = sanPhamService.findById(id);
        if (opt.isEmpty())
            return ResponseEntity.notFound().build();
        SanPham sp = opt.get();
        // cập nhật những field cơ bản — chỉnh theo model của bạn
        sp.setTen(payload.getTen());
        sp.setMoTa(payload.getMoTa());
        sp.setGia(payload.getGia());
        sp.setDanhMuc(payload.getDanhMuc());
        SanPham updated = sanPhamService.save(sp);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        sanPhamService.deleteById(id);
        return ResponseEntity.ok().build();
    }
}