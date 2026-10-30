package com.quizora.backend.repository;

import com.quizora.backend.domain.AttemptStatus;
import com.quizora.backend.domain.QuizAttempt;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Long> {

    List<QuizAttempt> findByUser_IdOrderByStartedAtDesc(Long userId);

    Page<QuizAttempt> findByUser_IdOrderByStartedAtDesc(Long userId, Pageable pageable);

    Optional<QuizAttempt> findByIdAndUser_Id(Long attemptId, Long userId);

    /** [attempts, answered, correct, seconds] for all finished attempts of a user. */
    @Query("select count(a), coalesce(sum(a.answeredCount), 0), coalesce(sum(a.correctCount), 0), " +
           "coalesce(sum(a.durationSeconds), 0) from QuizAttempt a " +
           "where a.user.id = :userId and a.status <> :status")
    List<Object[]> findTotalsByUser(@Param("userId") Long userId, @Param("status") AttemptStatus status);

    /** Per-course roll-up: [courseId, courseName, attempts, answered, correct, seconds, avgScore]. */
    @Query("select a.course.id, a.course.name, count(a), coalesce(sum(a.answeredCount), 0), " +
           "coalesce(sum(a.correctCount), 0), coalesce(sum(a.durationSeconds), 0), avg(a.scorePercent) " +
           "from QuizAttempt a where a.user.id = :userId and a.status <> :status " +
           "group by a.course.id, a.course.name order by a.course.name")
    List<Object[]> findCourseRollupByUser(@Param("userId") Long userId, @Param("status") AttemptStatus status);

    /** Distinct timestamps for active-day computations (streak / trend). */
    @Query("select coalesce(a.lastActiveAt, a.submittedAt) from QuizAttempt a " +
           "where a.user.id = :userId and " +
           "coalesce(a.lastActiveAt, a.submittedAt) is not null order by coalesce(a.lastActiveAt, a.submittedAt)")
    List<LocalDateTime> findActiveDatesByUser(@Param("userId") Long userId);

    /** [timestamp, scorePercent] for attempts with a score, ordered by time (for trend charting). */
    @Query("select coalesce(a.lastActiveAt, a.submittedAt), a.scorePercent from QuizAttempt a " +
           "where a.user.id = :userId and " +
           "coalesce(a.lastActiveAt, a.submittedAt) is not null order by coalesce(a.lastActiveAt, a.submittedAt)")
    List<Object[]> findScoresByUser(@Param("userId") Long userId);

    /** Institution-wide per-student roll-up: [userId, attempts, answered, correct, lastActive, avgScore]. */
    @Query("select a.user.id, count(a), coalesce(sum(a.answeredCount), 0), coalesce(sum(a.correctCount), 0), " +
           "max(coalesce(a.lastActiveAt, a.submittedAt)), avg(a.scorePercent) from QuizAttempt a " +
           "where a.user.institution.id = :institutionId and a.status <> :status " +
           "group by a.user.id")
    List<Object[]> findStudentRollupForInstitution(@Param("institutionId") Long institutionId,
                                                   @Param("status") AttemptStatus status);

    /** Institution-wide totals: [attempts, answered, correct, seconds]. */
    @Query("select count(a), coalesce(sum(a.answeredCount), 0), coalesce(sum(a.correctCount), 0), " +
           "coalesce(sum(a.durationSeconds), 0) from QuizAttempt a " +
           "where a.user.institution.id = :institutionId and a.status <> :status")
    List<Object[]> findTotalsForInstitution(@Param("institutionId") Long institutionId,
                                            @Param("status") AttemptStatus status);

    long countByUser_Institution_Id(Long institutionId);
}
