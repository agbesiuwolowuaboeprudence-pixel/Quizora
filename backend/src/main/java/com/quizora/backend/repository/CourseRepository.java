package com.quizora.backend.repository;

import com.quizora.backend.domain.Course;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CourseRepository extends JpaRepository<Course, Long> {

    List<Course> findByArchivedFalseOrderByNameAsc();

    boolean existsByCodeIgnoreCase(String code);
}
