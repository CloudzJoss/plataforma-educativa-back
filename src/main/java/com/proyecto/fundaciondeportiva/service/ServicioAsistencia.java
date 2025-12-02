package com.proyecto.fundaciondeportiva.service;
import com.proyecto.fundaciondeportiva.dto.request.RegistroAsistenciaDTO;
import com.proyecto.fundaciondeportiva.dto.response.AsistenciaDTO;
import java.util.List;

public interface ServicioAsistencia {
    List<AsistenciaDTO> obtenerAsistenciaDeSesion(Long sesionId);
    void registrarAsistenciaMasiva(RegistroAsistenciaDTO request);
    AsistenciaDTO obtenerMiAsistencia(Long sesionId, String emailAlumno);
    List<AsistenciaDTO> listarMisAsistenciasPorSeccion(Long seccionId, String emailAlumno);
}