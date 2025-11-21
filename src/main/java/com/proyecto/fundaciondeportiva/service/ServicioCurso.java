package com.proyecto.fundaciondeportiva.service;

import com.proyecto.fundaciondeportiva.dto.request.CursoRequestDTO;
import com.proyecto.fundaciondeportiva.dto.response.CursoResponseDTO;
import com.proyecto.fundaciondeportiva.model.enums.NivelAcademico;

import java.util.List;

/**
 * Interfaz (Contrato) para el servicio de gestión de Cursos.
 * (Corregido SIN gradoDestino)
 */
public interface ServicioCurso {

    List<CursoResponseDTO> listarTodosLosCursos();

    CursoResponseDTO obtenerCursoPorId(Long id);

    List<CursoResponseDTO> listarCursosPorNivel(NivelAcademico nivel);

    CursoResponseDTO crearCurso(CursoRequestDTO request, String emailAdmin);

    CursoResponseDTO actualizarCurso(Long id, CursoRequestDTO request);

    void eliminarCurso(Long id);
}