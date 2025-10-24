package com.proyecto.fundaciondeportiva.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
// Asegúrate de importar 'annotation.web.configurers.AbstractHttpConfigurer' si usas la lambda de CSRF
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer; // Para csrf(AbstractHttpConfigurer::disable)
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
// Ya no necesitamos @EnableMethodSecurity si todo es público
// @EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, AuthenticationProvider authenticationProvider) throws Exception {
        http
                // Deshabilita CSRF (recomendado para APIs stateless)
                .csrf(AbstractHttpConfigurer::disable) // Forma moderna de deshabilitar CSRF
                // --- CAMBIO PRINCIPAL AQUÍ ---
                // Configura las reglas de autorización
                .authorizeHttpRequests(auth -> auth
                        // Permite TODAS las peticiones sin autenticación
                        .anyRequest().permitAll()
                )
                // --- FIN DEL CAMBIO ---
                // Configura la gestión de sesiones como STATELESS (sin estado)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Define el proveedor de autenticación personalizado (aunque no se usará si todo es permitAll)
                .authenticationProvider(authenticationProvider);

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider(UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder);
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}

