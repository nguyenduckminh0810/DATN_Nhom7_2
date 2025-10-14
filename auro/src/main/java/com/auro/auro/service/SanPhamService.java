package com.auro.auro.service;

import com.auro.auro.dto.request.SanPhamRequest;
import com.auro.auro.dto.response.SanPhamResponse;
import com.auro.auro.exception.BadRequestException;
import com.auro.auro.exception.ResourceNotFoundException;
import com.auro.auro.model.DanhMuc;
import com.auro.auro.model.SanPham;
import com.auro.auro.repository.DanhMucRepository;
import com.auro.auro.repository.SanPhamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SanPhamService {

  private final SanPhamRepository sanPhamRepository;
  private final DanhMucRepository danhMucRepository;

  // tìm theo slug
  public Page<SanPhamResponse> getBySlug(String slug, Pageable pageable) {
    Page<SanPham> page = sanPhamRepository.findBySlugStartsWith(slug, pageable);
    return page.map(this::mapToResponse);
  }

  public Page<SanPhamResponse> getPageByCategorySlug(String slug, String search, Pageable pageable) {
    DanhMuc danhMuc = danhMucRepository.findBySlug(slug)
        .orElseThrow(() -> new ResourceNotFoundException("Danh mục không tồn tại: " + slug));
    Long danhMucId = danhMuc.getId();
    return getPage(search, danhMucId, pageable);
  }

  public ResponseEntity<Page<SanPhamResponse>> getByProductSlug(String slug, Pageable pageable) {
    Page<SanPham> sp = sanPhamRepository.findByDanhMuc_Slug(slug, pageable);
    return ResponseEntity.ok(sp.map(this::mapToResponse));
  }

  public Page<SanPhamResponse> getPage(String search, Long danhMucId, Pageable pageable) {
    Page<SanPham> page;

    if (search != null && !search.trim().isEmpty() && danhMucId != null) {
      page = sanPhamRepository.findByDanhMuc_IdAndTenContainingIgnoreCaseOrDanhMuc_IdAndMoTaContainingIgnoreCase(
          danhMucId, search, danhMucId, search, pageable);
    } else if (search != null && !search.trim().isEmpty()) {
      page = sanPhamRepository.findByTenContainingIgnoreCaseOrMoTaContainingIgnoreCase(search, search, pageable);
    } else if (danhMucId != null) {
      page = sanPhamRepository.findByDanhMuc_Id(danhMucId, pageable);
    } else {
      page = sanPhamRepository.findAll(pageable);
    }

    return page.map(this::mapToResponse);
  }

  public SanPhamResponse getById(Long id) {
    SanPham sp = sanPhamRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Sản phẩm không tồn tại: " + id));
    return mapToResponse(sp);
  }

  public SanPhamResponse create(SanPhamRequest req) {
    if (req.getDanhMucId() == null) {
      throw new BadRequestException("Danh mục là bắt buộc");
    }
    if (req.getTen() == null || req.getTen().trim().isEmpty()) {
      throw new BadRequestException("Tên sản phẩm là bắt buộc");
    }
    if (req.getGia() == null) {
      throw new BadRequestException("Giá là bắt buộc");
    }
    DanhMuc dm = danhMucRepository.findById(req.getDanhMucId())
        .orElseThrow(() -> new ResourceNotFoundException("Danh mục không tồn tại: " + req.getDanhMucId()));

    SanPham sp = new SanPham();
    sp.setTen(req.getTen());
    sp.setMoTa(req.getMoTa());
    sp.setDanhMuc(dm);
    sp.setGia(req.getGia());
    // generate slug from provided slug or name
    sp.setSlug(generateUniqueSlug(StringUtils.hasText(req.getSlug()) ? req.getSlug() : req.getTen(), null));
    sp.setTrangThai(req.getTrangThai() != null ? req.getTrangThai() : "active");
    sp.setTaoLuc(LocalDateTime.now());
    sp.setCapNhatLuc(LocalDateTime.now());

    SanPham saved = sanPhamRepository.save(sp);
    return mapToResponse(saved);
  }

  public SanPhamResponse update(Long id, SanPhamRequest req) {
    SanPham sp = sanPhamRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Sản phẩm không tồn tại: " + id));

    if (req.getTen() != null)
      sp.setTen(req.getTen());
    if (req.getMoTa() != null)
      sp.setMoTa(req.getMoTa());
    if (req.getGia() != null)
      sp.setGia(req.getGia());
    if (req.getDanhMucId() != null) {
      DanhMuc dm = danhMucRepository.findById(req.getDanhMucId())
          .orElseThrow(() -> new ResourceNotFoundException("Danh mục không tồn tại: " + req.getDanhMucId()));
      sp.setDanhMuc(dm);
    }
    if (req.getTrangThai() != null)
      sp.setTrangThai(req.getTrangThai());

    // slug: if provided use it, else regenerate from current name
    if (StringUtils.hasText(req.getSlug())) {
      sp.setSlug(generateUniqueSlug(req.getSlug(), id));
    } else {
      sp.setSlug(generateUniqueSlug(sp.getTen(), id));
    }
    sp.setCapNhatLuc(LocalDateTime.now());
    SanPham saved = sanPhamRepository.save(sp);
    return mapToResponse(saved);
  }

  public void delete(Long id) {
    SanPham sp = sanPhamRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Sản phẩm không tồn tại: " + id));
    sanPhamRepository.delete(sp);
  }

  private SanPhamResponse mapToResponse(SanPham sp) {
    SanPhamResponse res = new SanPhamResponse();
    res.setId(sp.getId());
    res.setTen(sp.getTen());
    res.setSlug(sp.getSlug());
    res.setMoTa(sp.getMoTa());
    if (sp.getDanhMuc() != null) {
      res.setDanhMucId(sp.getDanhMuc().getId());
      res.setDanhMucTen(sp.getDanhMuc().getTen());
    }
    res.setGia(sp.getGia());
    res.setTrangThai(sp.getTrangThai());
    res.setTaoLuc(sp.getTaoLuc());
    res.setCapNhatLuc(sp.getCapNhatLuc());
    return res;
  }

  private String generateUniqueSlug(String base, Long currentId) {
    String normalized = normalizeToSlug(base);
    String candidate = normalized;
    int suffix = 1;
    boolean exists = currentId == null
        ? sanPhamRepository.existsBySlug(candidate)
        : sanPhamRepository.existsBySlugAndIdNot(candidate, currentId);
    while (exists) {
      candidate = normalized + "-" + (++suffix);
      exists = currentId == null
          ? sanPhamRepository.existsBySlug(candidate)
          : sanPhamRepository.existsBySlugAndIdNot(candidate, currentId);
    }
    return candidate;
  }

  private String normalizeToSlug(String input) {
    if (!StringUtils.hasText(input))
      return "san-pham";
    String s = input.trim().toLowerCase();
    // Replace Vietnamese accents and special chars
    s = java.text.Normalizer.normalize(s, java.text.Normalizer.Form.NFD)
        .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
    s = s.replaceAll("[^a-z0-9\\s-]", "");
    s = s.replaceAll("[\\s-]+", "-").replaceAll("^-+|-+$", "");
    return s.isEmpty() ? "san-pham" : s;
  }

}
