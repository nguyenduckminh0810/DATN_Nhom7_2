package com.auro.auro.controller;

import com.auro.auro.dto.request.VoucherCreateRequest;
import com.auro.auro.dto.request.VoucherUpdateRequest;
import com.auro.auro.dto.response.ApiResponse;
import com.auro.auro.dto.response.VoucherResponse;
import com.auro.auro.model.Voucher;
import com.auro.auro.service.VoucherService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/phieu-giam-gia/quan-ly")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class VoucherAdminController {

    private final VoucherService voucherService;

    //getAllVoucherAdmin
    @GetMapping
    public ResponseEntity<ApiResponse<List<VoucherResponse>>> getAll() {
        System.out.println("=== GET ALL VOUCHERS DEBUG ===");
        
        try {
            List<Voucher> voucherEntities = voucherService.getAllVouchersForAdmin();
            System.out.println("Found " + voucherEntities.size() + " vouchers in database");
            
            List<VoucherResponse> vouchers = voucherEntities
            .stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());

            System.out.println("Converted to " + vouchers.size() + " responses");
            System.out.println("Voucher responses: " + vouchers);

            ApiResponse<List<VoucherResponse>> response = ApiResponse.success(vouchers, "Lấy danh sách voucher thành công");
            System.out.println("Final GET response: " + response);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.out.println("ERROR in getAll: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    //Tìm voucher theo id
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<VoucherResponse>> getVoucherById(@PathVariable Long id) {
        Voucher voucher = voucherService.getVoucherById(id);
        VoucherResponse response = convertToResponse(voucher);

        return ResponseEntity.ok(ApiResponse.success(response, "Lấy voucher thành công"));
    }

    // tạo voucher
    @PostMapping
    public ResponseEntity<ApiResponse<VoucherResponse>> createVoucher(@Valid @RequestBody VoucherCreateRequest request) {
        System.out.println("=== CREATE VOUCHER DEBUG ===");
        System.out.println("Received request: " + request);
        
        try {
            Voucher voucher = voucherService.createVoucher(request);
            System.out.println("Created voucher: " + voucher);
            
            VoucherResponse response = convertToResponse(voucher);
            System.out.println("Converted response: " + response);
            
            ApiResponse<VoucherResponse> apiResponse = ApiResponse.success(response, "Tạo voucher thành công");
            System.out.println("Final API response: " + apiResponse);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
        } catch (IllegalArgumentException e) {
            // Lỗi nghiệp vụ (mã trùng, dữ liệu không hợp lệ) -> 400
            System.out.println("BUSINESS ERROR in createVoucher: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            System.out.println("ERROR in createVoucher: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("Đã xảy ra lỗi hệ thống. Vui lòng thử lại sau."));
        }
    }

    // update voucher
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<VoucherResponse>> updateVoucher(@PathVariable Long id, @Valid @RequestBody VoucherUpdateRequest request) {
        Voucher voucher = voucherService.updateVoucher(id, request);
        VoucherResponse response = convertToResponse(voucher);

        return ResponseEntity.ok(ApiResponse.success(response, "Cập nhật voucher thành công"));
    }

    // Xóa voucher
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteVoucher(@PathVariable Long id) {
        voucherService.deleteVoucher(id);
        return ResponseEntity.ok(ApiResponse.success(null,"Xóa voucher thành công"));
    }

    // Tái kích hoạt voucher
    @PostMapping("/{id}/reactivate")
    public ResponseEntity<ApiResponse<VoucherResponse>> reactivateVoucher(
            @PathVariable Long id,
            @RequestParam(required = false) Integer soNgayGiaHan) {
        try {
            Voucher voucher = voucherService.reactivateVoucher(id, soNgayGiaHan);
            VoucherResponse response = convertToResponse(voucher);
            return ResponseEntity.ok(ApiResponse.success(response, "Tái kích hoạt voucher thành công"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Đã xảy ra lỗi hệ thống. Vui lòng thử lại sau."));
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
            .trangThai(voucher.getTrangThai())
            .build();
    }
}
