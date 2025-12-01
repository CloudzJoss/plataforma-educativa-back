package com.proyecto.fundaciondeportiva.service;

import com.proyecto.fundaciondeportiva.dto.request.SesionUpdateDTO;
import com.proyecto.fundaciondeportiva.dto.response.SesionDTO;

public interface ServicioSesion {
    SesionDTO actualizarInfoSesion(Long id, SesionUpdateDTO request);
}