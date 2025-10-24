package com.proyecto.fundaciondeportiva.controller;

import com.proyecto.fundaciondeportiva.dto.input.LoginInputDTO;
import com.proyecto.fundaciondeportiva.dto.output.LoginOutputDTO;
import com.proyecto.fundaciondeportiva.model.Usuario;
import com.proyecto.fundaciondeportiva.repository.UsuarioRepository;
import com.proyecto.fundaciondeportiva.service.JwtService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private JwtService jwtService;

    @PostMapping("/login")
    public ResponseEntity<LoginOutputDTO> login(@Valid @RequestBody LoginInputDTO loginInputDTO) {
        // 1. Spring Security autentica al usuario
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginInputDTO.getEmail(), loginInputDTO.getPassword())
        );
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        // 2. Buscamos los datos completos del usuario para la respuesta
        Usuario usuario = usuarioRepository.findByEmail(userDetails.getUsername()).orElseThrow();

        // 3. Generamos el token JWT
        String token = jwtService.generateToken(userDetails);

        // 4. Creamos y devolvemos la respuesta
        LoginOutputDTO response = LoginOutputDTO.builder()
                .token(token)
                .nombre(usuario.getNombre())
                .rol(usuario.getRol())
                .build();

        return ResponseEntity.ok(response);
    }
}