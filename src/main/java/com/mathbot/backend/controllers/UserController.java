package com.mathbot.backend.controllers;

import com.mathbot.backend.models.dto.user.ClassroomResponse;
import com.mathbot.backend.models.dto.user.CreateClassroomRequest;
import com.mathbot.backend.models.dto.user.UpdateUserRequest;
import com.mathbot.backend.models.dto.user.UserResponse;
import com.mathbot.backend.models.dto.user.TeacherDashboardResponse;
import com.mathbot.backend.services.ClassroomService;
import com.mathbot.backend.services.UserService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final ClassroomService classroomService;

    public UserController(UserService userService, ClassroomService classroomService) {
        this.userService = userService;
        this.classroomService = classroomService;
    }

    @GetMapping("/me")
    public UserResponse me(Authentication authentication) {
        return userService.getCurrentUser(authentication.getName());
    }

    @PutMapping("/me")
    public UserResponse updateMe(Authentication authentication, @Valid @RequestBody UpdateUserRequest request) {
        return userService.updateCurrentUser(authentication.getName(), request);
    }

    @GetMapping("/dashboard/teacher")
    public TeacherDashboardResponse teacherDashboard(Authentication authentication) {
        return userService.getTeacherDashboard(authentication.getName());
    }

    @GetMapping("/classrooms")
    public List<ClassroomResponse> myClassrooms(Authentication authentication) {
        return classroomService.listByTeacher(
                userService.getCurrentUser(authentication.getName()).id());
    }

    @PostMapping("/classrooms")
    public ClassroomResponse createClassroom(
            Authentication authentication,
            @Valid @RequestBody CreateClassroomRequest request) {
        return classroomService.createForTeacher(authentication.getName(), request);
    }
}