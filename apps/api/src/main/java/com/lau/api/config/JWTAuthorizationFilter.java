package com.lau.api.config;

import com.lau.api.exceptions.TokenExpiredException;
import com.lau.api.exceptions.UnauthorizedException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.ObjectMapper;

public class JWTAuthorizationFilter extends OncePerRequestFilter {

    private final TokenUtils tokenUtils;

    public JWTAuthorizationFilter(TokenUtils tokenUtils) {
        this.tokenUtils = tokenUtils;
    }
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return SecurityConfig.RUTA_LOGIN.equals(request.getServletPath());
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
        ProblemDetail p = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, mensaje);
        p.setProperty("code", error);                       // TOKEN_EXPIRED / UNAUTHORIZED
        response.setStatus(HttpStatus.UNAUTHORIZED.value()); // N-26: antes salía 200
        response.setCharacterEncoding("UTF-8");              // N-26: tildes del mensaje
        if (expirado) {
            response.setHeader("X-Token-Expired", "true");   // expuesto en CORS para que el front renueve sesión
        }
        response.setContentType("application/problem+json");
        MAPPER.writeValue(response.getWriter(), p);
    }
}