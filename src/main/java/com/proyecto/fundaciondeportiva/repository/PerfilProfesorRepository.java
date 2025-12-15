package com.proyecto.fundaciondeportiva.repository;

import com.proyecto.fundaciondeportiva.model.entity.PerfilProfesor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio para la entidad PerfilProfesor.
 * Reemplaza al 'PerfilProfesorRepository' anterior.
 */
@Repository
public interface PerfilProfesorRepository extends JpaRepository<PerfilProfesor, Long> {

    /**
     * Busca un perfil de profesor por su DNI.
     *  'findByDni' debe estar en inglés.
     */
    Optional<PerfilProfesor> findByDni(String dni);

    /**
     * Verifica de forma eficiente si un DNI ya existe.
     * 'existsByDni' debe estar en inglés.
     */
    boolean existsByDni(String dni);
}