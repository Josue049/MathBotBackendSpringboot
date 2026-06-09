package com.mathbot.backend.models.dto.user;

public record TeacherClassroomStats(
        Long id,
        String name,
        String grade,
        Integer studentCount,
        Integer totalConversations,
        Integer activeStudents) {
}
