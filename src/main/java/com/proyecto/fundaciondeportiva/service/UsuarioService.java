package com.proyecto.fundaciondeportiva.service;

import com.proyecto.fundaciondeportiva.dto.input.UsuarioInputDTO;
import com.proyecto.fundaciondeportiva.dto.output.UsuarioUpdateDTO;
import com.proyecto.fundaciondeportiva.dto.output.UsuarioOutputDTO;
import com.proyecto.fundaciondeportiva.dto.response.UsuarioResponse;
import com.proyecto.fundaciondeportiva.exception.RecursoNoEncontradoException;
import com.proyecto.fundaciondeportiva.exception.ValidacionException;
import com.proyecto.fundaciondeportiva.model.entity.PerfilAlumno;
import com.proyecto.fundaciondeportiva.model.entity.PerfilProfesor;
import com.proyecto.fundaciondeportiva.model.entity.Usuario;
import com.proyecto.fundaciondeportiva.model.enums.Rol;
import com.proyecto.fundaciondeportiva.repository.PerfilAlumnoRepository;
import com.proyecto.fundaciondeportiva.repository.PerfilProfesorRepository;
import com.proyecto.fundaciondeportiva.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * REFACTORIZACIÓN COMPLETA de tu UsuarioService.
 * AHORA usa los nuevos DTOs (UsuarioInputDTO, UsuarioOutputDTO, UsuarioUpdateDTO)
 * y las nuevas entidades (Usuario, PerfilAlumno, PerfilProfesor del diagrama).
 * Sigue implementando UserDetailsService.
 */
