package com.quizora.backend.repository;

import com.quizora.backend.domain.LicenseCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LicenseCodeRepository extends JpaRepository<LicenseCode, Long> {

    Optional<LicenseCode> findByCodeIgnoreCase(String code);

    List<LicenseCode> findByInstitutionIdOrderByCreatedAtDesc(Long institutionId);
}
