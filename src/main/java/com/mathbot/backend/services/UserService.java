package com.mathbot.backend.services;

import com.mathbot.backend.models.dto.auth.AuthResponse;
import com.mathbot.backend.models.dto.auth.LoginRequest;
import com.mathbot.backend.models.dto.auth.RegisterRequest;
import com.mathbot.backend.models.dto.user.TeacherDashboardResponse;
import com.mathbot.backend.models.dto.user.TeacherClassStats;
import com.mathbot.backend.models.dto.user.TeacherGradeStat;
import com.mathbot.backend.models.dto.user.TeacherStudentStats;
import com.mathbot.backend.models.dto.user.UpdateUserRequest;
import com.mathbot.backend.models.dto.user.UserResponse;
import com.mathbot.backend.models.entity.Role;
import com.mathbot.backend.models.entity.User;
import com.mathbot.backend.repositories.UserRepository;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UserService {

    private static final DateTimeFormatter ISO_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final ChatService chatService;

    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            ChatService chatService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.chatService = chatService;
    }

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByCorreoIgnoreCase(request.correo())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El correo ya está registrado");
        }
        if (userRepository.existsByUsuarioIgnoreCase(request.usuario())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El usuario ya está registrado");
        }

        User user = new User();
        user.setNombre(request.nombre());
        user.setCorreo(request.correo());
        user.setTelefono(request.telefono());
        user.setUsuario(request.usuario());
        user.setPassword(passwordEncoder.encode(request.contrasena()));
        user.setAvatar(request.avatar());

        Role role = parseRole(request.role());
        String institution = normalizeOptional(request.institution());

        if (role == Role.ROLE_TEACHER) {
            user.setInstitution(institution);
            user.setTeacherId(null);
            user.setRole(Role.ROLE_TEACHER);
            user.setApellidoPaterno(
                    request.apellidoPaterno() != null && !request.apellidoPaterno().isBlank()
                            ? request.apellidoPaterno().trim()
                            : request.nombre().trim());
            user.setApellidoMaterno(
                    request.apellidoMaterno() != null && !request.apellidoMaterno().isBlank()
                            ? request.apellidoMaterno().trim()
                            : "Adulto");
            user.setEdad(request.edad() != null ? request.edad() : 30);
            user.setGrado(request.grado() != null && !request.grado().isBlank() ? request.grado().trim() : "Profesor");
        } else {
            if (request.apellidoPaterno() == null || request.apellidoPaterno().isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "El apellido paterno es obligatorio para estudiantes");
            }
            if (request.apellidoMaterno() == null || request.apellidoMaterno().isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "El apellido materno es obligatorio para estudiantes");
            }
            if (request.edad() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La edad es obligatoria para estudiantes");
            }
            if (request.grado() == null || request.grado().isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El grado es obligatorio para estudiantes");
            }

            user.setApellidoPaterno(request.apellidoPaterno().trim());
            user.setApellidoMaterno(request.apellidoMaterno().trim());
            user.setEdad(request.edad());
            user.setGrado(request.grado().trim());
            user.setRole(Role.ROLE_STUDENT);

            if (institution == null) {
                user.setInstitution(null);
                user.setTeacherId(null);
            } else {
                if (request.teacherId() == null) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Si indicas una institución, debes elegir un profesor");
                }
                User teacher = userRepository.findById(request.teacherId())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "El profesor no existe"));
                if (teacher.getRole() != Role.ROLE_TEACHER) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "El usuario seleccionado no es profesor");
                }
                if (teacher.getInstitution() == null
                        || !teacher.getInstitution().equalsIgnoreCase(institution)) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "El profesor debe pertenecer a la misma institución");
                }
                user.setInstitution(institution);
                user.setTeacherId(teacher.getId());
            }
        }

        User savedUser = userRepository.save(user);
        return new AuthResponse(true, "Usuario registrado correctamente", jwtService.generateToken(savedUser),
                toUserResponse(savedUser));
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByUsuarioIgnoreCaseOrCorreoIgnoreCase(request.identifier(), request.identifier())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales inválidas"));

        if (!passwordEncoder.matches(request.contrasena(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales inválidas");
        }

        return new AuthResponse(true, "Autenticación exitosa", jwtService.generateToken(user), toUserResponse(user));
    }

    public UserResponse getCurrentUser(String username) {
        User user = userRepository.findByUsuarioIgnoreCaseOrCorreoIgnoreCase(username, username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
        return toUserResponse(user);
    }

    public UserResponse updateCurrentUser(String username, UpdateUserRequest request) {
        User user = userRepository.findByUsuarioIgnoreCaseOrCorreoIgnoreCase(username, username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        if (request.nombre() != null && !request.nombre().isBlank()) {
            user.setNombre(request.nombre().trim());
        }
        if (request.grado() != null && !request.grado().isBlank()) {
            user.setGrado(request.grado().trim());
        }
        if (request.avatar() != null && !request.avatar().isBlank()) {
            user.setAvatar(request.avatar().trim());
        }

        User saved = userRepository.save(user);
        return toUserResponse(saved);
    }

    public List<UserResponse> getTeachersByInstitution(String institution) {
        String normalized = normalizeOptional(institution);
        if (normalized == null) {
            return List.of();
        }
        return userRepository.findByRoleAndInstitutionIgnoreCaseOrderByNombreAsc(Role.ROLE_TEACHER, normalized)
                .stream()
                .map(this::toUserResponse)
                .toList();
    }

    public TeacherDashboardResponse getTeacherDashboard(String username) {
        User teacher = userRepository.findByUsuarioIgnoreCaseOrCorreoIgnoreCase(username, username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        if (teacher.getRole() != Role.ROLE_TEACHER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo un profesor puede ver este dashboard");
        }

        List<User> students = userRepository.findByTeacherIdOrderByNombreAsc(teacher.getId());
        List<TeacherStudentStats> studentStats = new ArrayList<>();
        Map<String, Integer> gradeCounts = new LinkedHashMap<>();
        int totalConversations = 0;
        int activeStudents = 0;

        for (User student : students) {
            int conversations = (int) chatService.countConversationsForUser(student.getId());
            totalConversations += conversations;

            String lastActivity = chatService.lastActivityForUser(student.getId());
            if (lastActivity != null && !lastActivity.isBlank()) {
                try {
                    LocalDateTime activityTime = LocalDateTime.parse(lastActivity, ISO_FORMAT);
                    if (ChronoUnit.DAYS.between(activityTime, LocalDateTime.now()) <= 30) {
                        activeStudents++;
                    }
                } catch (Exception ignored) {
                }
            }

            gradeCounts.merge(student.getGrado() != null ? student.getGrado() : "Sin grado", 1, Integer::sum);
            studentStats.add(new TeacherStudentStats(
                    student.getId(),
                    student.getNombre(),
                    student.getUsuario(),
                    student.getGrado(),
                    student.getAvatar(),
                    student.getInstitution(),
                    student.getTeacherId(),
                    conversations,
                    lastActivity));
        }

        List<TeacherGradeStat> gradeStats = gradeCounts.entrySet().stream()
                .map(entry -> new TeacherGradeStat(entry.getKey(), entry.getValue()))
                .toList();

        TeacherClassStats classStats = new TeacherClassStats(
                students.size(),
                totalConversations,
                activeStudents,
                gradeStats);

        return new TeacherDashboardResponse(true, toUserResponse(teacher), studentStats, classStats);
    }

    private Role parseRole(String rawRole) {
        if (rawRole == null || rawRole.isBlank()) {
            return Role.ROLE_STUDENT;
        }

        try {
            return Role.valueOf(rawRole.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return Role.ROLE_STUDENT;
        }
    }

    private String normalizeOptional(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private UserResponse toUserResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getNombre(),
                user.getApellidoPaterno(),
                user.getApellidoMaterno(),
                user.getEdad(),
                user.getGrado(),
                user.getInstitution(),
                user.getCorreo(),
                user.getTelefono(),
                user.getUsuario(),
                user.getAvatar(),
                user.getTeacherId(),
                user.getRole().name());
    }
}
