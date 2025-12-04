package com.proyecto.fundaciondeportiva.service;

import com.proyecto.fundaciondeportiva.dto.request.SeccionRequestDTO;
import com.proyecto.fundaciondeportiva.dto.response.SeccionResponseDTO;
import com.proyecto.fundaciondeportiva.model.enums.NivelAcademico;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

public interface ServicioSeccion {

    List<SeccionResponseDTO> listarTodasLasSecciones();
    List<SeccionResponseDTO> listarSeccionesActivas();
    SeccionResponseDTO obtenerSeccionPorId(Long id);

    SeccionResponseDTO crearSeccion(SeccionRequestDTO request);
    SeccionResponseDTO actualizarSeccion(Long id, SeccionRequestDTO request);

    void eliminarSeccion(Long id);
    void desactivarSeccion(Long id);
    void activarSeccion(Long id);

    List<SeccionResponseDTO> listarSeccionesPorCurso(Long cursoId);
    List<SeccionResponseDTO> listarSeccionesPorProfesor(Long profesorId);
    List<SeccionResponseDTO> listarSeccionesPorDniProfesor(String dni);

    List<SeccionResponseDTO> listarSeccionesConCupo();
    List<SeccionResponseDTO> listarSeccionesPorNivel(NivelAcademico nivel);

    // NUEVO MÉTODO
    List<SeccionResponseDTO> listarSeccionesPorHorario(DayOfWeek dia, LocalTime hora);
}