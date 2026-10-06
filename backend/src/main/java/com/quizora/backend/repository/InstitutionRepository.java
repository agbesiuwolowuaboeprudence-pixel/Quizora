package com.quizora.backend.repository;

import com.quizora.backend.domain.Institution;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InstitutionRepository extends JpaRepository<Institution, Long> {

    Optional<Institution> findByContactEmailIgnoreCase(String contactEmail);
}
