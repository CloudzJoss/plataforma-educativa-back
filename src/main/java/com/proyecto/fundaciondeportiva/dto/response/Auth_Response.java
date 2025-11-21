package com.proyecto.fundaciondeportiva.dto.response;

import com.proyecto.fundaciondeportiva.model.enums.Rol;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para la respuesta de Login.
 * REEMPLAZA a 'LoginOutputDTO'.
 * NO contiene el token (se envía por Cookie HttpOnly).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Auth_Response {
    private Long id;
    private String nombre;
    private String email;
    private Rol rol;
}