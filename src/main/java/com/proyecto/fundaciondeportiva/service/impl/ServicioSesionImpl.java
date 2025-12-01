package com.proyecto.fundaciondeportiva.service.impl;

import com.proyecto.fundaciondeportiva.dto.request.SesionUpdateDTO;
import com.proyecto.fundaciondeportiva.dto.response.SesionDTO;
import com.proyecto.fundaciondeportiva.exception.RecursoNoEncontradoException;
import com.proyecto.fundaciondeportiva.model.entity.Sesion;
import com.proyecto.fundaciondeportiva.repository.SesionRepository;
import com.proyecto.fundaciondeportiva.service.ServicioSesion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ServicioSesionImpl implements ServicioSesion {

    @Autowired
    private SesionRepository sesionRepository;

    @Override
    @Transactional
    public SesionDTO actualizarInfoSesion(Long id, SesionUpdateDTO request) {
        Sesion sesion = sesionRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Sesión no encontrada con id: " + id));

        // Actualizamos Tema si viene en la petición
        if (request.getTema() != null) {
            sesion.setTema(request.getTema());
        }

        // Actualizamos Resultado si viene en la petición
        if (request.getResultado() != null) {
            sesion.setResultado(request.getResultado());
        }

        Sesion sesionGuardada = sesionRepository.save(sesion);
        return SesionDTO.deEntidad(sesionGuardada);
    }
}