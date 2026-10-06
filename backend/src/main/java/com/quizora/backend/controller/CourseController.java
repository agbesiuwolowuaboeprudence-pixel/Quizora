package com.quizora.backend.controller;

import com.quizora.backend.dto.CourseDtos;
import com.quizora.backend.service.CourseService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
public class CourseController {

    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    /** Courses page - all JHS subjects available on the platform. */
    @GetMapping
    public List<CourseDtos.CourseResponse> list() {
        return courseService.list();
    }

    @GetMapping("/{id}")
    public CourseDtos.CourseResponse get(@PathVariable Long id) {
        return courseService.get(id);
    }

    /** Year selection - past-question years available for a course. */
    @GetMapping("/{id}/years")
    public List<CourseDtos.YearResponse> years(@PathVariable Long id) {
        return courseService.years(id);
    }
}
