package com.auro.auro.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.auro.auro.dto.request.CreateDonHangRequest;
import com.auro.auro.dto.response.DonHangResponse;
import com.auro.auro.model.DonHang;
import com.auro.auro.model.DonHangChiTiet;
import com.auro.auro.service.DonHangService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/don-hang")
@RequiredArgsConstructor

public class DonHangController {

    private final DonHangService donHangService;

    // Tạo đơn hàng
    @PostMapping
    public ResponseEntity<DonHang> createDonHang(@RequestBody CreateDonHangRequest request) {
        return ResponseEntity.ok(donHangService.createDonHang(request.getDonHang(), request.getChiTietList()));
    }

    @GetMapping
    public List<DonHangResponse> getAllDonHang() {
        return donHangService.getAllDonHangDTO();
    }

    // Lấy đơn hàng theo ID
    @GetMapping("/{id}")
    public ResponseEntity<DonHang> getDonHangById(@PathVariable Long id) {
        return ResponseEntity.ok(donHangService.getDonHangById(id));
    }

    // Xem đơn hàng chi tiết theo ID đơn hàng
    @GetMapping("/{id}/chi-tiet")
    public ResponseEntity<List<DonHangChiTiet>> getChiTietDonHangById(@PathVariable Long id) {
        return ResponseEntity.ok(donHangService.getChiTietByDonHangId(id));
    }

    // Cập nhật trạng thái đơn hàng
    @PutMapping("/{id}")
    public ResponseEntity<DonHangResponse> updateDonHang(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {

        System.out.println("=== UPDATE DON HANG ===");
        System.out.println("ID: " + id);
        System.out.println("Request body: " + request);

        try {
            DonHangResponse updated = donHangService.updateDonHang(id, request);
            System.out.println("Updated successfully: " + updated);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            System.err.println("Error updating: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    // Xóa đơn hàng
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteDonHang(@PathVariable Long id) {
        try {
            donHangService.deleteDonHang(id);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Xóa đơn hàng thành công");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("Error deleting: " + e.getMessage());
            throw e;
        }
    }

    // Phân trang đơn hàng
    @GetMapping("/phan-trang")
    public ResponseEntity<Map<String, Object>> getDonHangPhanTrang(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Pageable pageable = PageRequest.of(page, size,
                Sort.by(Sort.Direction.fromString(sortDir), sortBy));

        Page<DonHangResponse> donHangPage = donHangService.getDonHangPhanTrang(pageable);

        Map<String, Object> response = new HashMap<>();
        response.put("content", donHangPage.getContent());
        response.put("currentPage", donHangPage.getNumber());
        response.put("totalItems", donHangPage.getTotalElements());
        response.put("totalPages", donHangPage.getTotalPages());

        return ResponseEntity.ok(response);
    }
}
