package com.quizora.backend.service;

import com.quizora.backend.domain.Course;
import com.quizora.backend.dto.CourseDtos;
import com.quizora.backend.exception.NotFoundException;
import com.quizora.backend.repository.CourseRepository;
import com.quizora.backend.repository.QuestionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CourseService {

    private final CourseRepository courseRepository;
    private final QuestionRepository questionRepository;

    public CourseService(CourseRepository courseRepository, QuestionRepository questionRepository) {
        this.courseRepository = courseRepository;
        this.questionRepository = questionRepository;
    }

    /** All active courses with question counts and available past-question years. */
    public List<CourseDtos.CourseResponse> list() {
        return courseRepository.findByArchivedFalseOrderByNameAsc().stream()
                .map(this::toResponse)
                .toList();
    }

    public CourseDtos.CourseResponse get(Long courseId) {
        return toResponse(requireCourse(courseId));
    }

    /** Year-selection screen: which BECE years exist for this subject. */
    public List<CourseDtos.YearResponse> years(Long courseId) {
        requireCourse(courseId);
        return questionRepository.findYearsByCourseId(courseId).stream()
                .map(year -> new CourseDtos.YearResponse(year,
                        questionRepository.countByCourseIdAndYear(courseId, year)))
                .toList();
    }

    public Course requireCourse(Long courseId) {
        return courseRepository.findById(courseId)
                .orElseThrow(() -> new NotFoundException("Course not found"));
    }

    private CourseDtos.CourseResponse toResponse(Course course) {
        List<Integer> years = questionRepository.findYearsByCourseId(course.getId());
        long questionCount = questionRepository.countByCourseId(course.getId());
        return new CourseDtos.CourseResponse(
                course.getId(), course.getName(), course.getCode(),
                course.getDescription(), course.getLevel(), questionCount, years);
    }
}
