package com.mathbot.backend.controllers;

import com.mathbot.backend.models.dto.user.UpdateUserRequest;
import com.mathbot.backend.models.dto.user.UserResponse;
import com.mathbot.backend.models.dto.user.TeacherDashboardResponse;
import jakarta.validation.Valid;
import com.mathbot.backend.services.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
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
}