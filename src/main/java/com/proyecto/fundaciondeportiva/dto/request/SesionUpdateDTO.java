package com.proyecto.fundaciondeportiva.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SesionUpdateDTO {

    @Size(max = 200, message = "El tema no puede exceder los 200 caracteres")
    private String tema;

    @Size(max = 500, message = "El resultado no puede exceder los 500 caracteres")
    private String resultado;
}