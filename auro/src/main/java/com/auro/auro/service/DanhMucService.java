package com.auro.auro.service;

import com.auro.auro.dto.response.SanPhamResponse;
import com.auro.auro.exception.ResourceNotFoundException;
import com.auro.auro.model.DanhMuc;
import com.auro.auro.model.SanPham;
import com.auro.auro.repository.DanhMucRepository;
import com.auro.auro.repository.SanPhamRepository;

import org.springframework.util.StringUtils;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DanhMucService {
    private final SanPhamRepository sanPhamRepository;
    private final DanhMucRepository danhMucRepository;

    public Page<SanPham> findSanPhamBySlug(String slug, int page, int size, String sortBy, String direction) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.max(size, 1),
                Sort.by("DESC".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC,
                        (sortBy == null || sortBy.isBlank()) ? "id" : sortBy));

        // Tìm danh mục theo slug
        Optional<DanhMuc> dmOpt = danhMucRepository.findBySlug(slug);
        if (dmOpt.isPresent()) {
            List<Long> ids = new ArrayList<>();
            ids.add(dmOpt.get().getId());
            collectDescendantIds(dmOpt.get().getId(), ids); // thu thập id các danh mục con
            return sanPhamRepository.findByDanhMuc_IdIn(ids, pageable);
        }
        // fallback: tìm theo slug nếu không có danh mục
        return sanPhamRepository.findByDanhMuc_Slug(slug, pageable);
    }

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

    private SanPhamResponse mapToResponse(SanPham sp) {
        // basic mapping: return an empty response if product is null or populate as
        // needed
        if (sp == null) {
            return null;
        }
        SanPhamResponse res = new SanPhamResponse();
        // populate fields if your SanPhamResponse has setters, e.g.:
        // res.setId(sp.getId());
        // res.setTen(sp.getTen());
        // res.setGia(sp.getGia());
        // res.setMoTa(sp.getMoTa());
        // if (sp.getDanhMuc() != null) {
        // res.setDanhMucId(sp.getDanhMuc().getId());
        // res.setDanhMucSlug(sp.getDanhMuc().getSlug());
        // }
        return res;
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
}
