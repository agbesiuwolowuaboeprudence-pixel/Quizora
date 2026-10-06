package com.quizora.backend.repository;

import com.quizora.backend.domain.MotivationalQuote;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MotivationalQuoteRepository extends JpaRepository<MotivationalQuote, Long> {
}
