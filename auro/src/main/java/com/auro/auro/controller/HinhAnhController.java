package com.auro.auro.controller;

import com.auro.auro.model.HinhAnh;
import com.auro.auro.model.SanPham;
import com.auro.auro.repository.HinhAnhRepository;
import com.auro.auro.repository.SanPhamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/hinh-anh")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class HinhAnhController {

    private final HinhAnhRepository hinhAnhRepository;
    private final SanPhamRepository sanPhamRepository;

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    @GetMapping("/san-pham/{id}")
    public ResponseEntity<List<HinhAnh>> listBySanPham(@PathVariable("id") Long idSanPham) {
        List<HinhAnh> list = hinhAnhRepository.findBySanPham_IdOrderByThuTuAscIdAsc(idSanPham);
        return ResponseEntity.ok(list);
    }

    @PreAuthorize("hasAnyRole('STF','ADM')")
    @PostMapping(value = "/san-pham/{id}", consumes = { "multipart/form-data" })
    public ResponseEntity<?> uploadForSanPham(
            @PathVariable("id") Long idSanPham,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "laDaiDien", required = false) Boolean laDaiDien,
            @RequestParam(value = "thuTu", required = false) Integer thuTu) {
        try {
            if (file == null || file.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("File rỗng hoặc không hợp lệ");
            }

            SanPham sp = sanPhamRepository.findById(idSanPham)
                    .orElseThrow(() -> new IllegalArgumentException("Sản phẩm không tồn tại: " + idSanPham));

            // Ensure upload directory exists
            Path dir = Paths.get(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(dir);

            String original = file.getOriginalFilename();
            if (original == null)
                original = "";
            String ext = "";
            int dot = original.lastIndexOf('.');
            if (dot > -1)
                ext = original.substring(dot);
            String filename = UUID.randomUUID() + ext;
            Path target = dir.resolve(filename);
            Files.copy(file.getInputStream(), target, java.nio.file.StandardCopyOption.REPLACE_EXISTING);

            // Public URL via static-path-pattern /files/**
            String url = "/files/" + filename;

            HinhAnh ha = new HinhAnh();
            ha.setSanPham(sp);
            ha.setUrl(url);
            ha.setLaDaiDien(Boolean.TRUE.equals(laDaiDien));
            ha.setThuTu(thuTu);
            ha.setTaoLuc(LocalDateTime.now());

            HinhAnh saved = hinhAnhRepository.save(ha);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi upload: " + ex.getClass().getSimpleName() + ": " + ex.getMessage());
        }
    }

    @PreAuthorize("hasAnyRole('STF','ADM')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) throws IOException {
        var opt = hinhAnhRepository.findById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        HinhAnh ha = opt.get();
        // Try to delete file on disk (best-effort)
        try {
            if (ha.getUrl() != null) {
                String p = ha.getUrl();
                if (p.startsWith("/"))
                    p = p.substring(1);
                // Remove 'files/' prefix if present to get actual filename
                if (p.startsWith("files/"))
                    p = p.substring("files/".length());
                Path path = Paths.get(uploadDir).resolve(p).toAbsolutePath().normalize();
                Files.deleteIfExists(path);
            }
        } catch (Exception ignored) {
        }
        hinhAnhRepository.delete(ha);
        return ResponseEntity.noContent().build();
    }
}
