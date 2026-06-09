package com.mathbot.backend.services;

import com.mathbot.backend.models.dto.auth.AuthResponse;
import com.mathbot.backend.models.dto.auth.LoginRequest;
import com.mathbot.backend.models.dto.auth.RegisterRequest;
import com.mathbot.backend.models.dto.user.TeacherDashboardResponse;
import com.mathbot.backend.models.dto.user.TeacherClassStats;
import com.mathbot.backend.models.dto.user.TeacherClassroomStats;
import com.mathbot.backend.models.dto.user.TeacherGradeStat;
import com.mathbot.backend.models.dto.user.TeacherStudentStats;
import com.mathbot.backend.models.dto.user.UpdateUserRequest;
import com.mathbot.backend.models.dto.user.UserResponse;
import com.mathbot.backend.models.entity.Classroom;
import com.mathbot.backend.models.entity.Role;
import com.mathbot.backend.models.entity.School;
import com.mathbot.backend.models.entity.User;
import com.mathbot.backend.repositories.ClassroomRepository;
import com.mathbot.backend.repositories.SchoolRepository;
import com.mathbot.backend.repositories.UserRepository;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
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
    private final SchoolService schoolService;
    private final ClassroomService classroomService;
    private final ClassroomRepository classroomRepository;
    private final SchoolRepository schoolRepository;

    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            ChatService chatService,
            SchoolService schoolService,
            ClassroomService classroomService,
            ClassroomRepository classroomRepository,
            SchoolRepository schoolRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.chatService = chatService;
        this.schoolService = schoolService;
        this.classroomService = classroomService;
        this.classroomRepository = classroomRepository;
        this.schoolRepository = schoolRepository;
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
            user.setTeacherId(null);
            user.setClassroomId(null);
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

            if (institution != null) {
                School school = schoolService.findOrCreate(institution);
                user.setSchoolId(school.getId());
                user.setInstitution(school.getName());
            } else {
                user.setSchoolId(null);
                user.setInstitution(null);
            }
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
                user.setSchoolId(null);
                user.setTeacherId(null);
                user.setClassroomId(null);
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

                School school = resolveTeacherSchool(teacher, institution);
                user.setSchoolId(school.getId());
                user.setInstitution(school.getName());
                user.setTeacherId(teacher.getId());

                if (request.classroomId() != null) {
                    Classroom classroom = classroomService.requireClassroomForTeacher(
                            request.classroomId(), teacher.getId());
                    user.setClassroomId(classroom.getId());
                } else {
                    user.setClassroomId(null);
                }
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

        School school = schoolRepository.findByNameIgnoreCase(normalized).orElse(null);
        if (school != null) {
            return userRepository.findByRoleAndSchoolIdOrderByNombreAsc(Role.ROLE_TEACHER, school.getId())
                    .stream()
                    .map(this::toUserResponse)
                    .toList();
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
        Map<Long, ClassroomAccumulator> classroomAccumulators = new HashMap<>();
        int totalConversations = 0;
        int activeStudents = 0;

        for (User student : students) {
            int conversations = (int) chatService.countConversationsForUser(student.getId());
            totalConversations += conversations;

            String lastActivity = chatService.lastActivityForUser(student.getId());
            boolean isActive = false;
            if (lastActivity != null && !lastActivity.isBlank()) {
                try {
                    LocalDateTime activityTime = LocalDateTime.parse(lastActivity, ISO_FORMAT);
                    if (ChronoUnit.DAYS.between(activityTime, LocalDateTime.now()) <= 30) {
                        activeStudents++;
                        isActive = true;
                    }
                } catch (Exception ignored) {
                }
            }

            gradeCounts.merge(student.getGrado() != null ? student.getGrado() : "Sin grado", 1, Integer::sum);
            String classroomName = classroomService.resolveClassroomName(student.getClassroomId());

            studentStats.add(new TeacherStudentStats(
                    student.getId(),
                    student.getNombre(),
                    student.getUsuario(),
                    student.getGrado(),
                    student.getAvatar(),
                    student.getInstitution(),
                    student.getTeacherId(),
                    student.getClassroomId(),
                    classroomName,
                    conversations,
                    lastActivity));

            if (student.getClassroomId() != null) {
                ClassroomAccumulator acc = classroomAccumulators.computeIfAbsent(
                        student.getClassroomId(), id -> new ClassroomAccumulator(id));
                acc.studentCount++;
                acc.totalConversations += conversations;
                if (isActive) {
                    acc.activeStudents++;
                }
            }
        }

        List<Classroom> teacherClassrooms = classroomRepository.findByTeacherIdOrderByNameAsc(teacher.getId());
        List<TeacherClassroomStats> classroomStats = teacherClassrooms.stream()
                .map(classroom -> {
                    ClassroomAccumulator acc = classroomAccumulators.getOrDefault(
                            classroom.getId(), new ClassroomAccumulator(classroom.getId()));
                    return new TeacherClassroomStats(
                            classroom.getId(),
                            classroom.getName(),
                            classroom.getGrade(),
                            acc.studentCount,
                            acc.totalConversations,
                            acc.activeStudents);
                })
                .toList();

        List<TeacherGradeStat> gradeStats = gradeCounts.entrySet().stream()
                .map(entry -> new TeacherGradeStat(entry.getKey(), entry.getValue()))
                .toList();

        TeacherClassStats classStats = new TeacherClassStats(
                students.size(),
                totalConversations,
                activeStudents,
                gradeStats);

        return new TeacherDashboardResponse(
                true,
                toUserResponse(teacher),
                classroomStats,
                studentStats,
                classStats);
    }

    private School resolveTeacherSchool(User teacher, String institution) {
        if (teacher.getSchoolId() != null) {
            School school = schoolRepository.findById(teacher.getSchoolId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "La escuela del profesor no existe"));
            if (!school.getName().equalsIgnoreCase(institution)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "El profesor debe pertenecer a la misma institución");
            }
            return school;
        }

        if (teacher.getInstitution() == null || !teacher.getInstitution().equalsIgnoreCase(institution)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El profesor debe pertenecer a la misma institución");
        }

        return schoolService.findOrCreate(institution);
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
                user.getSchoolId(),
                user.getClassroomId(),
                classroomService.resolveClassroomName(user.getClassroomId()),
                user.getRole().name());
    }

    private static class ClassroomAccumulator {
        private final Long classroomId;
        private int studentCount;
        private int totalConversations;
        private int activeStudents;

        private ClassroomAccumulator(Long classroomId) {
            this.classroomId = classroomId;
        }
    }
}
