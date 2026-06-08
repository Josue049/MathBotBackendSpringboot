package com.mathbot.backend.services;

import com.mathbot.backend.models.entity.Role;
import com.mathbot.backend.models.entity.User;
import com.mathbot.backend.repositories.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthAccessService {

    private final UserRepository userRepository;

    public AuthAccessService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User requireUser(String username) {
        return userRepository.findByUsuarioIgnoreCaseOrCorreoIgnoreCase(username, username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no autenticado"));
    }

    public void assertCanAccessUserData(String username, Long targetUserId) {
        User current = requireUser(username);
        if (current.getId().equals(targetUserId)) {
            return;
        }

        if (current.getRole() == Role.ROLE_TEACHER) {
            User student = userRepository.findById(targetUserId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Estudiante no encontrado"));
            if (student.getTeacherId() != null && student.getTeacherId().equals(current.getId())) {
                return;
            }
        }

        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permiso para ver estos datos");
    }
}
