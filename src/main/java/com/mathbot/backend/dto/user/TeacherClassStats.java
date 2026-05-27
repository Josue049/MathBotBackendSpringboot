package com.mathbot.backend.dto.user;

import java.util.List;

public record TeacherClassStats(
        Integer totalStudents,
        Integer totalConversations,
        Integer activeStudents,
        List<TeacherGradeStat> gradeDistribution) {
}
