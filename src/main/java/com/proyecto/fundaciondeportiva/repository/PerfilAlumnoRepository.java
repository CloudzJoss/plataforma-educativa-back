package com.proyecto.fundaciondeportiva.repository;

import com.proyecto.fundaciondeportiva.model.entity.PerfilAlumno;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio para la entidad PerfilAlumno.
 * Reemplaza al 'PerfilAlumnoRepository' anterior.
 */
@Repository
public interface PerfilAlumnoRepository extends JpaRepository<PerfilAlumno, Long> {

    // --- Métodos para OBTENER el perfil ---
    /**
     * 🚨 NOTA: 'findByDni' debe estar en inglés.
     */
    Optional<PerfilAlumno> findByDni(String dni);

    /**
     * 🚨 NOTA: 'findByCodigoEstudiante' debe estar en inglés.
     */
    Optional<PerfilAlumno> findByCodigoEstudiante(String codigoEstudiante);

    // --- Métodos para VALIDAR si ya existe (más eficientes) ---
    /**
     * 🚨 NOTA: 'existsByDni' debe estar en inglés.
     */
    boolean existsByDni(String dni);

    /**
     * 🚨 NOTA: 'existsByCodigoEstudiante' debe estar en inglés.
     */
    boolean existsByCodigoEstudiante(String codigoEstudiante);
}