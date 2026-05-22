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

    /**
     * 🟢 NATIVE SPRING SECURITY BYPASS ROUTER
     * Prevents this filter from executing entirely for public resources,
     * documentation endpoints, and the Actuator container health checks.
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getServletPath();

        boolean skip = path.startsWith("/api/auth/") ||
                path.equals("/api/members/join") ||
                path.startsWith("/api/gyms/public/") ||
                path.startsWith("/api/attendance/scan/") ||
                path.equals("/api/attendance/toggle") ||
                path.startsWith("/api/dashboard/") ||
                path.startsWith("/actuator") ||          // Allows Docker health checks to pass cleanly
                path.startsWith("/v3/api-docs") ||
                path.startsWith("/swagger-ui") ||
                path.startsWith("/webjars");

        if (skip) {
            log.info("Bypassing JWT Authentication Filter for path -> {}", path);
        }

        return skip;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String path = request.getServletPath();
        log.info("Processing Protected Route Authentication Path -> {}", path);

        // =====================================================
        // EXTRACT AUTH HEADER
        // =====================================================
        final String authHeader = request.getHeader("Authorization");

        // =====================================================
        // NO AUTH HEADER FOUND
        // =====================================================
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("Missing or malformed Authorization header on protected resource -> {}", path);

            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("""
                    {
                        "success": false,
                        "message": "Access Denied: Missing Authorization token."
                    }
                    """);
            return;
        }

        // =====================================================
        // EXTRACT TOKEN
        // =====================================================
        final String jwtToken = authHeader.substring(7);

        try {
            // =====================================================
            // VALIDATE TOKEN
            // =====================================================
            if (!jwtUtil.validateToken(jwtToken)) {
                log.warn("JWT validation failed for token signature");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("""
                        {
                            "success": false,
                            "message": "Invalid or tampered JWT token."
                        }
                        """);
                return;
            }

            // =====================================================
            // EXTRACT EMAIL / SUBJECT
            // =====================================================
            String email = jwtUtil.extractEmail(jwtToken);
            log.info("JWT email successfully parsed -> {}", email);

            // =====================================================
            // ESTABLISH SECURITY CONTEXT
            // =====================================================
            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                var userDetails = userDetailsService.loadUserByUsername(email);

                var authentication = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );

                authentication.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.info("Security context successfully established for user principal -> {}", email);
            }

        } catch (ExpiredJwtException ex) {
            log.error("JWT execution aborted: Token expired -> {}", ex.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("""
                    {
                        "success": false,
                        "message": "JWT token has expired."
                    }
                    """);
            return;

        } catch (JwtException ex) {
            log.error("JWT execution aborted: Parsing failure -> {}", ex.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("""
                    {
                        "success": false,
                        "message": "Malformed or invalid security token."
                    }
                    """);
            return;

        } catch (Exception ex) {
            log.error("Fallback Filter Interceptor Level Exception catch:", ex);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("""
                    {
                        "success": false,
                        "message": "Internal authentication processing failure."
                    }
                    """);
            return;
        }

        filterChain.doFilter(request, response);
    }
}