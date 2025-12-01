package com.proyecto.fundaciondeportiva.dto.request;

import com.proyecto.fundaciondeportiva.model.enums.MomentoClase;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class RecursoRequestDTO {
    private String titulo;
    private String descripcion;
    private String tipoArchivo; // LINK, PDF, VIDEO, etc.
    private MomentoClase momento;
    private Long sesionId;

    // Opcionales: Uno de los dos debe venir
    private String urlExterna; // Para links de YouTube o webs
    private MultipartFile archivo; // Para PDFs o imágenes
}