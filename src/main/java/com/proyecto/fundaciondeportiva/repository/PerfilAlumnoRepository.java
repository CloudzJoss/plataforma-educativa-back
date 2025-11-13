package com.proyecto.fundaciondeportiva.repository;

import com.proyecto.fundaciondeportiva.model.PerfilAlumno;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PerfilAlumnoRepository extends JpaRepository<PerfilAlumno, Long> {

    boolean existsByCodigoEstudiante(String codigoEstudiante);

    // AÑADIDO: Método para verificar unicidad del DNI
    boolean existsByDni(String dni);

}