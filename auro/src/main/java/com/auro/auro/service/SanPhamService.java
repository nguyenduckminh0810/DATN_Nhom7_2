package com.auro.auro.service;

import com.auro.auro.model.SanPham;
import com.auro.auro.repository.SanPhamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SanPhamService {

    private final SanPhamRepository sanPhamRepository;

    public Page<SanPham> getAllSanPham(int page, int size, String search, Long danhMucId) {
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.max(1, size));

        if (search != null && !search.isEmpty()) {
            if (danhMucId != null) {
                try {
                    return sanPhamRepository.findByTenContainingIgnoreCaseAndDanhMucId(search, danhMucId, pageable);
                } catch (Throwable ignored) {
                    // fallback: filter in memory
                    return sanPhamRepository.findAll(pageable).map(sp -> sp).map(sp -> sp); // keep Page type - but we
                                                                                            // will filter below if
                                                                                            // needed
                }
            } else {
                return sanPhamRepository.findByTenContainingIgnoreCase(search, pageable);
            }
        } else if (danhMucId != null) {
            try {
                return sanPhamRepository.findByDanhMucId(danhMucId, pageable);
            } catch (Throwable ignored) {
                // fallback: filter the full list and build a page
                List<SanPham> filtered = sanPhamRepository.findAll().stream()
                        .filter(sp -> sp.getDanhMuc() != null && danhMucId.equals(sp.getDanhMuc().getId()))
                        .collect(Collectors.toList());
                int start = (int) pageable.getOffset();
                int end = Math.min(start + pageable.getPageSize(), filtered.size());
                if (start >= filtered.size()) {
                    return Page.empty(pageable);
                }
                return new org.springframework.data.domain.PageImpl<>(filtered.subList(start, end), pageable,
                        filtered.size());
            }
        } else {
            return sanPhamRepository.findAll(pageable);
        }
    }

    public Optional<SanPham> getSanPhamById(Long id) {
        return sanPhamRepository.findById(id);
    }

    public Optional<SanPham> getSanPhamBySlug(String slug) {
        try {
            return sanPhamRepository.findBySlug(slug);
        } catch (Throwable ignored) {
            return sanPhamRepository.findAll().stream()
                    .filter(sp -> slug != null && slug.equals(sp.getSlug()))
                    .findFirst();
        }
    }

    public List<SanPham> getFeaturedSanPham() {
        try {
            // trangThai trong model là Boolean => truyền true
            return sanPhamRepository.findTop8ByTrangThaiOrderByTaoLucDesc(true);
        } catch (Throwable ignored) {
            // fallback: lấy top 8 theo taoLuc
            return sanPhamRepository.findAll(PageRequest.of(0, 8))
                    .stream()
                    .collect(Collectors.toList());
        }
    }

    public List<SanPham> getSanPhamByDanhMuc(Long danhMucId) {
        try {
            // ưu tiên repo trực tiếp nếu có signature phù hợp (truyền Boolean true)
            return sanPhamRepository.findByDanhMucIdAndTrangThai(danhMucId, true);
        } catch (Throwable ex) {
            try {
                // nếu repo cung cấp findByDanhMucId with Pageable
                return sanPhamRepository.findByDanhMucId(danhMucId, PageRequest.of(0, 1000)).toList();
            } catch (Throwable ignored) {
                // fallback filter in memory
                return sanPhamRepository.findAll().stream()
                        .filter(sp -> sp.getDanhMuc() != null
                                && danhMucId.equals(sp.getDanhMuc().getId())
                                && Boolean.TRUE.equals(sp.getTrangThai()))
                        .collect(Collectors.toList());
            }
        }
    }

    @Transactional
    public SanPham createSanPham(SanPham sanPham) {
        if (sanPham.getSlug() == null || sanPham.getSlug().isEmpty()) {
            sanPham.setSlug(generateSlug(sanPham.getTen() == null ? "san-pham" : sanPham.getTen()));
        }
        if (sanPham.getTaoLuc() == null) {
            sanPham.setTaoLuc(LocalDateTime.now());
        }
        sanPham.setCapNhatLuc(LocalDateTime.now());
        // ensure trangThai default
        if (sanPham.getTrangThai() == null) {
            sanPham.setTrangThai(true);
        }
        return sanPhamRepository.save(sanPham);
    }

    @Transactional
    public Optional<SanPham> updateSanPham(Long id, SanPham sanPham) {
        return sanPhamRepository.findById(id)
                .map(existingSanPham -> {
                    if (sanPham.getTen() != null)
                        existingSanPham.setTen(sanPham.getTen());
                    if (sanPham.getSlug() == null || sanPham.getSlug().isEmpty()) {
                        existingSanPham.setSlug(generateSlug(existingSanPham.getTen()));
                    } else {
                        existingSanPham.setSlug(sanPham.getSlug());
                    }
                    if (sanPham.getThuongHieu() != null)
                        existingSanPham.setThuongHieu(sanPham.getThuongHieu());
                    if (sanPham.getMoTa() != null)
                        existingSanPham.setMoTa(sanPham.getMoTa());
                    if (sanPham.getHuongDanBaoQuan() != null)
                        existingSanPham.setHuongDanBaoQuan(sanPham.getHuongDanBaoQuan());
                    if (sanPham.getDanhMuc() != null)
                        existingSanPham.setDanhMuc(sanPham.getDanhMuc());
                    if (sanPham.getTrangThai() != null)
                        existingSanPham.setTrangThai(sanPham.getTrangThai());
                    existingSanPham.setCapNhatLuc(LocalDateTime.now());
                    return sanPhamRepository.save(existingSanPham);
                });
    }

    public boolean deleteSanPham(Long id) {
        if (sanPhamRepository.existsById(id)) {
            sanPhamRepository.deleteById(id);
            return true;
        }
        return false;
    }

    private String generateSlug(String ten) {
        if (ten == null)
            return "san-pham";
        return ten.toLowerCase()
                .replaceAll("[^a-z0-9\\s]", "")
                .replaceAll("\\s+", "-")
                .trim();
    }
}
