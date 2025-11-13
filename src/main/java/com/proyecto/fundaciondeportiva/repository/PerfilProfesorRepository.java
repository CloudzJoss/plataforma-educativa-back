package com.proyecto.fundaciondeportiva.repository;

import com.proyecto.fundaciondeportiva.model.PerfilProfesor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PerfilProfesorRepository extends JpaRepository<PerfilProfesor, Long> {
    // AÑADIDO: Método para verificar unicidad del DNI
    boolean existsByDni(String dni);
}