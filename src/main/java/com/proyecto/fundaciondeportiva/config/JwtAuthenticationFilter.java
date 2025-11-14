package com.proyecto.fundaciondeportiva.config;

import com.proyecto.fundaciondeportiva.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie; // 1. IMPORTAR Cookie
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
import java.util.Arrays; // 2. IMPORTAR Arrays (para helper de cookie)

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

        // --- 3. INICIO: LÓGICA DE COOKIE ---

        // 3a. Extraer el token de la cookie en lugar del header
        final String jwt = getJwtFromCookies(request);
        final String userEmail;

        // 3b. Verifica si el token (de la cookie) es nulo
        if (jwt == null) {
            filterChain.doFilter(request, response); // Si no hay token, pasa al siguiente filtro
            return;
        }

        // --- FIN: LÓGICA DE COOKIE ---

        // (La lógica de validación del token de 4 a 9 es idéntica)
        try {
            // 4. Extrae el email del token
            userEmail = jwtService.extractUsername(jwt);

            // 5. Verifica si el email no es nulo y si el usuario aún no está autenticado
            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                // 6. Carga los detalles del usuario desde la base de datos
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

                // 7. Valida el token
                if (jwtService.validateToken(jwt, userDetails)) {
                    // 8. Si el token es válido, crea un objeto de autenticación
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );
                    // 9. Establece la autenticación en el contexto de seguridad
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
            // 10. Pasa la petición al siguiente filtro
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            // (Manejo de excepciones sin cambios)
            logger.warn("Error al procesar el token JWT: {}" + e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Token JWT inválido o expirado");
            return;
        }
    }

    // --- 4. NUEVO MÉTODO HELPER ---
    /**
     * Extrae el JWT de la cookie "jwt_token" en la petición.
     * @param request La petición HTTP.
     * @return El string del token o null si no se encuentra.
     */
    private String getJwtFromCookies(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        // Busca la cookie con el nombre específico
        return Arrays.stream(cookies)
                .filter(cookie -> "jwt_token".equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }
}