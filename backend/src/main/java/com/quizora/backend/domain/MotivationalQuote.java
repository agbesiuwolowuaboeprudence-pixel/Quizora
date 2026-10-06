package com.quizora.backend.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "motivational_quotes")
public class MotivationalQuote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String text;

    @Column(nullable = false, length = 100)
    private String author;

    public MotivationalQuote() {
    }

    public MotivationalQuote(String text, String author) {
        this.text = text;
        this.author = author;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
}
