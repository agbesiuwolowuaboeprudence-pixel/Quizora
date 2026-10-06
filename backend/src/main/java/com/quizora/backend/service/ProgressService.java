package com.quizora.backend.service;

import com.quizora.backend.domain.AttemptStatus;
import com.quizora.backend.domain.QuizAttempt;
import com.quizora.backend.domain.User;
import com.quizora.backend.dto.ProgressDtos;
import com.quizora.backend.dto.QuizDtos;
import com.quizora.backend.dto.Mappers;
import com.quizora.backend.repository.QuizAttemptRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ProgressService {

    private final QuizAttemptRepository attemptRepository;

    public ProgressService(QuizAttemptRepository attemptRepository) {
        this.attemptRepository = attemptRepository;
    }

    @Transactional(readOnly = true)
    public ProgressDtos.ProgressResponse progress(User user) {
        Long userId = user.getId();

        ProgressDtos.TotalsDto totals = attemptRepository.findTotalsByUser(userId, AttemptStatus.IN_PROGRESS)
                .stream().findFirst()
                .map(row -> new ProgressDtos.TotalsDto(
                        (Long) row[0], (Long) row[1], (Long) row[2],
                        ((Long) row[1]) == 0 ? 0 : Math.round((Long) row[2] * 1000.0 / (Long) row[1]) / 10.0,
                        (Long) row[3]))
                .orElseGet(() -> new ProgressDtos.TotalsDto(0, 0, 0, 0, 0));

        List<ProgressDtos.CourseProgressDto> perCourse = attemptRepository
                .findCourseRollupByUser(userId, AttemptStatus.IN_PROGRESS)
                .stream()
                .map(row -> new ProgressDtos.CourseProgressDto(
                        (Long) row[0], (String) row[1], (Long) row[2],
                        (Long) row[3], (Long) row[4],
                        ((Long) row[3]) == 0 ? 0 : Math.round((Long) row[4] * 1000.0 / (Long) row[3]) / 10.0,
                        Math.round(((Double) row[6]) * 10.0) / 10.0,
                        (Long) row[5]))
                .toList();

        List<ProgressDtos.TrendPointDto> trend = attemptRepository.findScoresByUser(userId)
                .stream()
                .map(row -> new ProgressDtos.TrendPointDto(
                        ((LocalDateTime) row[0]).toLocalDate(),
                        1,
                        Math.round(((Number) row[1]).doubleValue() * 10.0) / 10.0))
                .collect(Collectors.groupingBy(ProgressDtos.TrendPointDto::date,
                        Collectors.summarizingDouble(ProgressDtos.TrendPointDto::averageScorePercent)))
                .entrySet().stream()
                .map(e -> new ProgressDtos.TrendPointDto(
                        e.getKey(),
                        (int) e.getValue().getCount(),
                        Math.round(e.getValue().getAverage() * 10.0) / 10.0))
                .sorted(Comparator.comparing(ProgressDtos.TrendPointDto::date))
                .toList();

        List<QuizDtos.AttemptSummary> recent = attemptRepository
                .findByUser_IdOrderByStartedAtDesc(userId,
                        org.springframework.data.domain.PageRequest.of(0, 5))
                .getContent()
                .stream()
                .map(Mappers::attemptSummary)
                .toList();

        return new ProgressDtos.ProgressResponse(totals, perCourse, trend, recent);
    }

    @Transactional(readOnly = true)
    public List<QuizDtos.AttemptSummary> attemptHistory(User user, int page, int size) {
        return attemptRepository
                .findByUser_IdOrderByStartedAtDesc(user.getId(),
                        org.springframework.data.domain.PageRequest.of(page, Math.min(size, 100)))
                .getContent()
                .stream()
                .map(Mappers::attemptSummary)
                .toList();
    }
}
