package com.proyecto.fundaciondeportiva.repository;

import com.proyecto.fundaciondeportiva.model.entity.Asistencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AsistenciaRepository extends JpaRepository<Asistencia, Long> {

    // Buscar todas las asistencias de una sesión específica
    List<Asistencia> findBySesionId(Long sesionId);

    // Buscar la asistencia de un alumno específico en una sesión
    Optional<Asistencia> findBySesionIdAndAlumnoId(Long sesionId, Long alumnoId);

    @Query("SELECT a FROM Asistencia a WHERE a.alumno.id = :alumnoId AND a.sesion.seccion.id = :seccionId ORDER BY a.sesion.fecha ASC")
    List<Asistencia> findByAlumnoIdAndSeccionId(@Param("alumnoId") Long alumnoId, @Param("seccionId") Long seccionId);

}