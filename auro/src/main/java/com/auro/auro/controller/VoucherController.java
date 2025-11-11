package com.auro.auro.controller;

import com.auro.auro.dto.request.VoucherCheckRequest;
import com.auro.auro.dto.request.VoucherApplyRequest;
import com.auro.auro.dto.response.VoucherResponse;
import com.auro.auro.model.Voucher;
import com.auro.auro.service.VoucherService;
import com.auro.auro.service.VoucherValidationResult;
import com.auro.auro.service.VoucherApplicationResult;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/phieu-giam-gia")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class VoucherController {
    private final VoucherService voucherService;

    @GetMapping("/co-san")
    public ResponseEntity<?> getCoSan() {
        List<VoucherResponse> vouchers = voucherService.getAllVouchers()
            .stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());

        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Lấy danh sách voucher thành công",
            "data", vouchers
        ));
    }

    @PostMapping("/kiem-tra")
    public ResponseEntity<?> kiemTra(@Valid @RequestBody VoucherCheckRequest request) {
        VoucherValidationResult result = voucherService.validateVoucher(request.getMaVoucher(), request.getKhachHangId(), request.getDonHangTong());

         if(result.isValid()) {
            VoucherResponse response = convertToResponse(result.getVoucher());
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Voucher hợp lệ",
                "data",response
            ));
         }else {
            return ResponseEntity.badRequest().body(Map.of(
                "success",false,
                "message", result.getMessage()
            ));
         }
    }

    @PostMapping("/ap-dung")
    public ResponseEntity<?> apDung(@Valid @RequestBody VoucherApplyRequest request) {
        VoucherApplicationResult result = voucherService.applyVoucher(
            request.getMaVoucher(),
            request.getKhachHangId(),
            request.getDonHangTong(),
            request.getPhiVanChuyen()
        );

        if(result.isSuccess()) {
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", result.getMessage(),
                "data", Map.of(
                    "voucher", convertToResponse(result.getVoucher()),
                    "giamGia", result.getGiamGia()
                )
            ));
        }else {
            return ResponseEntity.badRequest().body(Map.of(
                "success",false,
                "message", result.getMessage()
            ));
        }
    }

    private VoucherResponse convertToResponse(Voucher voucher) {
        return VoucherResponse.builder()
            .id(voucher.getId())
            .ma(voucher.getMa())
            .loai(voucher.getLoai())
            .giaTri(voucher.getGiaTri())
            .giamToiDa(voucher.getGiamToiDa())
            .donToiThieu(voucher.getDonToiThieu())
            .batDauLuc(voucher.getBatDauLuc())
            .ketThucLuc(voucher.getKetThucLuc())
            .gioiHanSuDung(voucher.getGioiHanSuDung())
            .build();
    }
  
}
