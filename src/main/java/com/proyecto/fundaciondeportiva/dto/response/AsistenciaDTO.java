package com.proyecto.fundaciondeportiva.dto.response;

import com.proyecto.fundaciondeportiva.model.enums.EstadoAsistencia;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AsistenciaDTO {
    private Long asistenciaId; // Puede ser null si aún no se ha guardado en BD
    private Long alumnoId;
    private String nombreAlumno;
    private String codigoEstudiante;
    private EstadoAsistencia estado;
    private String observacion;
}