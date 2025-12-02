package com.proyecto.fundaciondeportiva.dto.request;

import com.proyecto.fundaciondeportiva.model.enums.EstadoAsistencia;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class RegistroAsistenciaDTO {

    @NotNull
    private Long sesionId;

    // Lista de asistencias individuales para guardar en bloque
    private List<DetalleAsistenciaDTO> detalles;

    @Data
    public static class DetalleAsistenciaDTO {
        @NotNull
        private Long alumnoId;
        @NotNull
        private EstadoAsistencia estado;
        private String observacion;
    }
}