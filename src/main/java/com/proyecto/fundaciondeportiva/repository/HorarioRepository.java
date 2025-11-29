package com.proyecto.fundaciondeportiva.repository;

import com.proyecto.fundaciondeportiva.model.entity.Horario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface HorarioRepository extends JpaRepository<Horario, Long> {

    /**
     * Verifica si un PROFESOR tiene choque de horario en otra sección activa.
     * Lógica: Mismo día y solapamiento de horas (Inicio < FinExistente Y Fin > InicioExistente).
     * Se excluye la sección actual (seccionIdIgnorar) para permitir ediciones.
     */
    @Query("SELECT COUNT(h) > 0 FROM Horario h " +
            "JOIN h.seccion s " +
            "WHERE s.profesor.id = :profesorId " +
            "AND s.activa = true " +
            "AND s.id <> :seccionIdIgnorar " +
            "AND h.diaSemana = :dia " +
            "AND h.horaInicio < :horaFin " +
            "AND h.horaFin > :horaInicio")
    boolean existeCruceProfesor(@Param("profesorId") Long profesorId,
                                @Param("dia") DayOfWeek dia,
                                @Param("horaInicio") LocalTime horaInicio,
                                @Param("horaFin") LocalTime horaFin,
                                @Param("seccionIdIgnorar") Long seccionIdIgnorar);

    /**
     * Obtiene todos los horarios de todas las secciones donde un ALUMNO
     * tiene una matrícula ACTIVA.
     */
    @Query("SELECT h FROM Horario h " +
            "JOIN h.seccion s " +
            "JOIN s.matriculas m " +
            "WHERE m.alumno.id = :alumnoId " +
            "AND m.estado = 'ACTIVA'")
    List<Horario> findHorariosDeAlumno(@Param("alumnoId") Long alumnoId);
}