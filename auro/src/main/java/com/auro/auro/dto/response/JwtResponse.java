package com.auro.auro.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JwtResponse {

    private String accessToken;
    private String tokenType = "Bearer";
    private Long expiresIn;
    
    private UserInfoResponse user;
}
