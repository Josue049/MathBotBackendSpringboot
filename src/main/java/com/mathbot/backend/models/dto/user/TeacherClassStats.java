package com.mathbot.backend.models.dto.user;

import java.util.List;

public record TeacherClassStats(
        Integer totalStudents,
        Integer totalConversations,
        Integer activeStudents,
        List<TeacherGradeStat> gradeDistribution) {
}
