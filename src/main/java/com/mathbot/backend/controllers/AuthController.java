package com.mathbot.backend.controllers;

import com.mathbot.backend.models.dto.auth.AuthResponse;
import com.mathbot.backend.models.dto.auth.LoginRequest;
import com.mathbot.backend.models.dto.auth.RegisterRequest;
import com.mathbot.backend.models.dto.user.ClassroomResponse;
import com.mathbot.backend.models.dto.user.UserResponse;
import com.mathbot.backend.services.ClassroomService;
import com.mathbot.backend.services.UserService;
import java.util.List;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final ClassroomService classroomService;

    public AuthController(UserService userService, ClassroomService classroomService) {
        this.userService = userService;
        this.classroomService = classroomService;
    }

    @PostMapping("/register")
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return userService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return userService.login(request);
    }

    @GetMapping("/teachers")
    public List<UserResponse> teachers(@RequestParam String institution) {
        return userService.getTeachersByInstitution(institution);
    }

    @GetMapping("/classrooms")
    public List<ClassroomResponse> classrooms(@RequestParam Long teacherId) {
        return classroomService.listByTeacher(teacherId);
    }
}