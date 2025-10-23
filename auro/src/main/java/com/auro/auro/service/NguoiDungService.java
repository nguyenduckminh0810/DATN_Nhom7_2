package com.auro.auro.service;

import com.auro.auro.dto.request.UserUpdateRequest;
import com.auro.auro.dto.response.UserAdminResponse;
import com.auro.auro.model.TaiKhoan;
import com.auro.auro.repository.TaiKhoanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NguoiDungService {
    private final TaiKhoanRepository taiKhoanRepository;

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

    private UserAdminResponse toResponse(TaiKhoan tk) {
        return UserAdminResponse.builder()
                .id(tk.getId())
                .email(tk.getEmail())
                .soDienThoai(tk.getSoDienThoai())
                .vaiTroMa(tk.getVaiTro() != null ? tk.getVaiTro().getMa() : null)
                .vaiTroTen(tk.getVaiTro() != null ? tk.getVaiTro().getTen() : null)
                .trangThai(tk.getTrangThai())
                .taoLuc(tk.getTaoLuc())
                .capNhatLuc(tk.getCapNhatLuc())
                .build();
    }

    public UserAdminResponse getById(Long id) {
        TaiKhoan tk = taiKhoanRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản"));
        return toResponse(tk);
    }

    public UserAdminResponse updateUser(Long id, UserUpdateRequest req) {
        TaiKhoan tk = taiKhoanRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản"));
        if (req.getEmail() != null)
            tk.setEmail(req.getEmail());
        if (req.getSoDienThoai() != null)
            tk.setSoDienThoai(req.getSoDienThoai());
        if (req.getTrangThai() != null)
            tk.setTrangThai(req.getTrangThai());
        TaiKhoan saved = taiKhoanRepository.save(tk);
        return toResponse(saved);
    }

    public void softDelete(Long id) {
        TaiKhoan tk = taiKhoanRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản"));
        tk.setTrangThai(false);
        taiKhoanRepository.save(tk);
    }
}
