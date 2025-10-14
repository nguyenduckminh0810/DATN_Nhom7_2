package com.auro.auro.service;

import com.auro.auro.model.SanPham;
import com.auro.auro.repository.SanPhamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DanhMucService {

    private final SanPhamRepository sanPhamRepository;

    // ví dụ: sortBy = "taoLuc", direction = "DESC"
    public Page<SanPham> findSanPhamBySlug(String slug,
            int page,
            int size,
            String sortBy,
            String direction) {
        Sort sort = Sort.by(
                "DESC".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC,
                (sortBy == null || sortBy.isBlank()) ? "id" : sortBy);
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.max(size, 1), sort);
        return sanPhamRepository.findByDanhMuc_Slug(slug, pageable);
        // nếu dùng method có trạng thái:
        // return sanPhamRepository.findByDanhMuc_SlugAndTrangThai(slug, "active",
        // pageable);
    }
}
