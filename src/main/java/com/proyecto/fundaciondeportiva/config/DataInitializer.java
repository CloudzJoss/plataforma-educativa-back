package com.proyecto.fundaciondeportiva.config;

import com.proyecto.fundaciondeportiva.model.Rol;
import com.proyecto.fundaciondeportiva.model.Usuario;
import com.proyecto.fundaciondeportiva.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Esta es la lógica clave: solo se ejecuta si la tabla de usuarios está vacía.
        if (usuarioRepository.count() == 0) {
            System.out.println("No se encontraron usuarios, creando usuario administrador inicial...");

            // Creamos el usuario administrador
            Usuario admin = new Usuario();
            admin.setNombre("Admin Principal");
            admin.setEmail("admin@fundacion.com");
            // ¡Importante! Ciframos la contraseña antes de guardarla.
                    admin.setPassword(passwordEncoder.encode("adminfundacion_2025"));
            admin.setRol(Rol.ADMINISTRADOR);

            // Guardamos el usuario en la base de datos
            usuarioRepository.save(admin);

            System.out.println("Usuario administrador creado con éxito.");
        }
    }
}