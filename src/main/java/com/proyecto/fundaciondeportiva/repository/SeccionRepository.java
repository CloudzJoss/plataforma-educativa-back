package com.proyecto.fundaciondeportiva.repository;

import com.proyecto.fundaciondeportiva.model.entity.Seccion;
import com.proyecto.fundaciondeportiva.model.enums.NivelAcademico;
import com.proyecto.fundaciondeportiva.model.enums.Turno;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface SeccionRepository extends JpaRepository<Seccion, Long> {

    Optional<Seccion> findByCodigo(String codigo);

    boolean existsByCodigo(String codigo);

    List<Seccion> findByActivaTrue();

    List<Seccion> findByCursoId(Long cursoId);

    List<Seccion> findByProfesorId(Long profesorId);

    List<Seccion> findByTurno(Turno turno);

    List<Seccion> findByNivelSeccion(NivelAcademico nivel);

    List<Seccion> findByNivelSeccionAndGradoSeccion(NivelAcademico nivel, String grado);

    @Query("SELECT s FROM Seccion s WHERE s.activa = true AND s.fechaInicio <= :fecha AND s.fechaFin >= :fecha")
    List<Seccion> findSeccionesActivasEnFecha(@Param("fecha") LocalDate fecha);

    @Query("SELECT s FROM Seccion s WHERE s.activa = true AND SIZE(s.matriculas) < s.capacidad")
    List<Seccion> findSeccionesConCupoDisponible();

    @Query("SELECT s FROM Seccion s WHERE s.nivelSeccion = :nivel AND s.activa = true")
    List<Seccion> findByNivelAndActiva(@Param("nivel") NivelAcademico nivel);
}