package com.quizora.backend.domain;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "subscriptions")
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", unique = true)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PlanType plan;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SubscriptionStatus status = SubscriptionStatus.PENDING;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    /** Payment reference - "MOCK-..." until Paystack/Mobile Money is integrated. */
    @Column(length = 60)
    private String paymentRef;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Subscription() {
    }

    public Subscription(User user, PlanType plan, SubscriptionStatus status,
                        LocalDate startDate, LocalDate endDate, String paymentRef) {
        this.user = user;
        this.plan = plan;
        this.status = status;
        this.startDate = startDate;
        this.endDate = endDate;
        this.paymentRef = paymentRef;
    }

    /** True when the subscription grants access right now. */
    public boolean isActiveNow() {
        return status == SubscriptionStatus.ACTIVE
                && !LocalDate.now().isAfter(endDate);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public PlanType getPlan() { return plan; }
    public void setPlan(PlanType plan) { this.plan = plan; }

    public SubscriptionStatus getStatus() { return status; }
    public void setStatus(SubscriptionStatus status) { this.status = status; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public String getPaymentRef() { return paymentRef; }
    public void setPaymentRef(String paymentRef) { this.paymentRef = paymentRef; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
