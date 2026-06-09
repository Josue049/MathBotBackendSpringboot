package com.mathbot.backend.services;

import com.mathbot.backend.models.entity.Classroom;
import com.mathbot.backend.models.entity.Role;
import com.mathbot.backend.models.entity.User;
import com.mathbot.backend.repositories.ClassroomRepository;
import com.mathbot.backend.repositories.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthAccessService {

    private final UserRepository userRepository;
    private final ClassroomRepository classroomRepository;

    public AuthAccessService(UserRepository userRepository, ClassroomRepository classroomRepository) {
        this.userRepository = userRepository;
        this.classroomRepository = classroomRepository;
    }

    public User requireUser(String username) {
        return userRepository.findByUsuarioIgnoreCaseOrCorreoIgnoreCase(username, username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no autenticado"));
    }

    public void assertCanUseChat(String username) {
        User current = requireUser(username);
        if (current.getRole() == Role.ROLE_TEACHER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Los profesores no tienen acceso al chatbot. Usa el dashboard para ver el progreso de tus alumnos.");
        }
        if (current.getRole() != Role.ROLE_STUDENT) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo los estudiantes pueden usar el chatbot");
        }
    }

    public void assertCanAccessUserData(String username, Long targetUserId) {
        User current = requireUser(username);
        if (current.getId().equals(targetUserId)) {
            return;
        }

        if (current.getRole() == Role.ROLE_TEACHER) {
            User student = userRepository.findById(targetUserId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Estudiante no encontrado"));
            if (isTeacherOfStudent(current.getId(), student)) {
                return;
            }
        }

        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permiso para ver estos datos");
    }

    private boolean isTeacherOfStudent(Long teacherId, User student) {
        if (student.getTeacherId() != null && student.getTeacherId().equals(teacherId)) {
            return true;
        }
        if (student.getClassroomId() != null) {
            return classroomRepository.findById(student.getClassroomId())
                    .map(Classroom::getTeacherId)
                    .map(id -> id.equals(teacherId))
                    .orElse(false);
        }
        return false;
    }
}
