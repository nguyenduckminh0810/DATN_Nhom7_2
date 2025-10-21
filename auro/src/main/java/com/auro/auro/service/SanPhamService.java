package com.auro.auro.service;

import com.auro.auro.dto.request.SanPhamRequest;
import com.auro.auro.dto.response.SanPhamResponse;
import com.auro.auro.dto.response.SanPhamDetailResponse;
import com.auro.auro.exception.BadRequestException;
import com.auro.auro.exception.ResourceNotFoundException;
import com.auro.auro.model.DanhMuc;
import com.auro.auro.model.SanPham;
import com.auro.auro.repository.BienTheSanPhamRepository;
import com.auro.auro.repository.DanhMucRepository;
import com.auro.auro.repository.SanPhamRepository;
import com.auro.auro.repository.HinhAnhRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SanPhamService {

  private final SanPhamRepository sanPhamRepository;
  private final DanhMucRepository danhMucRepository;
  private final HinhAnhRepository hinhAnhRepository;
  private final BienTheSanPhamRepository bienTheSanPhamRepository;

  public Page<SanPhamResponse> getPageByCategorySlugIncludingChildren(
      String slug, String search, Pageable pageable) {

    DanhMuc root = danhMucRepository.findBySlug(slug)
        .orElseThrow(() -> new ResourceNotFoundException("Danh mục không tồn tại: " + slug));

    // thu thập id cha + các con
    List<Long> ids = new ArrayList<>();
    ids.add(root.getId());
    collectDescendantIds(root.getId(), ids); // viết ở dưới

    Page<SanPham> page;
    if (StringUtils.hasText(search)) {
      // nếu muốn search FE/BE có thể thêm method _IdInAndTenContaining..., tạm thời
      // search toàn cục:
      page = sanPhamRepository.findByTenContainingIgnoreCaseOrMoTaContainingIgnoreCase(search, search, pageable);
    } else {
      page = sanPhamRepository.findByDanhMuc_IdIn(ids, pageable);
    }
    return page.map(this::mapToResponse);
  }

  private void collectDescendantIds(Long parentId, List<Long> ids) {
    List<DanhMuc> children = danhMucRepository.findByDanhMucCha_Id(parentId);
    if (children != null) {
      for (DanhMuc child : children) {
        ids.add(child.getId());
        collectDescendantIds(child.getId(), ids);
      }
    }
  }

  // tìm theo slug
  public Page<SanPhamResponse> getBySlug(String slug, Pageable pageable) {
    Page<SanPhamResponse> page = sanPhamRepository.findBySlugStartsWith(slug, pageable);
    return page;
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

  public SanPhamDetailResponse getDetailById(Long id) {
    SanPham sp = sanPhamRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Sản phẩm không tồn tại: " + id));
    return mapToDetailResponse(sp);
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

  @Transactional
  public void delete(Long id) {
    SanPham sp = sanPhamRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Sản phẩm không tồn tại: " + id));

    // 1. Lấy danh sách tất cả biến thể của sản phẩm
    List<com.auro.auro.model.BienTheSanPham> variants = bienTheSanPhamRepository.findBySanPham_Id(id);

    // 2. Xóa hình ảnh của từng biến thể
    for (com.auro.auro.model.BienTheSanPham variant : variants) {
      hinhAnhRepository.deleteByBienThe_Id(variant.getId());
    }

    // 3. Xóa tất cả biến thể của sản phẩm
    bienTheSanPhamRepository.deleteBySanPham_Id(id);

    // 4. Xóa hình ảnh của sản phẩm
    hinhAnhRepository.deleteBySanPham_Id(id);

    // 5. Cuối cùng xóa sản phẩm
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
    try {
      // set ảnh đại diện: ưu tiên laDaiDien=true, nếu không có lấy ảnh đầu tiên theo
      // thứ tự
      var list = hinhAnhRepository.findBySanPham_IdOrderByThuTuAscIdAsc(sp.getId());
      if (list != null && !list.isEmpty()) {
        String url = list.stream().filter(ha -> Boolean.TRUE.equals(ha.getLaDaiDien()))
            .map(com.auro.auro.model.HinhAnh::getUrl)
            .findFirst()
            .orElseGet(() -> list.get(0).getUrl());
        res.setAnhDaiDien(url);
      }
    } catch (Exception ignored) {
    }
    return res;
  }

  private SanPhamDetailResponse mapToDetailResponse(SanPham sp) {
    SanPhamDetailResponse res = new SanPhamDetailResponse();
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

    // Load images
    try {
      var hinhAnhList = hinhAnhRepository.findBySanPham_IdOrderByThuTuAscIdAsc(sp.getId());
      if (hinhAnhList != null && !hinhAnhList.isEmpty()) {
        List<SanPhamDetailResponse.HinhAnhInfo> hinhAnhs = hinhAnhList.stream()
            .map(ha -> new SanPhamDetailResponse.HinhAnhInfo(
                ha.getId(),
                ha.getUrl(),
                ha.getLaDaiDien(),
                ha.getThuTu()))
            .collect(Collectors.toList());
        res.setHinhAnhs(hinhAnhs);

        // Set ảnh đại diện
        String anhDaiDien = hinhAnhList.stream()
            .filter(ha -> Boolean.TRUE.equals(ha.getLaDaiDien()))
            .map(com.auro.auro.model.HinhAnh::getUrl)
            .findFirst()
            .orElseGet(() -> hinhAnhList.get(0).getUrl());
        res.setAnhDaiDien(anhDaiDien);
      }
    } catch (Exception ignored) {
    }

    // Load variants
    try {
      var variants = bienTheSanPhamRepository.findBySanPham_Id(sp.getId());
      if (variants != null && !variants.isEmpty()) {
        List<SanPhamDetailResponse.BienTheInfo> bienThes = variants.stream()
            .map(bt -> {
              SanPhamDetailResponse.BienTheInfo info = new SanPhamDetailResponse.BienTheInfo();
              info.setId(bt.getId());

              // Set màu sắc
              if (bt.getMauSac() != null) {
                info.setMauSac(bt.getMauSac().getTen());
                info.setColorHex(bt.getMauSac().getMa());
              }

              // Set kích thước
              if (bt.getKichCo() != null) {
                info.setKichThuoc(bt.getKichCo().getTen());
              }

              info.setTonKho(bt.getSoLuongTon());
              info.setGia(bt.getGia() != null ? bt.getGia() : sp.getGia());

              // Load hình ảnh biến thể
              try {
                var btHinhAnhList = hinhAnhRepository.findByBienThe_IdOrderByThuTuAscIdAsc(bt.getId());
                if (btHinhAnhList != null && !btHinhAnhList.isEmpty()) {
                  info.setHinhAnh(btHinhAnhList.get(0).getUrl());
                }
              } catch (Exception ignored) {
              }

              return info;
            })
            .collect(Collectors.toList());
        res.setBienThes(bienThes);
      }
    } catch (Exception ignored) {
    }

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
