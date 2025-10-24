package com.proyecto.fundaciondeportiva.repository;

import com.proyecto.fundaciondeportiva.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    // Spring Data JPA crea automáticamente la consulta para este método
    boolean existsByEmail(String email);
    Optional<Usuario> findByEmail(String email);
}