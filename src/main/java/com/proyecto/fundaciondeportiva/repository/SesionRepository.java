package com.proyecto.fundaciondeportiva.repository;

import com.proyecto.fundaciondeportiva.model.entity.Sesion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio para la entidad Sesion. (NUEVO)
 */
@Repository
public interface SesionRepository extends JpaRepository<Sesion, Long> {

    /**
     * Busca todas las sesiones de una sección específica,
     * ordenadas por fecha.
     * 🚨 NOTA: 'findBySeccionIdOrderByFechaAsc' debe estar en inglés.
     */
    List<Sesion> findBySeccionIdOrderByFechaAsc(Long seccionId);
}