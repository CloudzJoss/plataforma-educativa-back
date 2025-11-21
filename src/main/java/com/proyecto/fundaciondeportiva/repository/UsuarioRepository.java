package com.proyecto.fundaciondeportiva.repository;

import com.proyecto.fundaciondeportiva.model.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio para la entidad Usuario.
 * Reemplaza al 'UsuarioRepository' anterior.
 */
@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    /**
     * Busca un usuario por su email para obtener el objeto completo.
     * VITAL para Spring Security (UserDetailsService).
     * 🚨 NOTA: El nombre 'findByEmail' DEBE estar en inglés.
     */
    Optional<Usuario> findByEmail(String email);

    /**
     * Verifica de forma eficiente si un email ya existe.
     * VITAL para validaciones (ej. al crear un nuevo usuario).
     * 🚨 NOTA: El nombre 'existsByEmail' DEBE estar en inglés.
     */
    boolean existsByEmail(String email);
}