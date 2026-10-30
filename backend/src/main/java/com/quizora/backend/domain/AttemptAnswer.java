package com.quizora.backend.domain;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/** One student answer inside a quiz attempt. Correctness is computed when the answer is recorded. */
@Entity
@Table(name = "attempt_answers",
        uniqueConstraints = @UniqueConstraint(columnNames = {"attempt_id", "question_id"}))
public class AttemptAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "attempt_id")
    private QuizAttempt attempt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_id")
    private Question question;

    /** Null when the student skipped the question. */
    @Enumerated(EnumType.STRING)
    @Column(length = 5)
    private AnswerOption selectedOption;

    @Column(nullable = false)
    private boolean correct;

    @Column(nullable = false, updatable = false)
    private LocalDateTime answeredAt = LocalDateTime.now();

    @Column(nullable = false)
    private int timeSpentSeconds;

    public AttemptAnswer() {
    }

    public AttemptAnswer(QuizAttempt attempt, Question question, AnswerOption selectedOption,
                         boolean correct, int timeSpentSeconds) {
        this.attempt = attempt;
        this.question = question;
        this.selectedOption = selectedOption;
        this.correct = correct;
        this.timeSpentSeconds = timeSpentSeconds;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public QuizAttempt getAttempt() { return attempt; }
    public void setAttempt(QuizAttempt attempt) { this.attempt = attempt; }

    public Question getQuestion() { return question; }
    public void setQuestion(Question question) { this.question = question; }

    public AnswerOption getSelectedOption() { return selectedOption; }
    public void setSelectedOption(AnswerOption selectedOption) { this.selectedOption = selectedOption; }

    public boolean isCorrect() { return correct; }
    public void setCorrect(boolean correct) { this.correct = correct; }

    public LocalDateTime getAnsweredAt() { return answeredAt; }
    public void setAnsweredAt(LocalDateTime answeredAt) { this.answeredAt = answeredAt; }

    public int getTimeSpentSeconds() { return timeSpentSeconds; }
    public void setTimeSpentSeconds(int timeSpentSeconds) { this.timeSpentSeconds = timeSpentSeconds; }
}
