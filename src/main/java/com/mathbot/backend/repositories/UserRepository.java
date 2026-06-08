package com.mathbot.backend.repositories;

import com.mathbot.backend.models.entity.Role;
import com.mathbot.backend.models.entity.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByCorreoIgnoreCase(String correo);

    boolean existsByUsuarioIgnoreCase(String usuario);

    Optional<User> findByCorreoIgnoreCase(String correo);

    Optional<User> findByUsuarioIgnoreCase(String usuario);

    Optional<User> findByUsuarioIgnoreCaseOrCorreoIgnoreCase(String usuario, String correo);

    List<User> findByRoleAndInstitutionIgnoreCaseOrderByNombreAsc(Role role, String institution);

    List<User> findByTeacherIdOrderByNombreAsc(Long teacherId);
}