package com.auro.auro.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collection;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
) throws ServletException, IOException {
    
    System.out.println("DEBUG - JwtAuthenticationFilter: Starting filter");
    
    final String authHeader = request.getHeader("Authorization");
    final String jwt;
    final String userEmail;

    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
        System.out.println("DEBUG - JwtAuthenticationFilter: No Bearer token");
        filterChain.doFilter(request, response);
        return;
    }

    jwt = authHeader.substring(7);
try {
    userEmail = jwtService.extractUsername(jwt);
} catch (io.jsonwebtoken.ExpiredJwtException |
         io.jsonwebtoken.MalformedJwtException |
         io.jsonwebtoken.security.SignatureException ex) {
    org.springframework.security.core.context.SecurityContextHolder.clearContext();
    filterChain.doFilter(request, response);
    return;
} catch (Exception ex) {
    org.springframework.security.core.context.SecurityContextHolder.clearContext();
    filterChain.doFilter(request, response);
    return;
}
System.out.println("DEBUG - JwtAuthenticationFilter: userEmail = " + userEmail);

    if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
        try {
            System.out.println("DEBUG - JwtAuthenticationFilter: Loading user details");
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);
            
            System.out.println("DEBUG - JwtAuthenticationFilter: userDetails loaded = " + userDetails.getUsername());
            
            if (jwtService.isTokenValid(jwt, userDetails)) {
                System.out.println("DEBUG - JwtAuthenticationFilter: Token is valid, extracting authorities");
                Collection<? extends GrantedAuthority> authorities = jwtService.extractAuthorities(jwt);
                
                System.out.println("DEBUG - JwtAuthenticationFilter: authorities = " + authorities);
                
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        authorities
                );
                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );
                SecurityContextHolder.getContext().setAuthentication(authToken);
                
                System.out.println("DEBUG - JwtAuthenticationFilter: Authentication set successfully");
            } else {
                System.out.println("DEBUG - JwtAuthenticationFilter: Token is invalid");
            }
        } catch (Exception e) {
            System.out.println("DEBUG - JwtAuthenticationFilter ERROR: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    filterChain.doFilter(request, response);
}
}