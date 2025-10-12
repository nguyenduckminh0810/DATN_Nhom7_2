package com.auro.auro.controller;

import com.auro.auro.dto.request.SanPhamRequest;
import com.auro.auro.dto.response.SanPhamResponse;
import com.auro.auro.service.SanPhamService;
import org.springframework.beans.factory.annotation.Autowired;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/san-pham")
@CrossOrigin(origins = "*")
public class SanPhamController {

    @Autowired
    private SanPhamService sanPhamService;

    @GetMapping
    public ResponseEntity<Page<SanPhamResponse>> page(
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "danhMucId", required = false) Long danhMucId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        Page<SanPhamResponse> p = sanPhamService.getPage(search, danhMucId, PageRequest.of(page, size));
        return ResponseEntity.ok(p);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SanPhamResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(sanPhamService.getById(id));
    }

    @PostMapping
    public ResponseEntity<SanPhamResponse> create(@Valid @RequestBody SanPhamRequest req) {
        SanPhamResponse res = sanPhamService.create(req);
        return ResponseEntity.status(201).body(res);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SanPhamResponse> update(@PathVariable Long id, @Valid @RequestBody SanPhamRequest req) {
        return ResponseEntity.ok(sanPhamService.update(id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        sanPhamService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
