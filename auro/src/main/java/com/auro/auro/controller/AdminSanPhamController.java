package com.auro.auro.controller;

import com.auro.auro.model.SanPham;
import com.auro.auro.service.SanPhamService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/san-pham")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AdminSanPhamController {

    private final SanPhamService sanPhamService;

    @PostMapping
    public ResponseEntity<SanPham> create(@RequestBody SanPham payload) {
        SanPham created = sanPhamService.createSanPham(payload);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SanPham> update(@PathVariable Long id, @RequestBody SanPham payload) {
        return sanPhamService.updateSanPham(id, payload)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        boolean ok = sanPhamService.deleteSanPham(id);
        return ok ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}