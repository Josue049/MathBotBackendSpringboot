package com.mathbot.backend.models.dto.user;

public record UserResponse(
                Long id,
                String nombre,
                String apellidoPaterno,
                String apellidoMaterno,
                Integer edad,
                String grado,
                String institution,
                String correo,
                String telefono,
                String usuario,
                String avatar,
                Long teacherId,
                String role) {
}