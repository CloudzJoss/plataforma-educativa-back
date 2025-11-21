package com.proyecto.fundaciondeportiva.repository;

import com.proyecto.fundaciondeportiva.model.entity.Asistencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio para la entidad Asistencia. (NUEVO)
 */
@Repository
public interface AsistenciaRepository extends JpaRepository<Asistencia, Long> {

    /**
     * Busca todas las asistencias de una sesión de clase.
     * 🚨 NOTA: 'findBySesionId' debe estar en inglés.
     */
    List<Asistencia> findBySesionId(Long sesionId);

    /**
     * Busca todo el historial de asistencias de un alumno en una sección.
     * 🚨 NOTA: 'findByAlumnoIdAndSesion_SeccionId' debe estar en inglés.
     */
    List<Asistencia> findByAlumnoIdAndSesion_SeccionId(Long alumnoId, Long seccionId);
}