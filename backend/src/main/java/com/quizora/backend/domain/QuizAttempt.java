package com.quizora.backend.domain;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "quiz_attempts", indexes = {
        @Index(name = "idx_attempt_user", columnList = "user_id")
})
public class QuizAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id")
    private Course course;

    @Column(name = "exam_year", nullable = false)
    private int year;

    @Column(nullable = false)
    private boolean timed;

    /** Server-authoritative clock for timed attempts (null = untimed practice). */
    private Integer timeLimitSeconds;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private AttemptStatus status = AttemptStatus.IN_PROGRESS;

    @Column(nullable = false, updatable = false)
    private LocalDateTime startedAt = LocalDateTime.now();

    private LocalDateTime submittedAt;

    /** Updated whenever the student answers a question; used for institutional "last active" reporting. */
    private LocalDateTime lastActiveAt;

    @Column(nullable = false)
    private int totalQuestions;

    @Column(nullable = false)
    private int answeredCount;

    @Column(nullable = false)
    private int correctCount;

    /** Score as a percentage of the whole paper: correct / totalQuestions * 100. */
    private double scorePercent;

    @Column(nullable = false)
    private long durationSeconds;

    public QuizAttempt() {
    }

    public QuizAttempt(User user, Course course, int year, boolean timed,
                       Integer timeLimitSeconds, int totalQuestions) {
        this.user = user;
        this.course = course;
        this.year = year;
        this.timed = timed;
        this.timeLimitSeconds = timeLimitSeconds;
        this.totalQuestions = totalQuestions;
    }

    /** Moment the server considers the attempt over (null when untimed). */
    public LocalDateTime expiresAt() {
        if (!timed || timeLimitSeconds == null) return null;
        return startedAt.plusSeconds(timeLimitSeconds);
    }

    public boolean isExpiredNow() {
        LocalDateTime expiry = expiresAt();
        return expiry != null && !LocalDateTime.now().isBefore(expiry);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Course getCourse() { return course; }
    public void setCourse(Course course) { this.course = course; }

    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }

    public boolean isTimed() { return timed; }
    public void setTimed(boolean timed) { this.timed = timed; }

    public Integer getTimeLimitSeconds() { return timeLimitSeconds; }
    public void setTimeLimitSeconds(Integer timeLimitSeconds) { this.timeLimitSeconds = timeLimitSeconds; }

    public AttemptStatus getStatus() { return status; }
    public void setStatus(AttemptStatus status) { this.status = status; }

    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }

    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }

    public LocalDateTime getLastActiveAt() { return lastActiveAt; }
    public void setLastActiveAt(LocalDateTime lastActiveAt) { this.lastActiveAt = lastActiveAt; }

    public int getTotalQuestions() { return totalQuestions; }
    public void setTotalQuestions(int totalQuestions) { this.totalQuestions = totalQuestions; }

    public int getAnsweredCount() { return answeredCount; }
    public void setAnsweredCount(int answeredCount) { this.answeredCount = answeredCount; }

    public int getCorrectCount() { return correctCount; }
    public void setCorrectCount(int correctCount) { this.correctCount = correctCount; }

    public double getScorePercent() { return scorePercent; }
    public void setScorePercent(double scorePercent) { this.scorePercent = scorePercent; }

    public long getDurationSeconds() { return durationSeconds; }
    public void setDurationSeconds(long durationSeconds) { this.durationSeconds = durationSeconds; }
}
