package com.proyecto.fundaciondeportiva.config;

import com.proyecto.fundaciondeportiva.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String jwt = getJwtFromCookies(request);
        final String userEmail;

        if (jwt == null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            userEmail = jwtService.extractUsername(jwt);

            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

                if (jwtService.validateToken(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );
                    SecurityContextHolder.getContext().setAuthentication(authToken);

                    // --- 🔄 LÓGICA DE SESIÓN DESLIZANTE (SLIDING SESSION) ---
                    // Como el usuario hizo una actividad (petición) y el token es válido,
                    // generamos un NUEVO token y actualizamos la cookie para darle 15 minutos más.

                    String newToken = jwtService.generateToken(userDetails);

                    Cookie newCookie = new Cookie("jwt_token", newToken);
                    newCookie.setHttpOnly(true);
                    newCookie.setSecure(true);
                    newCookie.setPath("/");
                    newCookie.setMaxAge(15 * 60); // 15 minutos más de vida

                    response.addCookie(newCookie);
                    // -------------------------------------------------------
                }
            }
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            logger.warn("Error al procesar el token JWT: {}" + e.getMessage());
            // Si el token expira o es inválido, limpiamos la cookie
            Cookie cleanCookie = new Cookie("jwt_token", null);
            cleanCookie.setPath("/");
            cleanCookie.setMaxAge(0);
            response.addCookie(cleanCookie);

            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            // No escribimos cuerpo para no romper el flujo JSON si el frontend no lo espera
            // o puedes usar response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
        }
    }

    private String getJwtFromCookies(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        return Arrays.stream(cookies)
                .filter(cookie -> "jwt_token".equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }
}