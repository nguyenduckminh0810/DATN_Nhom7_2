package com.auro.auro.service;

import com.auro.auro.dto.request.UserUpdateRequest;
import com.auro.auro.dto.response.UserAdminResponse;
import com.auro.auro.model.TaiKhoan;
import com.auro.auro.model.VaiTro;
import com.auro.auro.repository.TaiKhoanRepository;
import com.auro.auro.repository.VaiTroRepository;
import com.auro.auro.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NguoiDungService {
    private static final String STAFF_EDIT_INFO_FORBIDDEN = "Nhân viên không được phép sửa thông tin tài khoản";

    private final TaiKhoanRepository taiKhoanRepository;
    private final com.auro.auro.repository.DonHangRepository donHangRepository;
    private final com.auro.auro.repository.KhachHangRepository khachHangRepository;
    private final VaiTroRepository vaiTroRepository;

    public Page<UserAdminResponse> listUsers(Integer page, Integer size, String vaiTroMa, Boolean trangThai,
            String search) {
        int p = page != null && page >= 0 ? page : 0;
        int s = size != null && size > 0 ? size : 10;
        Pageable pageable = PageRequest.of(p, s, Sort.by(Sort.Direction.DESC, "taoLuc"));

        // For v1: basic in-memory filtering on small datasets. Can be replaced with
        // Specifications later.
        Page<TaiKhoan> tkPage = taiKhoanRepository.findAll(pageable);
        List<TaiKhoan> filtered = tkPage.getContent().stream()
                .filter(tk -> vaiTroMa == null || vaiTroMa.isBlank()
                        || (tk.getVaiTro() != null && vaiTroMa.equalsIgnoreCase(tk.getVaiTro().getMa())))
                .filter(tk -> trangThai == null || trangThai.equals(tk.getTrangThai()))
                .filter(tk -> {
                    if (search == null || search.isBlank())
                        return true;
                    String q = search.toLowerCase(Locale.ROOT);
                    String email = tk.getEmail() != null ? tk.getEmail().toLowerCase(Locale.ROOT) : "";
                    String sdt = tk.getSoDienThoai() != null ? tk.getSoDienThoai().toLowerCase(Locale.ROOT) : "";
                    String role = tk.getVaiTro() != null ? tk.getVaiTro().getMa().toLowerCase(Locale.ROOT) : "";
                    return email.contains(q) || sdt.contains(q) || role.contains(q);
                })
                .collect(Collectors.toList());

        List<UserAdminResponse> data = filtered.stream().map(this::toResponse).collect(Collectors.toList());
        return new PageImpl<>(data, pageable, tkPage.getTotalElements());
    }

    private TaiKhoan getCurrentTaiKhoan() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails principal)) {
            throw new AccessDeniedException("Không xác định được người dùng hiện tại");
        }
        return principal.getTaiKhoan();
    }

    private String getRoleCode(TaiKhoan tk) {
        return tk.getVaiTro() != null ? tk.getVaiTro().getMa() : null;
    }

    private boolean isAdmin(TaiKhoan tk) {
        String ma = getRoleCode(tk);
        return ma != null && "ADM".equalsIgnoreCase(ma);
    }

    private boolean isStaff(TaiKhoan tk) {
        String ma = getRoleCode(tk);
        return ma != null && "STF".equalsIgnoreCase(ma);
    }

    private UserAdminResponse toResponse(TaiKhoan tk) {
        // Calculate order count and total spent for customers
        Integer orderCount = 0;
        Double totalSpent = 0.0;

        // Find KhachHang by TaiKhoan
        var khachHangOpt = khachHangRepository.findByTaiKhoan(tk);
        if (khachHangOpt.isPresent()) {
            Long khachHangId = khachHangOpt.get().getId();
            orderCount = donHangRepository.countByKhachHangId(khachHangId);
            totalSpent = donHangRepository.sumTotalSpentByKhachHangId(khachHangId);
        }

        return UserAdminResponse.builder()
                .id(tk.getId())
                .email(tk.getEmail())
                .soDienThoai(tk.getSoDienThoai())
                .vaiTroMa(tk.getVaiTro() != null ? tk.getVaiTro().getMa() : null)
                .vaiTroTen(tk.getVaiTro() != null ? tk.getVaiTro().getTen() : null)
                .trangThai(tk.getTrangThai())
                .taoLuc(tk.getTaoLuc())
                .capNhatLuc(tk.getCapNhatLuc())
                .orderCount(orderCount)
                .totalSpent(totalSpent)
                .build();
    }

    public UserAdminResponse getById(Long id) {
        TaiKhoan tk = taiKhoanRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản"));
        return toResponse(tk);
    }

    public UserAdminResponse updateUser(Long id, UserUpdateRequest req) {
        TaiKhoan current = getCurrentTaiKhoan();
        TaiKhoan target = taiKhoanRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản"));

        String targetRole = getRoleCode(target);

        boolean currentIsAdmin = isAdmin(current);
        boolean currentIsStaff = isStaff(current);

        // STAFF rules
        if (currentIsStaff) {
            // 1) Cấm sửa email / sđt
            if (req.getEmail() != null && !req.getEmail().equals(target.getEmail())) {
                throw new AccessDeniedException(STAFF_EDIT_INFO_FORBIDDEN);
            }
            if (req.getSoDienThoai() != null && !req.getSoDienThoai().equals(target.getSoDienThoai())) {
                throw new AccessDeniedException(STAFF_EDIT_INFO_FORBIDDEN);
            }

            // 2) Cấm sửa vai trò
            if (req.getVaiTroMa() != null
                    && !req.getVaiTroMa().equalsIgnoreCase(targetRole)) {
                throw new AccessDeniedException(STAFF_EDIT_INFO_FORBIDDEN);
            }

            // 3) Chỉ được đổi trạng thái cho khách hàng (CUS)
            if (req.getTrangThai() != null
                    && !req.getTrangThai().equals(target.getTrangThai())
                    && !"CUS".equalsIgnoreCase(targetRole)) {
                throw new AccessDeniedException("Nhân viên chỉ được phép đổi trạng thái tài khoản khách hàng");
            }
        }

        // ADMIN có full quyền: sửa email, sđt, vai trò, trạng thái
        if (req.getEmail() != null) {
            target.setEmail(req.getEmail());
        }
        if (req.getSoDienThoai() != null) {
            target.setSoDienThoai(req.getSoDienThoai());
        }
        if (req.getTrangThai() != null) {
            target.setTrangThai(req.getTrangThai());
        }

        // Cập nhật vai trò nếu có vaiTroMa và người sửa là ADMIN
        if (req.getVaiTroMa() != null && currentIsAdmin) {
            VaiTro vaiTro = vaiTroRepository.findByMa(req.getVaiTroMa())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy vai trò: " + req.getVaiTroMa()));
            target.setVaiTro(vaiTro);
        }

        TaiKhoan saved = taiKhoanRepository.save(target);
        return toResponse(saved);
    }

    public void softDelete(Long id) {
        TaiKhoan tk = taiKhoanRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản"));
        tk.setTrangThai(false);
        taiKhoanRepository.save(tk);
    }

    public void bulkUpdateStatus(List<Long> ids, Boolean trangThai) {
        if (ids == null || ids.isEmpty()) {
            return;
        }

        TaiKhoan current = getCurrentTaiKhoan();
        boolean currentIsStaff = isStaff(current);

        List<TaiKhoan> targets = taiKhoanRepository.findAllById(ids);

        if (targets.size() != ids.size()) {
            throw new RuntimeException("Một số tài khoản không tồn tại");
        }

        // RBAC cho STAFF
        if (currentIsStaff) {
            for (TaiKhoan target : targets) {
                String targetRole = getRoleCode(target);
                // STAFF chỉ được đổi trạng thái cho CUS
                if (!"CUS".equalsIgnoreCase(targetRole)) {
                    throw new AccessDeniedException("Nhân viên chỉ được phép đổi trạng thái tài khoản khách hàng");
                }
            }
        }

        for (TaiKhoan target : targets) {
            target.setTrangThai(trangThai);
        }

        taiKhoanRepository.saveAll(targets);
    }
}
