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
import com.auro.auro.repository.GioHangChiTietRepository;
import com.auro.auro.repository.DonHangChiTietRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.UUID;
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
  private final GioHangChiTietRepository gioHangChiTietRepository;
  private final DonHangChiTietRepository donHangChiTietRepository;

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

  public Page<SanPhamResponse> getPage(String search, Long danhMucId, String sortBy, String sortOrder, String status,
      Boolean inStock, Pageable pageable) {
    // Create new pageable with sorting if specified
    if (sortBy != null && !sortBy.trim().isEmpty()) {
      org.springframework.data.domain.Sort.Direction direction = "asc".equalsIgnoreCase(sortOrder)
          ? org.springframework.data.domain.Sort.Direction.ASC
          : org.springframework.data.domain.Sort.Direction.DESC;

      String sortField = "created_at".equals(sortBy) ? "taoLuc" : sortBy;
      org.springframework.data.domain.Sort sort = org.springframework.data.domain.Sort.by(direction, sortField);
      pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
    }

    Page<SanPham> page;

    // Build query conditions
    if (search != null && !search.trim().isEmpty() && danhMucId != null) {
      if ("active".equals(status)) {
        page = sanPhamRepository.findByDanhMuc_IdAndTenContainingIgnoreCaseAndTrangThai(
            danhMucId, search, "active", pageable);
      } else {
        page = sanPhamRepository.findByDanhMuc_IdAndTenContainingIgnoreCaseOrDanhMuc_IdAndMoTaContainingIgnoreCase(
            danhMucId, search, danhMucId, search, pageable);
      }
    } else if (search != null && !search.trim().isEmpty()) {
      if ("active".equals(status)) {
        page = sanPhamRepository.findByTenContainingIgnoreCaseAndTrangThai(search, "active", pageable);
      } else {
        page = sanPhamRepository.findByTenContainingIgnoreCaseOrMoTaContainingIgnoreCase(search, search, pageable);
      }
    } else if (danhMucId != null) {
      if ("active".equals(status)) {
        page = sanPhamRepository.findByDanhMuc_IdAndTrangThai(danhMucId, "active", pageable);
      } else {
        page = sanPhamRepository.findByDanhMuc_Id(danhMucId, pageable);
      }
    } else {
      if ("active".equals(status)) {
        page = sanPhamRepository.findByTrangThai("active", pageable);
      } else {
        page = sanPhamRepository.findAll(pageable);
      }
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
    // Kiểm tra sản phẩm tồn tại
    if (!sanPhamRepository.existsById(id)) {
      throw new ResourceNotFoundException("Sản phẩm không tồn tại: " + id);
    }

    // 1. Kiểm tra xem có biến thể nào trong giỏ hàng hoặc đơn hàng không
    // Lấy danh sách ID biến thể (không load full entity để tránh
    // TransientObjectException)
    List<Long> variantIds = bienTheSanPhamRepository.findBySanPham_Id(id)
        .stream()
        .map(com.auro.auro.model.BienTheSanPham::getId)
        .collect(java.util.stream.Collectors.toList());

    for (Long variantId : variantIds) {
      long cartCount = gioHangChiTietRepository.countByBienThe_Id(variantId);
      if (cartCount > 0) {
        throw new BadRequestException(
            "Không thể xóa sản phẩm vì có " + cartCount + " mục trong giỏ hàng đang sử dụng biến thể. " +
                "Vui lòng yêu cầu khách hàng xóa khỏi giỏ hàng hoặc đặt trạng thái sản phẩm thành 'Ngừng bán'.");
      }

      long orderCount = donHangChiTietRepository.countByBienThe_Id(variantId);
      if (orderCount > 0) {
        throw new BadRequestException(
            "Không thể xóa sản phẩm vì có " + orderCount + " đơn hàng đang liên quan đến biến thể. " +
                "Vui lòng đặt trạng thái sản phẩm thành 'Ngừng bán' thay vì xóa.");
      }
    }

    // 2. Xóa hình ảnh của từng biến thể (dùng native delete, không load entity)
    for (Long variantId : variantIds) {
      hinhAnhRepository.deleteByBienThe_Id(variantId);
    }

    // 3. Xóa tất cả biến thể của sản phẩm
    bienTheSanPhamRepository.deleteBySanPham_Id(id);

    // 4. Xóa hình ảnh của sản phẩm
    hinhAnhRepository.deleteBySanPham_Id(id);

    // 5. Cuối cùng xóa sản phẩm (dùng JPQL DELETE để tránh load entity)
    sanPhamRepository.deleteProductById(id);
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

    // Load variants
    try {
      List<com.auro.auro.model.BienTheSanPham> variants = bienTheSanPhamRepository.findBySanPham_Id(sp.getId());
      if (variants != null && !variants.isEmpty()) {
        // Tính tổng số lượng tồn kho
        int tongTonKho = variants.stream()
            .mapToInt(bt -> bt.getSoLuongTon() != null ? bt.getSoLuongTon() : 0)
            .sum();
        res.setTonKho(tongTonKho);

        // Map variants to response
        List<SanPhamResponse.BienTheInfo> bienThes = variants.stream()
            .map(bt -> {
              SanPhamResponse.BienTheInfo info = new SanPhamResponse.BienTheInfo();
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
      } else {
        res.setTonKho(0);
      }
    } catch (Exception ignored) {
      res.setTonKho(0);
    }

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

  /**
   * Sinh slug unique với UUID ngắn để tránh trùng lặp hoàn toàn
   */
  private String generateUniqueSlug(String base, Long currentId) {
    // Generate random part (10 ký tự từ UUID)
    String randomPart = UUID.randomUUID().toString().replace("-", "").substring(0, 10);

    // Slug format: slug-{random}
    // VD: ao-thun-a1b2c3d4e5
    String slug = "slug-" + randomPart;

    // Đảm bảo unique (tuy UUID gần như không trùng, nhưng vẫn check)
    boolean exists = currentId == null
        ? sanPhamRepository.existsBySlug(slug)
        : sanPhamRepository.existsBySlugAndIdNot(slug, currentId);

    while (exists) {
      randomPart = UUID.randomUUID().toString().replace("-", "").substring(0, 10);
      slug = "slug-" + randomPart;
      exists = currentId == null
          ? sanPhamRepository.existsBySlug(slug)
          : sanPhamRepository.existsBySlugAndIdNot(slug, currentId);
    }

    return slug;
  }

  // Lấy sản phẩm bán chạy nhất dựa trên số lượng đã bán

  @Transactional(readOnly = true)
  public Page<SanPhamResponse> getBestSellers(Pageable pageable) {
    try {
      Page<SanPham> page = sanPhamRepository.findBestSellers(pageable);
      return page.map(this::mapToResponse);
    } catch (Exception e) {
      // Log lỗi chi tiết
      System.err.println("Error fetching best sellers: " + e.getMessage());
      e.printStackTrace();

      // Fallback: trả về sản phẩm active thông thường
      Page<SanPham> fallbackPage = sanPhamRepository.findAll(
          PageRequest.of(pageable.getPageNumber(), pageable.getPageSize()));
      return fallbackPage.map(this::mapToResponse);
    }
  }

  // Lấy sản phẩm liên quan dựa trên cùng danh mục
  @Transactional(readOnly = true)
  public Page<SanPhamResponse> getRelatedProducts(Long productId, Pageable pageable) {
    try {
      // Lấy sản phẩm gốc
      SanPham product = sanPhamRepository.findById(productId)
          .orElseThrow(() -> new ResourceNotFoundException("Sản phẩm không tồn tại"));

      // Lấy sản phẩm cùng danh mục, trừ sản phẩm hiện tại
      Page<SanPham> relatedPage = sanPhamRepository.findByDanhMucIdAndIdNotAndTrangThai(
          product.getDanhMuc().getId(),
          productId,
          "active",
          pageable);

      return relatedPage.map(this::mapToResponse);
    } catch (Exception e) {
      System.err.println("Error fetching related products: " + e.getMessage());
      e.printStackTrace();

      // Fallback: trả về sản phẩm active ngẫu nhiên
      Page<SanPham> fallbackPage = sanPhamRepository.findByTrangThai("active", pageable);
      return fallbackPage.map(this::mapToResponse);
    }
  }
}
