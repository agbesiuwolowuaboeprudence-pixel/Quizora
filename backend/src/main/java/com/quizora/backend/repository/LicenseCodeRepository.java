package com.quizora.backend.repository;

import com.quizora.backend.domain.LicenseCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

public interface LicenseCodeRepository extends JpaRepository<LicenseCode, Long> {

    Optional<LicenseCode> findByCodeIgnoreCase(String code);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select l from LicenseCode l where lower(l.code) = lower(:code)")
    Optional<LicenseCode> findByCodeIgnoreCaseForUpdate(@Param("code") String code);

    List<LicenseCode> findByInstitutionIdOrderByCreatedAtDesc(Long institutionId);
}
