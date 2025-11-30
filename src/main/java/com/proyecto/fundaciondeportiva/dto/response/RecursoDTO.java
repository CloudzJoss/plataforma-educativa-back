package com.proyecto.fundaciondeportiva.dto.response;

import com.proyecto.fundaciondeportiva.model.entity.Recurso;
import com.proyecto.fundaciondeportiva.model.enums.MomentoClase;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecursoDTO {
    private Long id;
    private String titulo;
    private String url;
    private String tipoArchivo;
    private MomentoClase momento;

    public static RecursoDTO deEntidad(Recurso recurso) {
        return RecursoDTO.builder()
                .id(recurso.getId())
                .titulo(recurso.getTitulo())
                .url(recurso.getUrl())
                .tipoArchivo(recurso.getTipoArchivo())
                .momento(recurso.getMomento())
                .build();
    }
}
