package com.quizora.backend.controller;

import com.quizora.backend.dto.DashboardDtos;
import com.quizora.backend.security.CurrentUser;
import com.quizora.backend.service.DashboardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;
    private final CurrentUser currentUser;

    public DashboardController(DashboardService dashboardService, CurrentUser currentUser) {
        this.dashboardService = dashboardService;
        this.currentUser = currentUser;
    }

    /** Personalised home screen: greeting, daily quote, summary analytics. */
    @GetMapping
    public DashboardDtos.DashboardResponse dashboard() {
        return dashboardService.dashboard(currentUser.get());
    }
}
