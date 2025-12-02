package com.auro.auro.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserDetailsService userDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints - ai cũng truy cập được
                        .requestMatchers("/api/auth/register").permitAll()
                        .requestMatchers("/api/auth/login").permitAll()
                        .requestMatchers("/api/auth/logout").permitAll()
                        .requestMatchers("/api/auth/forgot-password").permitAll()
                        .requestMatchers("/api/auth/reset-password").permitAll()
                        .requestMatchers("/api/admin/register-first-admin").permitAll() // Tạo admin đầu tiên
                        // .requestMatchers("/api/auth/**").permitAll()

                        .requestMatchers(HttpMethod.GET, "/api/san-pham").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/san-pham/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/danh-muc").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/danh-muc/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/hinh-anh/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/kich-co/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/mau-sac/**").permitAll() // ✅ Add this line
                        .requestMatchers(HttpMethod.GET, "/api/chat-lieu/**").permitAll() // ✅ Add this line for materials
                        // Static uploaded files
                        .requestMatchers("/files/**").permitAll()
                        .requestMatchers("/uploads/**").permitAll()

                        // Shipping API - GHN (public access)
                        .requestMatchers("/api/shipping/**").permitAll()

                        // API TEST (DÙNG XONG NHỚ XÓA)
                        .requestMatchers("/api/test/**").permitAll()

                        // Guest endpoints - khách vãng lai
                        .requestMatchers(HttpMethod.POST, "/api/don-hang/khach").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/don-hang/theo-doi/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/khach-hang/don-hang/guest-checkout").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/khach-hang/don-hang/**").permitAll()
                        .requestMatchers("/api/payment/**").permitAll()

                        // Customer endpoints - khách đã đăng ký
                        .requestMatchers("/api/auth/me").hasAnyRole("CUS", "STF", "ADM")
                        .requestMatchers("/api/auth/upload-avatar").hasAnyRole("CUS", "STF", "ADM")
                        .requestMatchers(HttpMethod.POST, "/api/don-hang").hasAnyRole("CUS", "STF", "ADM")
                        .requestMatchers(HttpMethod.GET, "/api/don-hang/cua-toi").hasAnyRole("CUS", "STF", "ADM")
                        .requestMatchers("/api/gio-hang/**").permitAll()

                        // Voucher
                        .requestMatchers(HttpMethod.GET, "/api/phieu-giam-gia/co-san").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/phieu-giam-gia/ap-dung").hasAnyRole("CUS", "STF", "ADM")
                        .requestMatchers(HttpMethod.POST, "/api/phieu-giam-gia/kiem-tra")
                        .hasAnyRole("CUS", "STF", "ADM")

                        // Thông tin hồ sơ địa chỉ
                        .requestMatchers("/api/ho-so/**").hasAnyRole("CUS", "STF", "ADM")
                        .requestMatchers("/api/dia-chi/**").hasAnyRole("CUS", "STF", "ADM")

                        // Staff + admin endpoints
                        // CRUD sản phẩm
                        // Cho phép PUT cho người dùng đã đăng nhập (CUS/STF/ADM) để thuận tiện phát
                        // triển
                        .requestMatchers(HttpMethod.POST, "/api/san-pham").hasAnyRole("STF", "ADM")
                        .requestMatchers(HttpMethod.PUT, "/api/san-pham/**").hasAnyRole("CUS", "STF", "ADM")
                        .requestMatchers(HttpMethod.DELETE, "/api/san-pham/**").hasAnyRole("STF", "ADM")

                        // upload/delete hình ảnh sản phẩm
                        .requestMatchers(HttpMethod.POST, "/api/hinh-anh/**").hasAnyRole("STF", "ADM")
                        .requestMatchers(HttpMethod.DELETE, "/api/hinh-anh/**").hasAnyRole("STF", "ADM")

                        // crud danh mục - chỉ admin
                        .requestMatchers(HttpMethod.POST, "/api/danh-muc/**").hasRole("ADM")
                        .requestMatchers(HttpMethod.PUT, "/api/danh-muc/**").hasRole("ADM")
                        .requestMatchers(HttpMethod.DELETE, "/api/danh-muc/**").hasRole("ADM")

                        // quản lý đơn hàng
                        .requestMatchers("/api/don-hang/quan-ly/**").hasAnyRole("STF", "ADM")

                        // quản lý voucher
                        .requestMatchers("/api/phieu-giam-gia/quan-ly/**").hasAnyRole("STF", "ADM")

                        // quản lý tồn kho
                        .requestMatchers("/api/ton-kho/**").hasAnyRole("STF", "ADM")

                        // ADMIN
                        .requestMatchers("/api/admin/**").hasRole("ADM")
                        // Quản lý người dùng: cho phép STF/ADM truy cập, hạn chế hành vi bằng
                        // @PreAuthorize ở controller
                        .requestMatchers("/api/nguoi-dung/**").hasAnyRole("STF", "ADM")
                        .requestMatchers("/api/thong-ke/**").hasAnyRole("STF", "ADM")
                        .requestMatchers("/api/cai-dat/**").hasRole("ADM")
                        .requestMatchers("/api/vai-tro/**").hasRole("ADM")

                        .requestMatchers("/api/don-hang/**").permitAll()

                        // All other requests need authentication (bao gồm /api/auth/me)
                        .anyRequest().authenticated())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}