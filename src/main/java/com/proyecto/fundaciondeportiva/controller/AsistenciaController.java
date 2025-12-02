package com.proyecto.fundaciondeportiva.controller;

import com.proyecto.fundaciondeportiva.dto.request.RegistroAsistenciaDTO;
import com.proyecto.fundaciondeportiva.dto.response.AsistenciaDTO;
import com.proyecto.fundaciondeportiva.service.ServicioAsistencia;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/asistencias")
public class AsistenciaController {

    @Autowired
    private ServicioAsistencia servicioAsistencia;

    // Obtener la "hoja de asistencia" de una sesión
    // Devuelve los alumnos y su estado (guardado o SIN_REGISTRAR)
    @GetMapping("/sesion/{sesionId}")
    @PreAuthorize("hasAnyRole('PROFESOR', 'ADMINISTRADOR')")
    public ResponseEntity<List<AsistenciaDTO>> obtenerAsistencia(@PathVariable Long sesionId) {
        List<AsistenciaDTO> asistencia = servicioAsistencia.obtenerAsistenciaDeSesion(sesionId);
        return ResponseEntity.ok(asistencia);
    }

    // Guardar o actualizar la asistencia de toda la clase
    @PostMapping("/guardar")
    @PreAuthorize("hasRole('PROFESOR')")
    public ResponseEntity<Void> guardarAsistencia(@Valid @RequestBody RegistroAsistenciaDTO request) {
        servicioAsistencia.registrarAsistenciaMasiva(request);
        return ResponseEntity.ok().build();
    }
}