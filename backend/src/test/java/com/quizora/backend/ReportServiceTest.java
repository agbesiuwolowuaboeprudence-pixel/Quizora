package com.quizora.backend;

import com.quizora.backend.dto.InstitutionDtos;
import com.quizora.backend.service.InstitutionService;
import com.quizora.backend.service.ReportService;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ReportServiceTest {

    @Test
    void csvTextFieldsCannotInjectSpreadsheetFormulas() {
        InstitutionService institutionService = mock(InstitutionService.class);
        InstitutionDtos.StudentDto student = new InstitutionDtos.StudentDto(
                1L, "=HYPERLINK(\"https://example.com\")", "student@example.com",
                null, 0, 0, 0, 0, null, "ACTIVE");
        InstitutionDtos.InstitutionTotalsDto totals =
                new InstitutionDtos.InstitutionTotalsDto(1, 0, 0, 0, 0, 1, 1);
        when(institutionService.summary(1L, null)).thenReturn(
                new InstitutionDtos.ReportSummaryResponse(1L, "Test", totals, List.of(student)));

        String csv = new String(new ReportService(institutionService).toCsv(1L, null),
                java.nio.charset.StandardCharsets.UTF_8);

        assertThat(csv).contains("'=HYPERLINK(\"\"https://example.com\"\")");
    }
}
