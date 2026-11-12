package com.lau.api.config;

import com.lau.api.exceptions.TokenExpiredException;
import com.lau.api.exceptions.UnauthorizedException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

public class JWTAuthorizationFilter extends OncePerRequestFilter {

    private final TokenUtils tokenUtils;

    public JWTAuthorizationFilter(TokenUtils tokenUtils) {
        this.tokenUtils = tokenUtils;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return request.getServletPath().startsWith("/auth/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            chain.doFilter(request, response);     // sin token: decide SecurityConfig (401 si la ruta es privada)
            return;
        }

        // El try envuelve SOLO la validación del token, no el resto de la petición
        try {
            String token = header.substring("Bearer ".length());
            SecurityContextHolder.getContext().setAuthentication(tokenUtils.getAuthentication(token));
        } catch (TokenExpiredException e) {
            responder401(response, "TOKEN_EXPIRED", e.getMessage(), true);
            return;
        } catch (UnauthorizedException e) {
            responder401(response, "UNAUTHORIZED", e.getMessage(), false);
            return;
        }

        chain.doFilter(request, response);
    }

    private static void responder401(HttpServletResponse response, String error, String mensaje, boolean expirado)
            throws IOException {
        SecurityContextHolder.clearContext();
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        if (expirado) {
            response.setHeader("X-Token-Expired", "true");
        }
        response.getWriter().write("{\"error\":\"" + error + "\",\"message\":\"" + mensaje + "\"}");
    }
}