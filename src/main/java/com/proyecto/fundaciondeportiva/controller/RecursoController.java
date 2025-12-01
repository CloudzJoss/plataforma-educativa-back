package com.proyecto.fundaciondeportiva.controller;

import com.proyecto.fundaciondeportiva.dto.request.RecursoRequestDTO;
import com.proyecto.fundaciondeportiva.model.entity.Recurso;
import com.proyecto.fundaciondeportiva.model.entity.Sesion;
import com.proyecto.fundaciondeportiva.repository.RecursoRepository;
import com.proyecto.fundaciondeportiva.repository.SesionRepository;
import com.proyecto.fundaciondeportiva.service.impl.AzureBlobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/recursos")
public class RecursoController {

    @Autowired
    private AzureBlobService azureBlobService;

    @Autowired
    private RecursoRepository recursoRepository;

    @Autowired
    private SesionRepository sesionRepository;

    // Endpoint para subir recursos
    // Nota: 'consumes = MediaType.MULTIPART_FORM_DATA_VALUE' es clave para recibir archivos
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> crearRecurso(@ModelAttribute RecursoRequestDTO request) {
        try {
            String urlFinal = "";

            // 1. Determinar la URL (subir archivo o usar link externo)
            if (request.getArchivo() != null && !request.getArchivo().isEmpty()) {
                // Subir a Azure
                urlFinal = azureBlobService.subirArchivo(request.getArchivo());
            } else if (request.getUrlExterna() != null && !request.getUrlExterna().isEmpty()) {
                // Usar link proporcionado
                urlFinal = request.getUrlExterna();
            } else {
                return ResponseEntity.badRequest().body("Debe proporcionar un archivo o una URL externa");
            }

            // 2. Buscar la sesión
            Sesion sesion = sesionRepository.findById(request.getSesionId())
                    .orElseThrow(() -> new RuntimeException("Sesión no encontrada"));

            // 3. Guardar en BD
            Recurso recurso = Recurso.builder()
                    .titulo(request.getTitulo())
                    .url(urlFinal) // Guardamos la URL de Azure o el Link de YouTube
                    .tipoArchivo(request.getTipoArchivo())
                    .momento(request.getMomento())
                    .sesion(sesion)
                    .build();

            recursoRepository.save(recurso);

            return ResponseEntity.ok("Recurso creado exitosamente");

        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("Error al subir archivo: " + e.getMessage());
        }
    }
}