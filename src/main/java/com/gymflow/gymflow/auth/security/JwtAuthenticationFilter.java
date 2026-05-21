package com.gymflow.gymflow.auth.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    private final CustomUserDetailsService userDetailsService;

    public JwtAuthenticationFilter(
            JwtUtil jwtUtil,
            @Lazy CustomUserDetailsService userDetailsService
    ) {

        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String path = request.getServletPath();

        log.info("Incoming Request Path -> {}", path);

        // =====================================================
        // SKIP JWT FOR PUBLIC ROUTES
        // =====================================================

        if (
                path.startsWith("/api/auth/") ||
                        path.equals("/api/members/join") ||
                        path.startsWith("/api/gyms/public/") ||
                        path.startsWith("/api/attendance/scan/") ||
                        path.equals("/api/attendance/toggle") ||
                        path.startsWith("/api/dashboard/") ||
                        path.startsWith("/v3/api-docs") ||
                        path.startsWith("/swagger-ui") ||
                        path.startsWith("/webjars")
        ) {

            log.info(
                    "Skipping JWT authentication for public route -> {}",
                    path
            );

            filterChain.doFilter(request, response);

            return;
        }

        // =====================================================
        // EXTRACT AUTH HEADER
        // =====================================================

        final String authHeader =
                request.getHeader("Authorization");

        // =====================================================
        // NO AUTH HEADER
        // =====================================================

        if (
                authHeader == null ||
                        !authHeader.startsWith("Bearer ")
        ) {

            log.warn(
                    "No Authorization header found for protected route -> {}",
                    path
            );

            filterChain.doFilter(request, response);

            return;
        }

        // =====================================================
        // EXTRACT TOKEN
        // =====================================================

        final String jwtToken =
                authHeader.substring(7);

        try {

            // =====================================================
            // VALIDATE TOKEN
            // =====================================================

            if (!jwtUtil.validateToken(jwtToken)) {

                log.warn("JWT validation failed");

                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

                response.setContentType("application/json");

                response.getWriter().write("""
                        {
                            "success": false,
                            "message": "Invalid JWT token."
                        }
                        """);

                return;
            }

            // =====================================================
            // EXTRACT EMAIL
            // =====================================================

            String email = jwtUtil.extractEmail(jwtToken);

            log.info("JWT email extracted -> {}", email);

            // =====================================================
            // AUTHENTICATION NOT SET
            // =====================================================

            if (
                    email != null &&
                            SecurityContextHolder
                                    .getContext()
                                    .getAuthentication() == null
            ) {

                var userDetails =
                        userDetailsService
                                .loadUserByUsername(email);

                var authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );

                authentication.setDetails(
                        new WebAuthenticationDetailsSource()
                                .buildDetails(request)
                );

                SecurityContextHolder
                        .getContext()
                        .setAuthentication(authentication);

                log.info(
                        "JWT authentication success for -> {}",
                        email
                );
            }

        } catch (ExpiredJwtException ex) {

            log.error("JWT token expired -> {}", ex.getMessage());

            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

            response.setContentType("application/json");

            response.getWriter().write("""
                    {
                        "success": false,
                        "message": "JWT token expired."
                    }
                    """);

            return;

        } catch (JwtException ex) {

            log.error("JWT exception -> {}", ex.getMessage());

            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

            response.setContentType("application/json");

            response.getWriter().write("""
                    {
                        "success": false,
                        "message": "Invalid JWT token."
                    }
                    """);

            return;

        } catch (Exception ex) {

            log.error(
                    "Could not authenticate JWT user",
                    ex
            );

            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

            response.setContentType("application/json");

            response.getWriter().write("""
                    {
                        "success": false,
                        "message": "Authentication failed."
                    }
                    """);

            return;
        }

        filterChain.doFilter(request, response);
    }
}