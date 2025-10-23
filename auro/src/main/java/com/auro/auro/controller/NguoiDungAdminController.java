package com.auro.auro.controller;

import com.auro.auro.dto.request.UserUpdateRequest;
import com.auro.auro.dto.response.UserAdminResponse;
import com.auro.auro.model.TaiKhoan;
import com.auro.auro.security.CustomUserDetails;
import com.auro.auro.service.NguoiDungService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/nguoi-dung")
@RequiredArgsConstructor
public class NguoiDungAdminController {

    private final NguoiDungService nguoiDungService;

    // GET /api/nguoi-dung?page=0&size=10&vaiTro=ADM&trangThai=true&search=abc
    @GetMapping
    @PreAuthorize("hasAnyRole('STF','ADM')")
    public ResponseEntity<Page<UserAdminResponse>> listUsers(
            @RequestParam(value = "page", required = false, defaultValue = "0") Integer page,
            @RequestParam(value = "size", required = false, defaultValue = "10") Integer size,
            @RequestParam(value = "vaiTro", required = false) String vaiTro,
            @RequestParam(value = "trangThai", required = false) Boolean trangThai,
            @RequestParam(value = "search", required = false) String search) {
        Page<UserAdminResponse> result = nguoiDungService.listUsers(page, size, vaiTro, trangThai, search);
        return ResponseEntity.ok(result);
    }

    // Get detail - staff and admin can view
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('STF','ADM')")
    public ResponseEntity<UserAdminResponse> getUser(@PathVariable Long id) {
        return ResponseEntity.ok(nguoiDungService.getById(id));
    }

    // Update - admin can update anyone; staff can only update customers (role CUS)
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('STF','ADM')")
    public ResponseEntity<UserAdminResponse> updateUser(
            @PathVariable Long id,
            @RequestBody UserUpdateRequest request,
            Authentication authentication) {
        TaiKhoan current = ((CustomUserDetails) authentication.getPrincipal()).getTaiKhoan();
        boolean isStaff = current.getVaiTro() != null && "STF".equalsIgnoreCase(current.getVaiTro().getMa());
        if (isStaff) {
            UserAdminResponse target = nguoiDungService.getById(id);
            if (target.getVaiTroMa() == null || !"CUS".equalsIgnoreCase(target.getVaiTroMa())) {
                return ResponseEntity.status(403).build();
            }
        }
        return ResponseEntity.ok(nguoiDungService.updateUser(id, request));
    }

    // Soft delete - admin only
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADM')")
    public ResponseEntity<Void> softDelete(@PathVariable Long id) {
        nguoiDungService.softDelete(id);
        return ResponseEntity.noContent().build();
    }
}
