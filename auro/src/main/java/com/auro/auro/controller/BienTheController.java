package com.auro.auro.controller;

import com.auro.auro.dto.request.VariantUpsertRequest;
import com.auro.auro.dto.response.InventoryItemResponse;
import com.auro.auro.dto.response.VariantResponse;
import com.auro.auro.service.BienTheService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bien-the")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class BienTheController {

    private final BienTheService bienTheService;

    /**
     * Lấy danh sách biến thể của sản phẩm
     */
    @GetMapping("/san-pham/{sanPhamId}")
    public ResponseEntity<List<VariantResponse>> getVariantsBySanPham(@PathVariable Long sanPhamId) {
        List<VariantResponse> variants = bienTheService.getVariantsBySanPhamId(sanPhamId);
        return ResponseEntity.ok(variants);
    }

    /**
     * Tạo hoặc cập nhật nhiều biến thể cho sản phẩm
     */
    @PostMapping("/san-pham/{sanPhamId}")
    public ResponseEntity<List<VariantResponse>> upsertVariants(
            @PathVariable Long sanPhamId,
            @RequestBody VariantUpsertRequest request) {
        List<VariantResponse> savedVariants = bienTheService.upsertVariants(sanPhamId, request);
        return ResponseEntity.ok(savedVariants);
    }

    /**
     * Xóa biến thể
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVariant(@PathVariable Long id) {
        bienTheService.deleteVariant(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Xóa tất cả biến thể của sản phẩm
     */
    @DeleteMapping("/san-pham/{sanPhamId}")
    public ResponseEntity<Void> deleteAllVariantsBySanPham(@PathVariable Long sanPhamId) {
        bienTheService.deleteAllVariantsBySanPham(sanPhamId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Cập nhật tồn kho của một biến thể
     */
    @PatchMapping("/{id}/stock")
    public ResponseEntity<VariantResponse> updateStock(
            @PathVariable Long id,
            @RequestParam Integer stock) {
        VariantResponse updated = bienTheService.updateStock(id, stock);
        return ResponseEntity.ok(updated);
    }

    /**
     * Lấy chi tiết một biến thể
     */
    @GetMapping("/{id}")
    public ResponseEntity<VariantResponse> getVariantById(@PathVariable Long id) {
        VariantResponse variant = bienTheService.getVariantById(id);
        return ResponseEntity.ok(variant);
    }

    /**
     * Lấy tất cả biến thể với thông tin sản phẩm (cho trang inventory)
     */
    @GetMapping("/inventory/all")
    public ResponseEntity<List<InventoryItemResponse>> getAllInventoryItems() {
        List<InventoryItemResponse> items = bienTheService.getAllInventoryItems();
        return ResponseEntity.ok(items);
    }
}
