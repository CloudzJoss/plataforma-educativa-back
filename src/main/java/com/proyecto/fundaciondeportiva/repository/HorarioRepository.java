package com.proyecto.fundaciondeportiva.repository;

import com.proyecto.fundaciondeportiva.model.entity.Horario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

/**
 * Repositorio para la entidad Horario.
 * Proporciona métodos para consultar horarios con validaciones de cruce.
 */
@Repository
public interface HorarioRepository extends JpaRepository<Horario, Long> {

    /**
     * Encuentra todos los horarios de una sección específica.
     * @param seccionId ID de la sección
     * @return Lista de horarios de la sección
     */
    @Query("SELECT h FROM Horario h WHERE h.seccion.id = :seccionId ORDER BY h.diaSemana, h.horaInicio")
    List<Horario> findBySeccionId(@Param("seccionId") Long seccionId);

    /**
     * Encuentra todos los horarios de un profesor en todas sus secciones activas.
     * @param profesorId ID del profesor
     * @return Lista de horarios del profesor
     */
    @Query("SELECT h FROM Horario h WHERE h.seccion.profesor.id = :profesorId " +
            "ORDER BY h.diaSemana, h.horaInicio")
    List<Horario> findByProfesorId(@Param("profesorId") Long profesorId);

    /**
     * Encuentra todos los horarios de un alumno en sus secciones activas.
     * Se usa para validar cruces de horarios al matricularse.
     *
     * @param alumnoId ID del alumno
     * @return Lista de horarios ocupados del alumno
     */
    @Query("SELECT h FROM Horario h " +
            "WHERE h.seccion IN (" +
            "  SELECT m.seccion FROM Matricula m " +
            "  WHERE m.alumno.id = :alumnoId AND m.estado = 'ACTIVA'" +
            ") " +
            "ORDER BY h.diaSemana, h.horaInicio")
    List<Horario> findHorariosDeAlumno(@Param("alumnoId") Long alumnoId);

    /**
     * Verifica si existe un cruce de horarios para un profesor.
     * Se usa al crear o actualizar una sección.
     *
     * Detecta si el profesor ya tiene una clase asignada en:
     * - El mismo día de la semana
     * - En un rango horario que se solapa
     * - En una sección diferente a la que se está ignorando
     *
     * @param profesorId ID del profesor
     * @param diaSemana Día de la semana a validar
     * @param horaInicio Hora de inicio del nuevo horario
     * @param horaFin Hora de fin del nuevo horario
     * @param seccionIdIgnorar ID de la sección a ignorar (para updates, pass -1 para creates)
     * @return true si existe cruce, false si no hay conflicto
     */
    @Query("SELECT COUNT(h) > 0 FROM Horario h " +
            "WHERE h.seccion.profesor.id = :profesorId " +
            "AND h.diaSemana = :diaSemana " +
            "AND h.horaInicio < :horaFin " +
            "AND h.horaFin > :horaInicio " +
            "AND h.seccion.id != :seccionIdIgnorar")
    boolean existeCruceProfesor(@Param("profesorId") Long profesorId,
                                @Param("diaSemana") DayOfWeek diaSemana,
                                @Param("horaInicio") LocalTime horaInicio,
                                @Param("horaFin") LocalTime horaFin,
                                @Param("seccionIdIgnorar") Long seccionIdIgnorar);

    /**
     * Verifica si existe un cruce de horarios para un alumno.
     * Se usa al matricularse en una sección.
     *
     * Detecta si el alumno ya tiene una clase en:
     * - El mismo día de la semana
     * - En un rango horario que se solapa
     *
     * @param alumnoId ID del alumno
     * @param diaSemana Día de la semana a validar
     * @param horaInicio Hora de inicio del nuevo horario
     * @param horaFin Hora de fin del nuevo horario
     * @return true si existe cruce, false si no hay conflicto
     */
    @Query("SELECT COUNT(h) > 0 FROM Horario h " +
            "WHERE h.seccion IN (" +
            "  SELECT m.seccion FROM Matricula m " +
            "  WHERE m.alumno.id = :alumnoId AND m.estado = 'ACTIVA'" +
            ") " +
            "AND h.diaSemana = :diaSemana " +
            "AND h.horaInicio < :horaFin " +
            "AND h.horaFin > :horaInicio")
    boolean existeCruceAlumno(@Param("alumnoId") Long alumnoId,
                              @Param("diaSemana") DayOfWeek diaSemana,
                              @Param("horaInicio") LocalTime horaInicio,
                              @Param("horaFin") LocalTime horaFin);

    /**
     * Elimina todos los horarios de una sección.
     * Se usa cuando se elimina una sección.
     *
     * @param seccionId ID de la sección
     */
    @Query("DELETE FROM Horario h WHERE h.seccion.id = :seccionId")
    void deleteBySeccionId(@Param("seccionId") Long seccionId);

    /**
     * Cuenta cuántos horarios tiene una sección.
     *
     * @param seccionId ID de la sección
     * @return Cantidad de horarios
     */
    @Query("SELECT COUNT(h) FROM Horario h WHERE h.seccion.id = :seccionId")
    long countBySeccionId(@Param("seccionId") Long seccionId);
}