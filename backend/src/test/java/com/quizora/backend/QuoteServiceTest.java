package com.quizora.backend;

import com.quizora.backend.domain.MotivationalQuote;
import com.quizora.backend.dto.DashboardDtos;
import com.quizora.backend.repository.MotivationalQuoteRepository;
import com.quizora.backend.service.QuoteService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.domain.Sort;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class QuoteServiceTest {

    @Test
    @DisplayName("the daily quote is stable for the whole day and expires at midnight")
    void servesOneQuotePerDay() {
        MotivationalQuoteRepository repository = Mockito.mock(MotivationalQuoteRepository.class);
        Mockito.when(repository.findAll(Mockito.any(Sort.class))).thenReturn(List.of(
                new MotivationalQuote("Quote one", "Author A"),
                new MotivationalQuote("Quote two", "Author B"),
                new MotivationalQuote("Quote three", "Author C")));

        QuoteService service = new QuoteService(repository);
        DashboardDtos.QuoteDto first = service.todayQuote();
        DashboardDtos.QuoteDto second = service.todayQuote();

        // deterministic within the same day
        assertThat(first.text()).isEqualTo(second.text());
        assertThat(first.text()).isIn("Quote one", "Quote two", "Quote three");
        assertThat(first.validUntil()).isEqualTo(LocalDate.now().plusDays(1).atStartOfDay());
    }

    @Test
    @DisplayName("falls back gracefully when no quotes are seeded")
    void fallbackWhenEmpty() {
        MotivationalQuoteRepository repository = Mockito.mock(MotivationalQuoteRepository.class);
        Mockito.when(repository.findAll(Mockito.any(Sort.class))).thenReturn(List.of());

        DashboardDtos.QuoteDto quote = new QuoteService(repository).todayQuote();
        assertThat(quote.text()).isNotBlank();
        assertThat(quote.author()).isNotBlank();
    }
}
