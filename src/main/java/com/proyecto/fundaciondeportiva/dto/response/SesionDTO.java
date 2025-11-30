package com.proyecto.fundaciondeportiva.dto.response;

import com.proyecto.fundaciondeportiva.model.entity.Sesion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SesionDTO {
    private Long id;
    private String tema;
    private String descripcion;
    private LocalDate fecha;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private List<RecursoDTO> recursos;

    public static SesionDTO deEntidad(Sesion sesion) {
        // Convertir recursos si existen
        List<RecursoDTO> recursosDTO = new ArrayList<>();
        if (sesion.getRecursos() != null) {
            recursosDTO = sesion.getRecursos().stream()
                    .map(RecursoDTO::deEntidad)
                    .collect(Collectors.toList());
        }

        return SesionDTO.builder()
                .id(sesion.getId())
                .tema(sesion.getTema())
                .descripcion(sesion.getDescripcion())
                .fecha(sesion.getFecha())
                .horaInicio(sesion.getHoraInicio())
                .horaFin(sesion.getHoraFin())
                .recursos(recursosDTO)
                .build();
    }
}
