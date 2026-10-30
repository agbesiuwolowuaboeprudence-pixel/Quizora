package com.quizora.backend.repository;

import com.quizora.backend.domain.AttemptAnswer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AttemptAnswerRepository extends JpaRepository<AttemptAnswer, Long> {

    List<AttemptAnswer> findByAttemptId(Long attemptId);

    Optional<AttemptAnswer> findByAttemptIdAndQuestionId(Long attemptId, Long questionId);

    long countByAttemptId(Long attemptId);

    long countByAttemptIdAndCorrectTrue(Long attemptId);
}
