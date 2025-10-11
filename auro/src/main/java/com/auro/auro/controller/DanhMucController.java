package com.auro.auro.controller;

import com.auro.auro.dto.response.DanhMucResponse;
import com.auro.auro.model.DanhMuc;
import com.auro.auro.repository.DanhMucRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/categories")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class DanhMucController {

    private final DanhMucRepository danhMucRepository;

    @GetMapping
    public ResponseEntity<List<DanhMucResponse>> getAll() {
        List<DanhMucResponse> list = danhMucRepository.findAll().stream()
                .map(this::map)
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    @GetMapping("/page")
    public ResponseEntity<Page<DanhMucResponse>> page(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<DanhMucResponse> res = danhMucRepository.findAll(PageRequest.of(page, size))
                .map(this::map);
        return ResponseEntity.ok(res);
    }

    private DanhMucResponse map(DanhMuc dm) {
        return new DanhMucResponse(dm.getId(), dm.getTen(), dm.getSlug());
    }
}
