package com.quizora.backend.service;

import com.quizora.backend.domain.MotivationalQuote;
import com.quizora.backend.dto.DashboardDtos;
import com.quizora.backend.repository.MotivationalQuoteRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Serves the daily motivational quote.
 * The quote changes every 24 hours: the day number selects it deterministically,
 * so every client sees the same quote on the same day.
 */
@Service
public class QuoteService {

    private static final MotivationalQuote FALLBACK =
            new MotivationalQuote("Little by little, the bird builds its nest - keep practising every day.", "Ghanaian proverb");

    private final MotivationalQuoteRepository quoteRepository;

    public QuoteService(MotivationalQuoteRepository quoteRepository) {
        this.quoteRepository = quoteRepository;
    }

    public DashboardDtos.QuoteDto todayQuote() {
        List<MotivationalQuote> quotes = quoteRepository.findAll(Sort.by("id"));
        MotivationalQuote quote;
        if (quotes.isEmpty()) {
            quote = FALLBACK;
        } else {
            int index = (int) Math.floorMod(java.time.LocalDate.now().toEpochDay(), quotes.size());
            quote = quotes.get(index);
        }
        // valid until the next 24-hour boundary (start of tomorrow, server time)
        LocalDateTime validUntil = java.time.LocalDate.now().plusDays(1).atStartOfDay();
        return new DashboardDtos.QuoteDto(quote.getText(), quote.getAuthor(), validUntil);
    }
}
