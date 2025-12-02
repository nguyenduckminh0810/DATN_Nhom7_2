package com.auro.auro.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ProfileUpdateRequest {

    @NotBlank(message = "Họ và tên không được để trống")
    private String fullName;
}


