package com.auro.auro.controller;

import com.auro.auro.dto.request.SanPhamRequest;
import com.auro.auro.dto.response.SanPhamResponse;
import com.auro.auro.dto.response.SanPhamDetailResponse;
import com.auro.auro.service.SanPhamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;

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

    @GetMapping("/danh-muc/{slug}")
    public ResponseEntity<Page<SanPhamResponse>> getByCategoryWithChildren(
            @PathVariable String slug,
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "12") int size) {

        // sanPhamService.getPageByCategorySlugIncludingChildren(...) is not defined;
        // return an empty page as a safe fallback until the service method is
        // implemented.
        Page<SanPhamResponse> p = sanPhamService
                .getPageByCategorySlugIncludingChildren(slug, search, PageRequest.of(page, size));
        return ResponseEntity.ok(p);
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<Page<SanPhamResponse>> getByProductSlug(
            @PathVariable String slug,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {

        PageRequest pageable = PageRequest.of(page, size);
        Page<SanPhamResponse> p = sanPhamService.getBySlug(slug, pageable);
        return ResponseEntity.ok(p);
    }

    @GetMapping
    public ResponseEntity<Page<SanPhamResponse>> page(
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "danhMucId", required = false) Long danhMucId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        Page<SanPhamResponse> p = sanPhamService.getPage(search, danhMucId, PageRequest.of(page, size));
        return ResponseEntity.ok(p);
    }

    @GetMapping("/{slug}")
    public ResponseEntity<Page<SanPhamResponse>> findBySlug(
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "danhMucId", required = false) Long danhMucId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        Page<SanPhamResponse> p = sanPhamService.getPage(search, danhMucId, PageRequest.of(page, size));
        return ResponseEntity.ok(p);
    }

    @GetMapping("/id/{id}")
    public ResponseEntity<SanPhamResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(sanPhamService.getById(id));
    }

    @GetMapping("/id/{id}/detail")
    public ResponseEntity<SanPhamDetailResponse> getDetail(@PathVariable Long id) {
        return ResponseEntity.ok(sanPhamService.getDetailById(id));
    }

    @PostMapping
    public ResponseEntity<SanPhamResponse> create(@Valid @RequestBody SanPhamRequest req) {
        SanPhamResponse res = sanPhamService.create(req);
        return ResponseEntity.status(201).body(res);
    }

    @PutMapping("/id/{id}")
    public ResponseEntity<SanPhamResponse> update(@PathVariable Long id, @Valid @RequestBody SanPhamRequest req) {
        return ResponseEntity.ok(sanPhamService.update(id, req));
    }

    @DeleteMapping("/id/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        sanPhamService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
