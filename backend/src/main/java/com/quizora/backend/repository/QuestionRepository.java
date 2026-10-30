package com.quizora.backend.repository;

import com.quizora.backend.domain.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Long> {

    /** Ordered paper for a course/year - order is stable across a quiz attempt. */
    List<Question> findByCourseIdAndYearOrderByIdAsc(Long courseId, int year);

    long countByCourseIdAndYear(Long courseId, int year);

    long countByCourseId(Long courseId);

    @Query("select distinct q.year from Question q where q.course.id = :courseId order by q.year desc")
    List<Integer> findYearsByCourseId(@Param("courseId") Long courseId);

    List<Question> findByCourseIdOrderByIdAsc(Long courseId);

    List<Question> findByYearOrderByIdAsc(int year);
}
