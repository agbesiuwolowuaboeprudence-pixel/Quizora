package com.quizora.backend.repository;

import com.quizora.backend.domain.Role;
import com.quizora.backend.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    List<User> findByInstitutionIdOrderByFullNameAsc(Long institutionId);

    long countByRole(Role role);

    long countByInstitutionId(Long institutionId);

    long countByInstitutionIdAndRole(Long institutionId, Role role);

    List<User> findByRoleOrderByCreatedAtDesc(Role role);

    /** Loads the user with its institution initialized (avoids LazyInitializationException). */
    @Query("select u from User u left join fetch u.institution where u.id = :id")
    java.util.Optional<User> findWithInstitution(@Param("id") Long id);
}
