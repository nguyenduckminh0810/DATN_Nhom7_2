package com.auro.auro.controller;

import com.auro.auro.model.ChatLieu;
import com.auro.auro.repository.ChatLieuRepository;
import com.auro.auro.repository.BienTheSanPhamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat-lieu")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ChatLieuController {

    private final ChatLieuRepository chatLieuRepository;
    private final BienTheSanPhamRepository bienTheSanPhamRepository;

    /**
     * Lấy tất cả chất liệu
     */
    @GetMapping
    public ResponseEntity<List<ChatLieu>> getAllMaterials() {
        List<ChatLieu> materials = chatLieuRepository.findAll();
        return ResponseEntity.ok(materials);
    }

    /**
     * Lấy chất liệu theo ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ChatLieu> getMaterialById(@PathVariable Long id) {
        return chatLieuRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Thêm chất liệu mới
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADM', 'STF')")
    public ResponseEntity<?> createMaterial(@RequestBody ChatLieu chatLieu) {
        // Kiểm tra xem chất liệu đã tồn tại chưa
        if (chatLieu.getTen() != null && !chatLieu.getTen().trim().isEmpty()) {
            if (chatLieuRepository.existsByTen(chatLieu.getTen().trim())) {
                return ResponseEntity.badRequest()
                        .body(java.util.Map.of("message", "Chất liệu đã tồn tại", "error", "Material already exists"));
            }
        } else {
            return ResponseEntity.badRequest()
                    .body(java.util.Map.of("message", "Tên chất liệu không được để trống", "error", "Material name is required"));
        }

        // Tạo mới chất liệu
        ChatLieu newChatLieu = new ChatLieu();
        newChatLieu.setTen(chatLieu.getTen().trim());
        ChatLieu saved = chatLieuRepository.save(newChatLieu);
        return ResponseEntity.ok(saved);
    }

    /**
     * Cập nhật chất liệu
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADM', 'STF')")
    public ResponseEntity<?> updateMaterial(@PathVariable Long id, @RequestBody ChatLieu chatLieu) {
        return chatLieuRepository.findById(id)
                .map(existing -> {
                    // Kiểm tra tên mới có trùng với chất liệu khác không
                    if (chatLieu.getTen() != null && !chatLieu.getTen().trim().isEmpty()) {
                        if (!existing.getTen().equals(chatLieu.getTen().trim())) {
                            if (chatLieuRepository.existsByTen(chatLieu.getTen().trim())) {
                                return ResponseEntity.badRequest()
                                        .body(java.util.Map.of("message", "Chất liệu đã tồn tại", "error", "Material already exists"));
                            }
                        }
                        existing.setTen(chatLieu.getTen().trim());
                        ChatLieu updated = chatLieuRepository.save(existing);
                        return ResponseEntity.ok(updated);
                    } else {
                        return ResponseEntity.badRequest()
                                .body(java.util.Map.of("message", "Tên chất liệu không được để trống", "error", "Material name is required"));
                    }
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Xóa chất liệu
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADM', 'STF')")
    public ResponseEntity<?> deleteMaterial(@PathVariable Long id) {
        return chatLieuRepository.findById(id)
                .map(chatLieu -> {
                    // Kiểm tra xem chất liệu có đang được sử dụng không
                    long count = bienTheSanPhamRepository.countByChatLieu_Id(id);
                    if (count > 0) {
                        return ResponseEntity.badRequest()
                                .body(java.util.Map.of(
                                    "message", 
                                    String.format("Không thể xóa chất liệu. Đang được sử dụng bởi %d biến thể sản phẩm", count),
                                    "error", 
                                    "Material is in use by product variants"
                                ));
                    }
                    
                    try {
                        chatLieuRepository.deleteById(id);
                        return ResponseEntity.ok(java.util.Map.of("message", "Đã xóa chất liệu thành công"));
                    } catch (Exception e) {
                        // Nếu có lỗi khác, trả về lỗi
                        return ResponseEntity.badRequest()
                                .body(java.util.Map.of("message", "Không thể xóa chất liệu: " + e.getMessage(), "error", e.getMessage()));
                    }
                })
                .orElse(ResponseEntity.notFound().build());
    }
}