@Service
public class UsuarioService implements UserDetailsService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PerfilAlumnoRepository perfilAlumnoRepository;

    @Autowired
    private PerfilProfesorRepository perfilProfesorRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // --- 1. Lógica de Seguridad (Implementación de UserDetailsService) ---
    /**
     * Este método es llamado por JwtAuthenticationFilter.
     * Carga el usuario (nuestra NUEVA entidad Usuario) desde la BD.
     * La entidad 'Usuario' (del nuevo modelo) ya implementa 'UserDetails',
     * por lo que podemos devolverla directamente.
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // 🚨 CAMBIO: Devolvemos nuestra entidad 'Usuario' directamente.
        // Esto reemplaza tu lógica antigua de 'User.builder()'
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con email: " + email));
    }

    // --- 2. Lógica de Negocio (CRUD del Admin) ---

    /**
     * Registra un nuevo usuario (Admin, Profesor o Alumno) en el sistema.
     * 🚨 CAMBIO: AHORA USA EL NUEVO DTO 'UsuarioInputDTO'
     * 🚨 CAMBIO: Ahora devuelve la ENTIDAD Usuario (para ser compatible con tu controller)
     */
    @Transactional
    public Usuario crearUsuario(UsuarioInputDTO request) {

        // --- 1. Validaciones de Negocio ---
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new ValidacionException("El correo electrónico ya está en uso.");
        }

        // Construir la entidad Usuario base
        Usuario nuevoUsuario = Usuario.builder()
                .nombre(request.getNombre())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .rol(request.getRol())
                .build();

        // --- 2. Lógica de Perfiles (basada en el diagrama PlantUML) ---
        if (request.getRol() == Rol.ALUMNO) {
            // Validaciones de Alumno
            if (request.getDniAlumno() == null || request.getDniAlumno().isBlank()) {
                throw new ValidacionException("El DNI es obligatorio para el alumno.");
            }
            if (perfilAlumnoRepository.existsByDni(request.getDniAlumno())) {
                throw new ValidacionException("El DNI del alumno ya está registrado.");
            }
            if (request.getNivel() == null) {
                throw new ValidacionException("El Nivel Académico es obligatorio para el alumno.");
            }
            if (request.getGrado() == null || request.getGrado().isBlank()) {
                throw new ValidacionException("El Grado es obligatorio para el alumno.");
            }

            // Generar código de estudiante si no se provee
            String codigoEstudiante = request.getCodigoEstudiante();
            if (codigoEstudiante == null || codigoEstudiante.isBlank()) {
                codigoEstudiante = generarCodigoEstudianteUnico();
            } else if (perfilAlumnoRepository.existsByCodigoEstudiante(codigoEstudiante)) {
                throw new ValidacionException("El código de estudiante ya existe.");
            }

            // Crear y asociar el perfil
            PerfilAlumno perfil = PerfilAlumno.builder()
                    .dni(request.getDniAlumno())
                    .nivel(request.getNivel())
                    .grado(request.getGrado())
                    .codigoEstudiante(codigoEstudiante)
                    .usuario(nuevoUsuario) // Vincula el perfil al usuario
                    .build();
            nuevoUsuario.setPerfilAlumno(perfil); // Vincula el usuario al perfil

        } else if (request.getRol() == Rol.PROFESOR) {
            // Validaciones de Profesor
            if (request.getDniProfesor() == null || request.getDniProfesor().isBlank()) {
                throw new ValidacionException("El DNI es obligatorio para el profesor.");
            }
            if (perfilProfesorRepository.existsByDni(request.getDniProfesor())) {
                throw new ValidacionException("El DNI del profesor ya está registrado.");
            }

            // Crear y asociar el perfil
            PerfilProfesor perfil = PerfilProfesor.builder()
                    .dni(request.getDniProfesor())
                    .telefono(request.getTelefono())
                    .experiencia(request.getExperiencia())
                    .gradoAcademico(request.getGradoAcademico())
                    .usuario(nuevoUsuario) // Vincula el perfil al usuario
                    .build();
            nuevoUsuario.setPerfilProfesor(perfil); // Vincula el usuario al perfil

        } else if (request.getRol() == Rol.ADMINISTRADOR) {
            // El Admin no tiene perfil.
        }

        // --- 3. Guardado ---
        // Se guarda el Usuario (y el Perfil se guarda en cascada)
        return usuarioRepository.save(nuevoUsuario);
    }

    /**
     * Lista todos los usuarios (sin cambios, sigue devolviendo la entidad
     * para que el controlador la mapee).
     */
    @Transactional(readOnly = true)
    public List<Usuario> listarTodosLosUsuarios() {
        return usuarioRepository.findAll();
    }

    /**
     * Obtiene un usuario por ID (sin cambios, sigue devolviendo la entidad
     * para que el controlador la mapee).
     */
    @Transactional(readOnly = true)
    public Usuario obtenerUsuarioPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado con id: " + id));
    }

    /**
     * Obtiene un usuario por Email (sin cambios, sigue devolviendo Optional<Usuario>
     * como lo tenías).
     */
    @Transactional(readOnly = true)
    public Optional<Usuario> obtenerUsuarioPorEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }

    /**
     * 🚨 CAMBIO: AHORA USA EL NUEVO DTO 'UsuarioUpdateDTO'
     */
    @Transactional
    public Usuario actualizarUsuario(Long id, UsuarioUpdateDTO request) {

        Usuario usuario = obtenerUsuarioPorId(id); // Usa el método que ya lanza excepción

        // --- Actualizar Campos de Usuario ---
        if (StringUtils.hasText(request.getNombre())) {
            usuario.setNombre(request.getNombre());
        }
        if (StringUtils.hasText(request.getPassword())) {
            usuario.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        if (StringUtils.hasText(request.getEmail()) && !request.getEmail().equals(usuario.getEmail())) {
            if (usuarioRepository.existsByEmail(request.getEmail())) {
                throw new ValidacionException("El nuevo correo electrónico ya está en uso.");
            }
            usuario.setEmail(request.getEmail());
        }

        // --- Actualizar Perfiles (Lógica mejorada) ---
        if (usuario.getRol() == Rol.ALUMNO) {
            // Si es alumno, DEBE tener un perfil. Si no, lo crea.
            PerfilAlumno perfil = Optional.ofNullable(usuario.getPerfilAlumno()).orElse(new PerfilAlumno());
            perfil.setUsuario(usuario); // Asegura el vínculo
            usuario.setPerfilAlumno(perfil);

            // Validar DNI de Alumno
            if (StringUtils.hasText(request.getDniAlumno()) && !request.getDniAlumno().equals(perfil.getDni())) {
                if (perfilAlumnoRepository.existsByDni(request.getDniAlumno())) {
                    throw new ValidacionException("El nuevo DNI de alumno ya está en uso.");
                }
                perfil.setDni(request.getDniAlumno());
            }
            // Actualizar otros campos de Alumno
            if (request.getNivel() != null) {
                perfil.setNivel(request.getNivel());
            }
            if (StringUtils.hasText(request.getGrado())) {
                perfil.setGrado(request.getGrado());
            }
            if (StringUtils.hasText(request.getCodigoEstudiante())) {
                perfil.setCodigoEstudiante(request.getCodigoEstudiante());
            }
        }
        else if (usuario.getRol() == Rol.PROFESOR) {
            // Si es profesor, DEBE tener un perfil. Si no, lo crea.
            PerfilProfesor perfil = Optional.ofNullable(usuario.getPerfilProfesor()).orElse(new PerfilProfesor());
            perfil.setUsuario(usuario); // Asegura el vínculo
            usuario.setPerfilProfesor(perfil);

            // Validar DNI de Profesor
            if (StringUtils.hasText(request.getDniProfesor()) && !request.getDniProfesor().equals(perfil.getDni())) {
                if (perfilProfesorRepository.existsByDni(request.getDniProfesor())) {
                    throw new ValidacionException("El nuevo DNI de profesor ya está en uso.");
                }
                perfil.setDni(request.getDniProfesor());
            }
            // Actualizar otros campos de Profesor
            if (StringUtils.hasText(request.getTelefono())) {
                perfil.setTelefono(request.getTelefono());
            }
            if (StringUtils.hasText(request.getExperiencia())) {
                perfil.setExperiencia(request.getExperiencia());
            }
            if (StringUtils.hasText(request.getGradoAcademico())) {
                perfil.setGradoAcademico(request.getGradoAcademico());
            }
        }

        return usuarioRepository.save(usuario);
    }

    @Transactional
    public void eliminarUsuario(Long id) {
        if (!usuarioRepository.existsById(id)) {
            throw new RecursoNoEncontradoException("Usuario no encontrado con id: " + id);
        }
        usuarioRepository.deleteById(id);
    }

    // --- Métodos Helper ---

    private String generarCodigoEstudianteUnico() {
        String codigoGenerado;
        boolean codigoExiste;
        do {
            codigoGenerado = "E-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            codigoExiste = perfilAlumnoRepository.existsByCodigoEstudiante(codigoGenerado);
        } while (codigoExiste);
        return codigoGenerado;
    }

    /**
     * Obtiene un usuario por email y lo convierte a DTO.
     * Necesario para el endpoint de "Mis Secciones" del profesor.
     */
    @Transactional(readOnly = true)
    public UsuarioResponse obtenerUsuarioResponsePorEmail(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado con email: " + email));
        return UsuarioResponse.deEntidad(usuario);
    }
}