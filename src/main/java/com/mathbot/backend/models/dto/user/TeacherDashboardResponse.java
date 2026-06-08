package com.mathbot.backend.models.dto.user;

import java.util.List;

public record TeacherDashboardResponse(
        boolean ok,
        UserResponse teacher,
        List<TeacherStudentStats> students,
        TeacherClassStats classStats) {
}
