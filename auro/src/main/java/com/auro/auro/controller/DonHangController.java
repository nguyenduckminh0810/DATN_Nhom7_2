package com.auro.auro.controller;

import java.util.List;

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

    // Lấy danh sách đơn hàng
    // @GetMapping
    // public ResponseEntity<List<DonHang>> getAllDonHang() {
    // return ResponseEntity.ok(donHangService.getAllDonHang());
    // }

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
    public ResponseEntity<DonHang> updateDonHang(@PathVariable Long id, @RequestBody DonHang donHang) {
        return ResponseEntity.ok(donHangService.updateDonHang(id, donHang));
    }

    // Xóa đơn hàng
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDonHang(@PathVariable Long id) {
        donHangService.deleteDonHang(id);
        return ResponseEntity.noContent().build();
    }
}
