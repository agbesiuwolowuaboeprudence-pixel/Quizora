package com.quizora.backend.service;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.quizora.backend.domain.User;
import com.quizora.backend.dto.InstitutionDtos;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** Downloadable institutional performance reports (CSV / PDF). */
@Service
public class ReportService {

    private final InstitutionService institutionService;

    public ReportService(InstitutionService institutionService) {
        this.institutionService = institutionService;
    }

    public byte[] toCsv(Long institutionId, User actor) {
        InstitutionDtos.ReportSummaryResponse report = institutionService.summary(institutionId, actor);

        StringBuilder csv = new StringBuilder();
        csv.append("Student Name,Email,Attempts,Questions Attempted,Accuracy %,Average Score %,Last Active,Subscription\n");
        for (InstitutionDtos.StudentDto s : report.students()) {
            csv.append(escape(s.fullName())).append(',')
               .append(escape(s.email())).append(',')
               .append(s.attempts()).append(',')
               .append(s.questionsAttempted()).append(',')
               .append(s.accuracyPercent()).append(',')
               .append(s.averageScorePercent()).append(',')
               .append(s.lastActiveAt() != null ? s.lastActiveAt().toLocalDate().toString() : "").append(',')
               .append(escape(s.subscriptionStatus())).append('\n');
        }
        csv.append('\n');
        csv.append("Total Students,").append(report.totals().students()).append('\n');
        csv.append("Active Students (30 days),").append(report.totals().studentsActive30Days()).append('\n');
        csv.append("Total Attempts,").append(report.totals().attempts()).append('\n');
        csv.append("Overall Accuracy %,").append(report.totals().accuracyPercent()).append('\n');

        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    public byte[] toPdf(Long institutionId, User actor) throws DocumentException {
        InstitutionDtos.ReportSummaryResponse report = institutionService.summary(institutionId, actor);

        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
        Font metaFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 40, 40, 40, 40);
        PdfWriter.getInstance(document, out);
        document.open();

        document.add(new Paragraph("Quizora - Institutional Performance Report", titleFont));
        document.add(new Paragraph(report.institutionName(), metaFont));
        document.add(new Paragraph("Generated: "
                + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))));
        document.add(new Paragraph(" "));

        InstitutionDtos.InstitutionTotalsDto t = report.totals();
        document.add(new Paragraph(String.format(
                "Students: %d   |   Active (30 days): %d   |   Attempts: %d   |   Questions: %d   |   Accuracy: %s%%   |   Seats: %d/%d",
                t.students(), t.studentsActive30Days(), t.attempts(), t.questionsAttempted(),
                t.accuracyPercent(), t.licenseSeatsUsed(), t.licenseSeatsTotal()), metaFont));
        document.add(new Paragraph(" "));

        PdfPTable table = new PdfPTable(new float[]{2.4f, 2.6f, 0.8f, 1f, 1f, 1.2f, 1.2f});
        table.setWidthPercentage(100);
        for (String header : new String[]{"Student", "Email", "Att.", "Acc %", "Avg %", "Last active", "Subscription"}) {
            PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
            cell.setBackgroundColor(new Color(230, 230, 230));
            cell.setPadding(4);
            table.addCell(cell);
        }
        for (InstitutionDtos.StudentDto s : report.students()) {
            table.addCell(cell(s.fullName()));
            table.addCell(cell(s.email()));
            cell(table, String.valueOf(s.attempts()));
            cell(table, String.valueOf(s.accuracyPercent()));
            cell(table, String.valueOf(s.averageScorePercent()));
            cell(table, s.lastActiveAt() != null ? s.lastActiveAt().toLocalDate().toString() : "-");
            cell(table, s.subscriptionStatus());
        }
        document.add(table);
        document.close();

        return out.toByteArray();
    }

    private PdfPCell cell(String value) {
        PdfPCell c = new PdfPCell(new Phrase(value == null ? "" : value));
        c.setPadding(4);
        return c;
    }

    private void cell(PdfPTable table, String value) {
        table.addCell(cell(value));
    }

    private String escape(String value) {
        if (value == null) return "";
        String v = value.replace("\"", "\"\"");
        if (v.contains(",") || v.contains("\"") || v.contains("\n")) {
            return "\"" + v + "\"";
        }
        return v;
    }
}
