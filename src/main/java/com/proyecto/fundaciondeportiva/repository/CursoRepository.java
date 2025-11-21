package com.proyecto.fundaciondeportiva.repository;

import com.proyecto.fundaciondeportiva.model.entity.Curso;
import com.proyecto.fundaciondeportiva.model.enums.NivelAcademico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la entidad Curso.
 * 🚨 ACTUALIZADO: Se eliminó el método de búsqueda por Nivel y Grado.
 */
@Repository
public interface CursoRepository extends JpaRepository<Curso, Long> {

    /**
     * Busca un curso por su código único.
     */
    Optional<Curso> findByCodigo(String codigo);

    /**
     * Busca todos los cursos destinados a un nivel específico.
     * 🚨 ACTUALIZADO: Ahora busca solo por Nivel.
     */
    List<Curso> findByNivelDestino(NivelAcademico nivel);

    /**
     * Verifica de forma eficiente si un código de curso ya existe.
     */
    boolean existsByCodigo(String codigo);
}