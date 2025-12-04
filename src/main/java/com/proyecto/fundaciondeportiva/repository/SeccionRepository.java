package com.proyecto.fundaciondeportiva.repository;

import com.proyecto.fundaciondeportiva.model.entity.Seccion;
import com.proyecto.fundaciondeportiva.model.enums.NivelAcademico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SeccionRepository extends JpaRepository<Seccion, Long> {

    Optional<Seccion> findByCodigo(String codigo);

    boolean existsByCodigo(String codigo);

    List<Seccion> findByActivaTrue();

    List<Seccion> findByCursoId(Long cursoId);

    List<Seccion> findByProfesorId(Long profesorId);

    List<Seccion> findByNivelSeccion(NivelAcademico nivel);

    List<Seccion> findByNivelSeccionAndActivaTrue(NivelAcademico nivel);

    List<Seccion> findByNivelSeccionAndGradoSeccion(NivelAcademico nivel, String grado);

    @Query("SELECT s FROM Seccion s WHERE s.activa = true AND SIZE(s.matriculas) < s.capacidad")
    List<Seccion> findSeccionesConCupoDisponible();

    //NUEVO: Buscar secciones que se estén dictando en un día y hora específicos
    @Query("SELECT DISTINCT s FROM Seccion s JOIN s.horarios h " +
            "WHERE s.activa = true " +
            "AND h.diaSemana = :dia " +
            "AND h.horaInicio <= :hora " +
            "AND h.horaFin > :hora")
    List<Seccion> findByHorarioActivo(@Param("dia") DayOfWeek dia, @Param("hora") LocalTime hora);
}