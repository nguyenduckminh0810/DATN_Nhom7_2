package com.auro.auro.controller;

import com.auro.auro.dto.request.DanhMucCreateRequest;
import com.auro.auro.dto.response.DanhMucResponse;
import com.auro.auro.model.DanhMuc;
import com.auro.auro.repository.DanhMucRepository;
import com.auro.auro.repository.SanPhamRepository;
import com.auro.auro.repository.BienTheSanPhamRepository;
import com.auro.auro.repository.HinhAnhRepository;
import com.auro.auro.repository.DonHangChiTietRepository;
import com.auro.auro.repository.GioHangChiTietRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/danh-muc")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class DanhMucController {

    private final DanhMucRepository danhMucRepository;
    private final SanPhamRepository sanPhamRepository;
    private final BienTheSanPhamRepository bienTheSanPhamRepository;
    private final HinhAnhRepository hinhAnhRepository;
    private final DonHangChiTietRepository donHangChiTietRepository;
    private final GioHangChiTietRepository gioHangChiTietRepository;

    @GetMapping
    public ResponseEntity<List<DanhMucResponse>> getAll() {
        List<DanhMucResponse> list = danhMucRepository.findAll().stream()
                .map(this::map)
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DanhMucResponse> getById(
            @org.springframework.web.bind.annotation.PathVariable("id") Long id) {
        DanhMuc dm = danhMucRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Danh mục không tồn tại"));
        return ResponseEntity.ok(map(dm));
    }

    @GetMapping("/page")
    public ResponseEntity<Page<DanhMucResponse>> page(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<DanhMucResponse> res = danhMucRepository.findAll(PageRequest.of(page, size))
                .map(this::map);
        return ResponseEntity.ok(res);
    }

    private DanhMucResponse map(DanhMuc dm) {
        DanhMucResponse res = new DanhMucResponse();
        res.setId(dm.getId());
        res.setTen(dm.getTen());
        res.setSlug(dm.getSlug());
        res.setIdCha(dm.getDanhMucCha() != null ? dm.getDanhMucCha().getId() : null);
        res.setThuTu(dm.getThuTu());
        // return numeric flag (1 or 0)
        res.setHoatDong(dm.getHoatDong() != null && dm.getHoatDong() == 1 ? 1 : 0);
        return res;
    }

    @PostMapping("/create")
    public ResponseEntity<DanhMucResponse> create(@Valid @RequestBody DanhMucCreateRequest req) {
        if (danhMucRepository.existsBySlug(req.getSlug())) {
            return ResponseEntity.badRequest().build();
        }

        DanhMuc dm = new DanhMuc();
        dm.setTen(req.getTen());
        dm.setSlug(req.getSlug());
        dm.setThuTu(req.getThuTu());
        dm.setHoatDong(req.getHoatDong());

        if (req.getIdCha() != null) {
            danhMucRepository.findById(req.getIdCha()).ifPresent(dm::setDanhMucCha);
        }

        DanhMuc saved = danhMucRepository.save(dm);
        DanhMucResponse out = map(saved);
        return ResponseEntity.ok(out);
    }

    @org.springframework.web.bind.annotation.PutMapping("/{id}")
    public ResponseEntity<DanhMucResponse> update(@org.springframework.web.bind.annotation.PathVariable("id") Long id,
            @Valid @RequestBody DanhMucCreateRequest req) {
        DanhMuc existing = danhMucRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Danh mục không tồn tại"));

        // Check slug uniqueness (exclude current id)
        if (danhMucRepository.existsBySlugAndIdNot(req.getSlug(), id)) {
            return ResponseEntity.badRequest().body(null);
        }

        existing.setTen(req.getTen());
        existing.setSlug(req.getSlug());
        existing.setThuTu(req.getThuTu());
        existing.setHoatDong(req.getHoatDong());

        if (req.getIdCha() != null) {
            danhMucRepository.findById(req.getIdCha()).ifPresent(existing::setDanhMucCha);
        } else {
            existing.setDanhMucCha(null);
        }

        DanhMuc saved = danhMucRepository.save(existing);
        DanhMucResponse out = map(saved);
        return ResponseEntity.ok(out);
    }

    // Delete category (soft/hard logic). If force=true, will delete related
    // products and descendant categories.
    @org.springframework.web.bind.annotation.DeleteMapping("/{id}")
    @org.springframework.transaction.annotation.Transactional
    public ResponseEntity<Object> deleteCategory(@org.springframework.web.bind.annotation.PathVariable("id") Long id,
            @RequestParam(defaultValue = "false") boolean force) {
        DanhMuc root = danhMucRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Danh mục không tồn tại"));

        // collect all descendant ids (including root)
        List<Long> toDeleteIds = new java.util.ArrayList<>();
        collectDescendants(root, toDeleteIds);

        // check related products
        long relatedProducts = sanPhamRepository.countByDanhMuc_IdIn(toDeleteIds);

        // gather dependent counts: we'll fetch only product IDs to avoid selecting all
        // product columns
        java.util.List<Long> productIds = sanPhamRepository.findIdsByDanhMucIdIn(toDeleteIds);
        long variantCount = 0;
        long imageCount = 0;
        long orderItemCount = 0;
        long cartItemCount = 0;

        if (!productIds.isEmpty()) {
            variantCount = bienTheSanPhamRepository.findBySanPham_IdIn(productIds).size();
            // images
            for (Long pid : productIds) {
                imageCount += hinhAnhRepository.findBySanPham_IdOrderByThuTuAscIdAsc(pid).size();
            }
            // order items reference variants; sum by variants
            java.util.List<com.auro.auro.model.BienTheSanPham> variants = bienTheSanPhamRepository
                    .findBySanPham_IdIn(productIds);
            java.util.List<Long> variantIds = new java.util.ArrayList<>();
            for (com.auro.auro.model.BienTheSanPham v : variants)
                variantIds.add(v.getId());
            if (!variantIds.isEmpty()) {
                // for order items
                for (Long vid : variantIds) {
                    orderItemCount += donHangChiTietRepository.findByBienThe_Id(vid).size();
                    // cart items: no convenient method to count by variant; assume repository
                    // exists
                    // We'll try to delete cart items by variant id if repository supports it
                    try {
                        cartItemCount += gioHangChiTietRepository.findByBienThe_Id(vid).size();
                    } catch (NoSuchMethodError | AbstractMethodError ex) {
                        // repository may not implement findByBienThe_Id; ignore for counting
                    }
                }
            }
        }

        if (relatedProducts > 0 && !force) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(java.util.Map.of(
                    "message", "Danh mục hoặc các danh mục con có liên kết tới sản phẩm",
                    "relatedCategoryIds", toDeleteIds,
                    "relatedProducts", relatedProducts,
                    "variantCount", variantCount,
                    "imageCount", imageCount,
                    "orderItemCount", orderItemCount,
                    "cartItemCount", cartItemCount));
        }

        try {
            if (relatedProducts > 0 && force) {
                // Delete dependent resources in safe order
                // 1) delete order items referencing variants
                if (!productIds.isEmpty()) {
                    java.util.List<com.auro.auro.model.BienTheSanPham> variants = bienTheSanPhamRepository
                            .findBySanPham_IdIn(productIds);
                    for (com.auro.auro.model.BienTheSanPham v : variants) {
                        try {
                            donHangChiTietRepository.deleteByBienThe_Id(v.getId());
                        } catch (Exception ignored) {
                        }
                        try {
                            gioHangChiTietRepository.deleteByBienThe_Id(v.getId());
                        } catch (Exception ignored) {
                        }
                        // delete images for variant if any
                        try {
                            hinhAnhRepository.deleteByBienThe_Id(v.getId());
                        } catch (Exception ignored) {
                        }
                    }
                    // delete variants
                    bienTheSanPhamRepository.deleteBySanPham_IdIn(productIds);
                    // delete images for products
                    for (Long pid : productIds) {
                        try {
                            hinhAnhRepository.deleteBySanPham_Id(pid);
                        } catch (Exception ignored) {
                        }
                    }
                    // delete products
                    sanPhamRepository.deleteByDanhMuc_IdIn(toDeleteIds);
                }
            }

            // delete categories (children first if cascade not configured)
            toDeleteIds.forEach(danhMucRepository::deleteById);

            return ResponseEntity
                    .ok(java.util.Map.of("message", "Xóa thành công", "deletedProductCount", relatedProducts));
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Lỗi khi xóa: " + e.getMessage());
        }
    }

    private void collectDescendants(DanhMuc node, List<Long> out) {
        out.add(node.getId());
        List<DanhMuc> children = danhMucRepository.findByDanhMucCha_Id(node.getId());
        if (children != null && !children.isEmpty()) {
            for (DanhMuc c : children) {
                collectDescendants(c, out);
            }
        }
    }
}
