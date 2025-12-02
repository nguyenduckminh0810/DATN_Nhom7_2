package com.auro.auro.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfileResponse {

    private Long id;
    private String fullName;
    private String email;
    private String phone;
    private String avatar;
    private String role;
}


