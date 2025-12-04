package com.auro.auro.controller;

import com.auro.auro.dto.request.BulkStatusUpdateRequest;
import com.auro.auro.service.NguoiDungService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class UserBulkAdminController {

    private final NguoiDungService nguoiDungService;

    @PutMapping("/bulk-status")
    @PreAuthorize("hasAnyRole('STF','ADM')")
    public ResponseEntity<Void> bulkUpdateStatus(@RequestBody BulkStatusUpdateRequest request) {
        nguoiDungService.bulkUpdateStatus(request.getIds(), request.getTrangThai());
        return ResponseEntity.noContent().build();
    }
}


