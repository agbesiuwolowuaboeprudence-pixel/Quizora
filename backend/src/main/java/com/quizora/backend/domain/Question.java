package com.quizora.backend.domain;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * A past-question. Phase 1 stores objective (MCQ) questions;
 * Phase 2 will extend this with subjective text answers and marking schemes.
 */
@Entity
@Table(name = "questions", indexes = {
        @Index(name = "idx_question_course_year", columnList = "course_id,exam_year")
})
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id")
    private Course course;

    @Column(name = "exam_year", nullable = false)
    private int year;                    // e.g. 2023 BECE paper year

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private QuestionSection section = QuestionSection.OBJECTIVE;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String questionText;

    @Column(nullable = false, length = 500)
    private String optionA;

    @Column(nullable = false, length = 500)
    private String optionB;

    @Column(nullable = false, length = 500)
    private String optionC;

    @Column(nullable = false, length = 500)
    private String optionD;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 5)
    private AnswerOption correctOption;

    @Column(columnDefinition = "TEXT")
    private String explanation;          // optional feedback shown after submission

    @Column(nullable = false)
    private int marks = 1;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Question() {
    }

    public Question(Course course, int year, QuestionSection section, String questionText,
                    String optionA, String optionB, String optionC, String optionD,
                    AnswerOption correctOption, String explanation, int marks) {
        this.course = course;
        this.year = year;
        this.section = section;
        this.questionText = questionText;
        this.optionA = optionA;
        this.optionB = optionB;
        this.optionC = optionC;
        this.optionD = optionD;
        this.correctOption = correctOption;
        this.explanation = explanation;
        this.marks = marks;
    }

    public String optionAt(AnswerOption option) {
        switch (option) {
            case A: return optionA;
            case B: return optionB;
            case C: return optionC;
            default: return optionD;
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Course getCourse() { return course; }
    public void setCourse(Course course) { this.course = course; }

    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }

    public QuestionSection getSection() { return section; }
    public void setSection(QuestionSection section) { this.section = section; }

    public String getQuestionText() { return questionText; }
    public void setQuestionText(String questionText) { this.questionText = questionText; }

    public String getOptionA() { return optionA; }
    public void setOptionA(String optionA) { this.optionA = optionA; }

    public String getOptionB() { return optionB; }
    public void setOptionB(String optionB) { this.optionB = optionB; }

    public String getOptionC() { return optionC; }
    public void setOptionC(String optionC) { this.optionC = optionC; }

    public String getOptionD() { return optionD; }
    public void setOptionD(String optionD) { this.optionD = optionD; }

    public AnswerOption getCorrectOption() { return correctOption; }
    public void setCorrectOption(AnswerOption correctOption) { this.correctOption = correctOption; }

    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }

    public int getMarks() { return marks; }
    public void setMarks(int marks) { this.marks = marks; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
