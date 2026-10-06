package com.quizora.backend.controller;

import com.quizora.backend.domain.PlanType;
import com.quizora.backend.domain.User;
import com.quizora.backend.dto.AuthDtos;
import com.quizora.backend.exception.BadRequestException;
import com.quizora.backend.exception.NotFoundException;
import com.quizora.backend.security.CurrentUser;
import com.quizora.backend.service.SubscriptionService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/subscriptions")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;
    private final CurrentUser currentUser;

    public SubscriptionController(SubscriptionService subscriptionService, CurrentUser currentUser) {
        this.subscriptionService = subscriptionService;
        this.currentUser = currentUser;
    }

    @GetMapping("/me")
    public AuthDtos.SubscriptionDto me() {
        User user = currentUser.get();
        return subscriptionService.findByUser(user.getId())
                .map(s -> new AuthDtos.SubscriptionDto(s.getPlan().name(), s.getStatus().name(),
                        s.getStartDate(), s.getEndDate(),
                        Math.max(0, java.time.temporal.ChronoUnit.DAYS.between(
                                java.time.LocalDate.now(), s.getEndDate()))))
                .orElseThrow(() -> new NotFoundException("No subscription found for this account"));
    }

    /**
     * Individual subscription purchase.
     * MOCK payment: activates immediately. Replace with Paystack/Mobile Money verification later.
     */
    @PostMapping("/subscribe")
    public AuthDtos.SubscriptionDto subscribe(@Valid @RequestBody AuthDtos.SubscribeRequest request) {
        PlanType plan;
        try {
            plan = PlanType.valueOf(request.plan().trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Plan must be MONTHLY or YEARLY");
        }
        User user = currentUser.get();
        var subscription = subscriptionService.activate(user, plan, null, "MOCK-PAY-" + System.currentTimeMillis());
        return new AuthDtos.SubscriptionDto(subscription.getPlan().name(), subscription.getStatus().name(),
                subscription.getStartDate(), subscription.getEndDate(),
                Math.max(0, java.time.temporal.ChronoUnit.DAYS.between(
                        java.time.LocalDate.now(), subscription.getEndDate())));
    }
}
