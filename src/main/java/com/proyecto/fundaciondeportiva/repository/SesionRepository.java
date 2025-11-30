package com.proyecto.fundaciondeportiva.repository;

import com.proyecto.fundaciondeportiva.model.entity.Sesion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SesionRepository extends JpaRepository<Sesion, Long> {
    List<Sesion> findBySeccionIdOrderByFechaAsc(Long seccionId);

    // Para borrar sesiones viejas al actualizar una sección
    void deleteBySeccionId(Long seccionId);
}