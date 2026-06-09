package com.mathbot.backend.models.dto.user;

public record ClassroomResponse(
        Long id,
        String name,
        String grade,
        Long teacherId,
        Long schoolId,
        String schoolName,
        Integer studentCount) {
}
