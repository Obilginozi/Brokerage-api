package com.brokerage.api.security;

import com.brokerage.api.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.lang.NonNull;

import java.io.IOException;
import java.util.Collections;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private final JwtService jwtService;
    
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, 
                                  @NonNull HttpServletResponse response, 
                                  @NonNull FilterChain filterChain) throws ServletException, IOException {
        
        final String authHeader = request.getHeader("Authorization");
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }
        
        try {
            final String jwt = authHeader.substring(7);
            final String username = jwtService.extractUsername(jwt);
            final Boolean isAdmin = jwtService.extractIsAdmin(jwt);
            
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                if (jwtService.validateToken(jwt)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        username,
                        null,
                        Collections.singletonList(new SimpleGrantedAuthority(isAdmin ? "ROLE_ADMIN" : "ROLE_USER"))
                    );
                    
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    log.debug("User {} authenticated successfully", username);
                }
            }
        } catch (Exception e) {
            log.error("Error processing JWT token: {}", e.getMessage());
        }
        
        filterChain.doFilter(request, response);
    }
}
