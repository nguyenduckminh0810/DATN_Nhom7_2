package com.auro.auro.controller;

import com.auro.auro.dto.request.TaoDonTuGioHangRequest;
import com.auro.auro.dto.response.DonHangResponse;
import com.auro.auro.service.DonHangService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@RequestMapping("/api/khach-hang/don-hang")
@PreAuthorize("hasRole('CUS')")
public class DonHangKhachController {

    @Autowired
    private DonHangService donHangService;

    // tạo đơn hàng từ giỏ hàng
    @PostMapping("/tao-tu-gio-hang")
    public ResponseEntity<DonHangResponse> taoDonTuGioHang( @RequestBody TaoDonTuGioHangRequest request, Authentication auth) {
        try{
            Long khachHangId = Long.parseLong(auth.getName());
            DonHangResponse dh = donHangService.taoDonTuGioHang(request, khachHangId);
            return ResponseEntity.ok(dh);
        }catch(Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // lấy đơn hàng của khách
    @GetMapping("/don-hang-cua-toi")
    public ResponseEntity <Page<DonHangResponse>> layDonHangCuaToi(@RequestParam(defaultValue = "0") int trang, @RequestParam(defaultValue = "10") int kichThuoc, Authentication auth) {
        try{
            Long khachHangId = Long.parseLong(auth.getName());
            Page<DonHangResponse> dhs = donHangService.layDonHangCuaKhach(khachHangId, trang, kichThuoc);
            return ResponseEntity.ok(dhs);
        }catch(Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Hủy đơn hàng
    @PutMapping("/{donHangId}/huy")
    public ResponseEntity<DonHangResponse> huyDonHang(@PathVariable Long donHangId, Authentication auth) {
        try{
            Long khachHangId = Long.parseLong(auth.getName());
            DonHangResponse dh = donHangService.huyDonHang(donHangId,khachHangId);
            return ResponseEntity.ok(dh);
        }catch(Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Lấy chi tiết đơn hàng
    @GetMapping("/{donHangId}")
    public ResponseEntity<DonHangResponse> layChiTietDonHang(@PathVariable Long donHangId, Authentication auth) {
        try{
            Long khachHangId = Long.parseLong(auth.getName());
            DonHangResponse dh = donHangService.layChiTietDonHangKhach(donHangId, khachHangId);
            return ResponseEntity.ok(dh);
        }catch(Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    

}
