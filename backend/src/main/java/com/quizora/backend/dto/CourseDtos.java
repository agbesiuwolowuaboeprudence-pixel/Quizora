package com.quizora.backend.dto;

import com.quizora.backend.domain.Level;

import java.util.List;

/** Request/response records for courses and year selection. */
public final class CourseDtos {

    private CourseDtos() {}

    public record CourseResponse(Long id, String name, String code, String description,
                                 Level level, long questionCount, List<Integer> years) {}

    public record YearResponse(int year, long questionCount) {}
}
