package com.mathbot.backend.models.dto.user;

public record TeacherStudentStats(
        Long id,
        String nombre,
        String usuario,
        String grado,
        String avatar,
        String institution,
        Long teacherId,
        Integer conversations,
        String lastActivity) {
}
