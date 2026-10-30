package com.quizora.backend.controller;

import com.quizora.backend.domain.User;
import com.quizora.backend.dto.InstitutionDtos;
import com.quizora.backend.security.CurrentUser;
import com.quizora.backend.service.InstitutionService;
import com.quizora.backend.service.ReportService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/institutions")
public class InstitutionController {

    private final InstitutionService institutionService;
    private final ReportService reportService;
    private final CurrentUser currentUser;

    public InstitutionController(InstitutionService institutionService,
                                 ReportService reportService,
                                 CurrentUser currentUser) {
        this.institutionService = institutionService;
        this.reportService = reportService;
        this.currentUser = currentUser;
    }

    /** Admin: create an institution + its admin account. */
    @PostMapping
    public InstitutionDtos.InstitutionResponse create(
            @Valid @RequestBody InstitutionDtos.CreateInstitutionRequest request) {
        return institutionService.create(request);
    }

    /** Institutional account overview (licence codes, seats, student count). */
    @GetMapping("/{id}")
    public InstitutionDtos.InstitutionResponse get(@PathVariable Long id) {
        return institutionService.get(id, currentUser.get());
    }

    /** Generate a new bulk licence code (e.g. 20 seats for a defined period). */
    @PostMapping("/{id}/licenses")
    public InstitutionDtos.LicenseDto createLicense(@PathVariable Long id,
                                                    @Valid @RequestBody InstitutionDtos.CreateLicenseRequest request) {
        return institutionService.createLicense(id, request, currentUser.get());
    }

    /** Students onboarded under the licence, each with a progress snapshot. */
    @GetMapping("/{id}/students")
    public List<InstitutionDtos.StudentDto> students(@PathVariable Long id) {
        return institutionService.students(id, currentUser.get());
    }

    /** Institutional reporting dashboard data. */
    @GetMapping("/{id}/report/summary")
    public InstitutionDtos.ReportSummaryResponse reportSummary(@PathVariable Long id) {
        return institutionService.summary(id, currentUser.get());
    }

    /** Downloadable report: ?format=csv (default) or ?format=pdf */
    @GetMapping("/{id}/report/export")
    public ResponseEntity<byte[]> export(@PathVariable Long id,
                                         @RequestParam(defaultValue = "csv") String format) throws Exception {
        User actor = currentUser.get();
        if ("pdf".equalsIgnoreCase(format)) {
            byte[] pdf = reportService.toPdf(id, actor);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"quizora-report-" + id + ".pdf\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdf);
        }
        byte[] csv = reportService.toCsv(id, actor);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"quizora-report-" + id + ".csv\"")
                .contentType(new MediaType("text", "csv", java.nio.charset.StandardCharsets.UTF_8))
                .body(csv);
    }
}
