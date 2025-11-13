package com.proyecto.fundaciondeportiva.service;

import io.jsonwebtoken.Claims; // Importa Claims
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function; // Importa Function

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String SECRET_KEY;

    // --- Generación de Token (Sin cambios) ---
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        // Puedes añadir claims personalizados aquí si lo necesitas
        // claims.put("userId", ((CustomUserDetails) userDetails).getId());
        return createToken(claims, userDetails.getUsername());
    }

    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject) // Email del usuario
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10)) // 10 horas
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // --- NUEVO: Métodos para Validación ---

    /**
     * Extrae el nombre de usuario (email) del token JWT.
     * @param token El token JWT.
     * @return El email del usuario.
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extrae la fecha de expiración del token JWT.
     * @param token El token JWT.
     * @return La fecha de expiración.
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Extrae un claim específico del token usando una función resolver.
     * @param token El token JWT.
     * @param claimsResolver La función para extraer el claim.
     * @param <T> El tipo del claim.
     * @return El valor del claim.
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Parsea el token y extrae todos los claims (cuerpo del token).
     * @param token El token JWT.
     * @return Los claims del token.
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Verifica si el token JWT ha expirado.
     * @param token El token JWT.
     * @return true si ha expirado, false en caso contrario.
     */
    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Valida si un token JWT es válido para un UserDetails específico.
     * Comprueba que el username coincida y que el token no haya expirado.
     * @param token El token JWT.
     * @param userDetails Los detalles del usuario cargados desde la base de datos.
     * @return true si el token es válido, false en caso contrario.
     */
    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }
}
