package com.proyecto.fundaciondeportiva.repository;

import com.proyecto.fundaciondeportiva.model.entity.Recurso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio para la entidad Recurso. (NUEVO)
 */
@Repository
public interface RecursoRepository extends JpaRepository<Recurso, Long> {

    /**
     * Busca todos los recursos de una sesión de clase,
     * ordenados por el momento en que se usan.
     * 'findBySesionIdOrderByMomentoAsc' debe estar en inglés.
     */
    List<Recurso> findBySesionIdOrderByMomentoAsc(Long sesionId);
}