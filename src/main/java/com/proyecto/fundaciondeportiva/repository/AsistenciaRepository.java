package com.proyecto.fundaciondeportiva.repository;

import com.proyecto.fundaciondeportiva.model.entity.Asistencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AsistenciaRepository extends JpaRepository<Asistencia, Long> {

    // Buscar todas las asistencias de una sesión específica
    List<Asistencia> findBySesionId(Long sesionId);

    // Buscar la asistencia de un alumno específico en una sesión
    Optional<Asistencia> findBySesionIdAndAlumnoId(Long sesionId, Long alumnoId);
}