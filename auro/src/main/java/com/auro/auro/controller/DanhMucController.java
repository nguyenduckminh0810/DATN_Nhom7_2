package com.auro.auro.controller;

import com.auro.auro.dto.request.DanhMucCreateRequest;
import com.auro.auro.dto.response.DanhMucResponse;
import com.auro.auro.dto.response.SanPhamResponse;
import com.auro.auro.exception.ResourceNotFoundException;
import com.auro.auro.model.DanhMuc;
import com.auro.auro.repository.DanhMucRepository;
import com.auro.auro.repository.SanPhamRepository;
import com.auro.auro.service.SanPhamService;
import com.auro.auro.repository.BienTheSanPhamRepository;
import com.auro.auro.repository.HinhAnhRepository;
import com.auro.auro.repository.DonHangChiTietRepository;
import com.auro.auro.repository.GioHangChiTietRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.server.ResponseStatusException;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
        List<DanhMuc> all = danhMucRepository.findAll();

        // Tự build childrenMap, tránh key = null
        Map<Long, List<Long>> childrenMap = new java.util.HashMap<>();
        for (DanhMuc dm : all) {
            Long parentId = (dm.getDanhMucCha() != null) ? dm.getDanhMucCha().getId() : 0L; // 0L = gốc
            childrenMap.computeIfAbsent(parentId, k -> new java.util.ArrayList<>()).add(dm.getId());
        }

        List<DanhMucResponse> list = new java.util.ArrayList<>();
        for (DanhMuc dm : all) {
            // Thu thập id của chính nó + toàn bộ con cháu
            java.util.List<Long> ids = new java.util.ArrayList<>();
            ids.add(dm.getId());
            collectDescendantIds(dm.getId(), childrenMap, ids);

            long count = sanPhamRepository.countByDanhMuc_IdIn(ids);

            DanhMucResponse res = map(dm);
            res.setProductCount(count);
            list.add(res);
        }
        return ResponseEntity.ok(list);
    }

    private void collectDescendantIds(Long parentId,
            java.util.Map<Long, java.util.List<Long>> childrenMap,
            java.util.List<Long> out) {
        java.util.List<Long> children = childrenMap.getOrDefault(parentId, java.util.Collections.emptyList());
        for (Long childId : children) {
            out.add(childId);
            collectDescendantIds(childId, childrenMap, out);
        }
    }

    private void collectDescendantIds(Long parentId, List<Long> out) {
        List<DanhMuc> children = danhMucRepository.findByDanhMucCha_Id(parentId);
        if (children != null) {
            for (DanhMuc c : children) {
                out.add(c.getId());
                collectDescendantIds(c.getId(), out);
            }
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<DanhMucResponse> getById(@PathVariable Long id) {
        DanhMuc dm = danhMucRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Danh mục không tồn tại"));
        List<Long> ids = new ArrayList<>();
        ids.add(dm.getId());
        collectDescendantIds(dm.getId(), ids);
        DanhMucResponse res = map(dm);
        res.setProductCount(sanPhamRepository.countByDanhMuc_IdIn(ids));
        return ResponseEntity.ok(res);
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

    @PreAuthorize("hasRole('ADM')")
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

    @PreAuthorize("hasRole('ADM')")
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
    @PreAuthorize("hasRole('ADM')")
    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<?> deleteCategory(@PathVariable Long id,
            @RequestParam(name = "force", defaultValue = "false") boolean force) {

        DanhMuc root = danhMucRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Danh mục không tồn tại: " + id));

        // Thu thập toàn bộ id: chính nó + con cháu
        List<Long> toDeleteIds = new ArrayList<>();
        toDeleteIds.add(root.getId());
        collectDescendantIds(root.getId(), toDeleteIds);

        // ĐẾM tổng số sản phẩm thuộc bất kỳ id nào ở trên
        long relatedProducts = sanPhamRepository.countByDanhMuc_IdIn(toDeleteIds);

        if (relatedProducts > 0 && !force) {
            Map<String, Object> body = Map.of(
                    "message", "Danh mục hoặc danh mục con có liên kết tới " + relatedProducts + " sản phẩm",
                    "relatedProducts", relatedProducts,
                    "relatedCategoryIds", toDeleteIds);
            return ResponseEntity.status(HttpStatus.CONFLICT).contentType(MediaType.APPLICATION_JSON).body(body);
        }

        // Nếu force: xóa theo thứ tự Hình ảnh → Biến thể → Sản phẩm → Danh mục
        if (relatedProducts > 0) {
            // Lấy danh sách ID sản phẩm thuộc các danh mục này (chỉ ID, không load entity)
            List<Long> sanPhamIds = sanPhamRepository.findByDanhMuc_IdIn(toDeleteIds)
                    .stream()
                    .map(sp -> sp.getId())
                    .collect(Collectors.toList());

            if (!sanPhamIds.isEmpty()) {
                // Lấy danh sách ID biến thể (chỉ ID, không load entity)
                List<Long> bienTheIds = bienTheSanPhamRepository.findIdsBySanPham_IdIn(sanPhamIds);

                // Bước 1: Xóa hình ảnh của biến thể
                if (!bienTheIds.isEmpty()) {
                    hinhAnhRepository.deleteByBienThe_IdIn(bienTheIds);
                }

                // Bước 2: Xóa hình ảnh của sản phẩm
                hinhAnhRepository.deleteBySanPham_IdIn(sanPhamIds);

                // Bước 3: Xóa biến thể
                bienTheSanPhamRepository.deleteBySanPham_IdIn(sanPhamIds);
            }

            // Bước 4: Xóa sản phẩm
            sanPhamRepository.deleteByDanhMuc_IdIn(toDeleteIds);
        }

        // Bước 5: Xóa danh mục từ lá lên gốc
        for (int i = toDeleteIds.size() - 1; i >= 0; i--) {
            danhMucRepository.deleteById(toDeleteIds.get(i));
        }

        return ResponseEntity.ok(Map.of(
                "deletedCategoryIds", toDeleteIds,
                "deletedProducts", relatedProducts));
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
