package com.proyecto.fundaciondeportiva.repository;

import com.proyecto.fundaciondeportiva.model.entity.Matricula;
import com.proyecto.fundaciondeportiva.model.entity.Seccion;
import com.proyecto.fundaciondeportiva.model.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio para la entidad Matricula. (NUEVO)
 */
@Repository
public interface MatriculaRepository extends JpaRepository<Matricula, Long> {

    /**
     * Verifica si ya existe una matrícula para un alumno en una sección.
     * 🚨 NOTA: 'existsBySeccionAndAlumno' debe estar en inglés.
     */
    boolean existsBySeccionAndAlumno(Seccion seccion, Usuario alumno);

    /**
     * Busca todas las matrículas de un alumno específico.
     * 🚨 NOTA: 'findByAlumnoId' debe estar en inglés.
     */
    List<Matricula> findByAlumnoId(Long alumnoId);

    /**
     * Cuenta cuántos alumnos hay matriculados en una sección.
     * 🚨 NOTA: 'countBySeccionId' debe estar en inglés.
     */
    long countBySeccionId(Long seccionId);
}