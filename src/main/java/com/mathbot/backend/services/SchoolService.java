package com.mathbot.backend.services;

import com.mathbot.backend.models.entity.School;
import com.mathbot.backend.repositories.SchoolRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SchoolService {

    private final SchoolRepository schoolRepository;

    public SchoolService(SchoolRepository schoolRepository) {
        this.schoolRepository = schoolRepository;
    }

    @Transactional
    public School findOrCreate(String name) {
        String normalized = name.trim();
        return schoolRepository.findByNameIgnoreCase(normalized)
                .orElseGet(() -> {
                    School school = new School();
                    school.setName(normalized);
                    return schoolRepository.save(school);
                });
    }
}
