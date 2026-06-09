package com.mathbot.backend.models.dto.user;

public record TeacherStudentStats(
        Long id,
        String nombre,
        String usuario,
        String grado,
        String avatar,
        String institution,
        Long teacherId,
        Long classroomId,
        String classroomName,
        Integer conversations,
        String lastActivity) {
}
