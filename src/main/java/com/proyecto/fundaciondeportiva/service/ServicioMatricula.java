package com.proyecto.fundaciondeportiva.service;

import com.proyecto.fundaciondeportiva.dto.request.MatriculaRequestDTO;
import com.proyecto.fundaciondeportiva.dto.response.MatriculaResponseDTO;
import com.proyecto.fundaciondeportiva.model.enums.EstadoMatricula;

import java.util.List;

public interface ServicioMatricula {

    // Operaciones de Alumno
    MatriculaResponseDTO matricularseEnSeccion(Long alumnoId, MatriculaRequestDTO request);

    MatriculaResponseDTO retirarseDeSeccion(Long alumnoId, Long seccionId);

    List<MatriculaResponseDTO> listarMisMatriculas(Long alumnoId);

    List<MatriculaResponseDTO> listarMisMatriculasActivas(Long alumnoId);

    // Operaciones de Profesor
    List<MatriculaResponseDTO> listarAlumnosDeSeccion(Long seccionId);

    List<MatriculaResponseDTO> listarAlumnosActivosDeSeccion(Long seccionId);

    // Operaciones de Administrador
    List<MatriculaResponseDTO> listarTodasLasMatriculas();

    MatriculaResponseDTO obtenerMatriculaPorId(Long id);

    MatriculaResponseDTO actualizarEstadoMatricula(Long id, EstadoMatricula nuevoEstado);

    MatriculaResponseDTO asignarCalificacion(Long id, Double calificacion);

    void eliminarMatricula(Long id);
}