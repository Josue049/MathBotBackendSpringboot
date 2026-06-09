package com.mathbot.backend.services;

import com.mathbot.backend.models.dto.user.ClassroomResponse;
import com.mathbot.backend.models.dto.user.CreateClassroomRequest;
import com.mathbot.backend.models.entity.Classroom;
import com.mathbot.backend.models.entity.Role;
import com.mathbot.backend.models.entity.School;
import com.mathbot.backend.models.entity.User;
import com.mathbot.backend.repositories.ClassroomRepository;
import com.mathbot.backend.repositories.SchoolRepository;
import com.mathbot.backend.repositories.UserRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ClassroomService {

    private final ClassroomRepository classroomRepository;
    private final UserRepository userRepository;
    private final SchoolRepository schoolRepository;
    private final SchoolService schoolService;

    public ClassroomService(
            ClassroomRepository classroomRepository,
            UserRepository userRepository,
            SchoolRepository schoolRepository,
            SchoolService schoolService) {
        this.classroomRepository = classroomRepository;
        this.userRepository = userRepository;
        this.schoolRepository = schoolRepository;
        this.schoolService = schoolService;
    }

    public List<ClassroomResponse> listByTeacher(Long teacherId) {
        User teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Profesor no encontrado"));
        if (teacher.getRole() != Role.ROLE_TEACHER) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El usuario no es profesor");
        }

        return classroomRepository.findByTeacherIdOrderByNameAsc(teacherId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public ClassroomResponse createForTeacher(String username, CreateClassroomRequest request) {
        User teacher = userRepository.findByUsuarioIgnoreCaseOrCorreoIgnoreCase(username, username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        if (teacher.getRole() != Role.ROLE_TEACHER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo un profesor puede crear salones");
        }

        if (teacher.getSchoolId() == null) {
            if (teacher.getInstitution() == null || teacher.getInstitution().isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Debes tener una institución asignada para crear salones");
            }
            School school = schoolService.findOrCreate(teacher.getInstitution());
            teacher.setSchoolId(school.getId());
            teacher.setInstitution(school.getName());
            userRepository.save(teacher);
        }

        String name = request.name().trim();
        if (name.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El nombre del salón es obligatorio");
        }

        Classroom classroom = new Classroom();
        classroom.setName(name);
        classroom.setGrade(request.grade() != null && !request.grade().isBlank() ? request.grade().trim() : null);
        classroom.setTeacherId(teacher.getId());
        classroom.setSchoolId(teacher.getSchoolId());

        return toResponse(classroomRepository.save(classroom));
    }

    public Classroom requireClassroomForTeacher(Long classroomId, Long teacherId) {
        return classroomRepository.findByIdAndTeacherId(classroomId, teacherId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "El salón no pertenece al profesor seleccionado"));
    }

    public String resolveClassroomName(Long classroomId) {
        if (classroomId == null) {
            return null;
        }
        return classroomRepository.findById(classroomId)
                .map(Classroom::getName)
                .orElse(null);
    }

    private ClassroomResponse toResponse(Classroom classroom) {
        String schoolName = schoolRepository.findById(classroom.getSchoolId())
                .map(School::getName)
                .orElse(null);
        int studentCount = (int) userRepository.findByClassroomIdOrderByNombreAsc(classroom.getId()).size();
        return new ClassroomResponse(
                classroom.getId(),
                classroom.getName(),
                classroom.getGrade(),
                classroom.getTeacherId(),
                classroom.getSchoolId(),
                schoolName,
                studentCount);
    }
}
