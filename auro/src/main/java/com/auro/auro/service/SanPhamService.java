package com.auro.auro.service;

import com.auro.auro.model.SanPham;
import com.auro.auro.repository.SanPhamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SanPhamService {

    private final SanPhamRepository sanPhamRepository;

    public Page<SanPham> getAllSanPham(int page, int size, String search, Long danhMucId) {
        Pageable pageable = PageRequest.of(page, size);
        
        if (search != null && !search.isEmpty()) {
            if (danhMucId != null) {
                return sanPhamRepository.findByTenContainingIgnoreCaseAndDanhMucId(search, danhMucId, pageable);
            } else {
                return sanPhamRepository.findByTenContainingIgnoreCase(search, pageable);
            }
        } else if (danhMucId != null) {
            return sanPhamRepository.findByDanhMucId(danhMucId, pageable);
        } else {
            return sanPhamRepository.findAll(pageable);
        }
    }

    public Optional<SanPham> getSanPhamById(Long id) {
        return sanPhamRepository.findById(id);
    }

    public Optional<SanPham> getSanPhamBySlug(String slug) {
        return sanPhamRepository.findBySlug(slug);
    }

    public List<SanPham> getFeaturedSanPham() {
        // Lấy 8 sản phẩm nổi bật (có thể thay đổi logic này)
        return sanPhamRepository.findTop8ByTrangThaiOrderByTaoLucDesc("active");
    }

    public List<SanPham> getSanPhamByDanhMuc(Long danhMucId) {
        return sanPhamRepository.findByDanhMucIdAndTrangThai(danhMucId, "active");
    }

    public SanPham createSanPham(SanPham sanPham) {
        // Tự động tạo slug nếu chưa có
        if (sanPham.getSlug() == null || sanPham.getSlug().isEmpty()) {
            sanPham.setSlug(generateSlug(sanPham.getTen()));
        }
        
        return sanPhamRepository.save(sanPham);
    }

    public Optional<SanPham> updateSanPham(Long id, SanPham sanPham) {
        return sanPhamRepository.findById(id)
                .map(existingSanPham -> {
                    existingSanPham.setTen(sanPham.getTen());
                    existingSanPham.setSlug(sanPham.getSlug());
                    existingSanPham.setThuongHieu(sanPham.getThuongHieu());
                    existingSanPham.setMoTa(sanPham.getMoTa());
                    existingSanPham.setHuongDanBaoQuan(sanPham.getHuongDanBaoQuan());
                    existingSanPham.setDanhMuc(sanPham.getDanhMuc());
                    existingSanPham.setTrangThai(sanPham.getTrangThai());
                    existingSanPham.setCapNhatLuc(java.time.LocalDateTime.now());
                    
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
        return ten.toLowerCase()
                .replaceAll("[^a-z0-9\\s]", "")
                .replaceAll("\\s+", "-")
                .trim();
    }
}
