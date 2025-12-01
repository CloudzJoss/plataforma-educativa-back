package com.proyecto.fundaciondeportiva.controller;

import com.proyecto.fundaciondeportiva.dto.request.SesionUpdateDTO;
import com.proyecto.fundaciondeportiva.dto.response.SesionDTO;
import com.proyecto.fundaciondeportiva.service.ServicioSesion;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sesiones")
public class SesionController {

    @Autowired
    private ServicioSesion servicioSesion;

    // Endpoint para que el profesor actualice Tema y Resultado
    @PatchMapping("/{id}/info")
    @PreAuthorize("hasRole('PROFESOR')")
    public ResponseEntity<SesionDTO> actualizarInfoSesion(
            @PathVariable Long id,
            @Valid @RequestBody SesionUpdateDTO request) {

        SesionDTO response = servicioSesion.actualizarInfoSesion(id, request);
        return ResponseEntity.ok(response);
    }
}