package com.auro.auro.controller;

import com.auro.auro.model.MauSac;
import com.auro.auro.model.SanPham;
import com.auro.auro.repository.MauSacRepository;
import com.auro.auro.repository.SanPhamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/api/test")
public class TestController {

    private final MauSacRepository mauSacRepository;

    @Autowired
    public TestController(MauSacRepository mauSacRepository) {
        this.mauSacRepository = mauSacRepository;
    }

    // lấy tất cả màu sắc
    @GetMapping("/tat-ca")
    public ResponseEntity<Page<MauSac>> listProducts(
            @RequestParam(value = "q", required = false) String q,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.max(1, size));
        Page<MauSac> results;
        if (q == null || q.isBlank()) {
            results = mauSacRepository.findAll(pageable);
        } else {
            results = mauSacRepository.findAll(pageable);
        }
        return ResponseEntity.ok(results);
    }
}
