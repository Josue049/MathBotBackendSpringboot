package com.mathbot.backend.repositories;

import com.mathbot.backend.models.entity.Classroom;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClassroomRepository extends JpaRepository<Classroom, Long> {
    List<Classroom> findByTeacherIdOrderByNameAsc(Long teacherId);

    List<Classroom> findBySchoolIdOrderByNameAsc(Long schoolId);

    Optional<Classroom> findByIdAndTeacherId(Long id, Long teacherId);
}
