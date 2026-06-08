package com.mathbot.backend.models.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
                @NotBlank String nombre,
                String apellidoPaterno,
                String apellidoMaterno,
                Integer edad,
                String grado,
                String institution,
                String role,
                Long teacherId,
                @NotBlank @Email String correo,
                String telefono,
                @NotBlank String usuario,
                @NotBlank @Size(min = 6, max = 120) String contrasena,
                String avatar) {
}
