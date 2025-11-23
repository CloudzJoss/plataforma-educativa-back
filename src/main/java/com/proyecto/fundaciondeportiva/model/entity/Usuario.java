package com.proyecto.fundaciondeportiva.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.proyecto.fundaciondeportiva.model.enums.Rol;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "usuarios", uniqueConstraints = {
        @UniqueConstraint(columnNames = "email")
})
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"}) // 👈 AÑADIDO
public class Usuario implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 100, nullable = false)
    private String nombre;

    @Column(length = 100, nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    @JsonIgnore // 👈 AÑADIDO - NUNCA serializar el password
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Rol rol;

    @CreationTimestamp
    @Column(name = "fecha_creacion", updatable = false, nullable = false)
    private LocalDateTime fechaCreacion;

    // --- Relaciones 1:1 con Perfiles ---
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "perfil_alumno_id", referencedColumnName = "id")
    private PerfilAlumno perfilAlumno;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "perfil_profesor_id", referencedColumnName = "id")
    private PerfilProfesor perfilProfesor;

    // --- Relaciones (ignoradas en JSON para evitar recursión) ---
    @OneToMany(mappedBy = "creadoPor")
    @JsonIgnore // 👈 AÑADIDO
    private Set<Curso> cursosCreados;

    @OneToMany(mappedBy = "profesor")
    @JsonIgnore // 👈 AÑADIDO
    private Set<Seccion> seccionesAsignadas;

    @OneToMany(mappedBy = "alumno")
    @JsonIgnore // 👈 AÑADIDO
    private Set<Matricula> matriculas;

    @OneToMany(mappedBy = "alumno")
    @JsonIgnore // 👈 AÑADIDO
    private Set<Asistencia> asistencias;

    // --- Métodos de Spring Security ---
    @Override
    @JsonIgnore // 👈 AÑADIDO
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + rol.name()));
    }

    @Override
    @JsonIgnore // 👈 AÑADIDO
    public String getPassword() {
        return this.password;
    }

    @Override
    @JsonIgnore // 👈 AÑADIDO
    public String getUsername() {
        return this.email;
    }

    @Override
    @JsonIgnore // 👈 AÑADIDO
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    @JsonIgnore // 👈 AÑADIDO
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    @JsonIgnore // 👈 AÑADIDO
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    @JsonIgnore // 👈 AÑADIDO
    public boolean isEnabled() {
        return true;
    }
}