package com.proyecto.fundaciondeportiva.repository;

import com.proyecto.fundaciondeportiva.model.entity.Matricula;
import com.proyecto.fundaciondeportiva.model.enums.EstadoMatricula;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MatriculaRepository extends JpaRepository<Matricula, Long> {

    /**
     * Busca todas las matrículas de un alumno específico
     */
    List<Matricula> findByAlumnoId(Long alumnoId);

    /**
     * Busca todas las matrículas de una sección específica
     */
    List<Matricula> findBySeccionId(Long seccionId);

    /**
     * Busca matrículas activas de un alumno
     */
    List<Matricula> findByAlumnoIdAndEstado(Long alumnoId, EstadoMatricula estado);

    /**
     * Busca matrículas activas de una sección
     */
    List<Matricula> findBySeccionIdAndEstado(Long seccionId, EstadoMatricula estado);

    /**
     * Verifica si un alumno ya está matriculado en una sección
     */
    boolean existsByAlumnoIdAndSeccionId(Long alumnoId, Long seccionId);

    /**
     * Busca una matrícula específica de un alumno en una sección
     */
    Optional<Matricula> findByAlumnoIdAndSeccionId(Long alumnoId, Long seccionId);

    /**
     * Cuenta las matrículas activas de una sección
     */
    @Query("SELECT COUNT(m) FROM Matricula m WHERE m.seccion.id = :seccionId AND m.estado = 'ACTIVA'")
    long countMatriculasActivasBySeccionId(@Param("seccionId") Long seccionId);

    /**
     * Busca todas las matrículas de las secciones de un profesor
     */
    @Query("SELECT m FROM Matricula m WHERE m.seccion.profesor.id = :profesorId")
    List<Matricula> findByProfesorId(@Param("profesorId") Long profesorId);

    /**
     * Busca matrículas por DNI del alumno
     */
    @Query("SELECT m FROM Matricula m WHERE m.alumno.perfilAlumno.dni = :dni")
    List<Matricula> findByAlumnoDni(@Param("dni") String dni);
}