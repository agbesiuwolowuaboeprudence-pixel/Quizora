package com.quizora.backend.service;

import com.quizora.backend.domain.LicenseCode;
import com.quizora.backend.domain.PlanType;
import com.quizora.backend.domain.Subscription;
import com.quizora.backend.domain.SubscriptionStatus;
import com.quizora.backend.domain.User;
import com.quizora.backend.exception.BadRequestException;
import com.quizora.backend.exception.ForbiddenException;
import com.quizora.backend.repository.SubscriptionRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

@Service
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final int trialDays;

    public SubscriptionService(SubscriptionRepository subscriptionRepository,
                               @Value("${quizora.subscription.trial-days}") int trialDays) {
        this.subscriptionRepository = subscriptionRepository;
        this.trialDays = trialDays;
    }

    /** Finds the user's subscription, lazily flipping it to EXPIRED once past its end date. */
    public Optional<Subscription> findByUser(Long userId) {
        Optional<Subscription> found = subscriptionRepository.findByUserId(userId);
        found.ifPresent(this::lazyExpire);
        return found;
    }

    /** Guards features that require an active subscription. */
    public Subscription requireActive(User user) {
        Subscription subscription = findByUser(user.getId()).orElseThrow(() ->
                new ForbiddenException("You have no subscription yet. Please subscribe to start practising."));
        if (!subscription.isActiveNow()) {
            throw new ForbiddenException("Your subscription ended on " + subscription.getEndDate()
                    + ". Please renew to continue practising.");
        }
        return subscription;
    }

    public Subscription createTrial(User user) {
        LocalDate today = LocalDate.now();
        return subscriptionRepository.save(new Subscription(user, PlanType.TRIAL,
                SubscriptionStatus.ACTIVE, today, today.plusDays(trialDays), "FREE-TRIAL"));
    }

    /** Converts (or creates) the user's subscription as an institutional one tied to a licence. */
    public Subscription createInstitutional(User user, LicenseCode license) {
        LocalDate start = LocalDate.now().isBefore(license.getValidFrom())
                ? license.getValidFrom() : LocalDate.now();
        Subscription subscription = subscriptionRepository.findByUserId(user.getId())
                .orElseGet(() -> new Subscription(user, PlanType.INSTITUTIONAL,
                        SubscriptionStatus.ACTIVE, start, license.getValidUntil(),
                        "LIC-" + license.getCode()));
        subscription.setPlan(PlanType.INSTITUTIONAL);
        subscription.setStatus(SubscriptionStatus.ACTIVE);
        subscription.setStartDate(start);
        subscription.setEndDate(license.getValidUntil());
        subscription.setPaymentRef("LIC-" + license.getCode());
        return subscriptionRepository.save(subscription);
    }

    /**
     * Activates (or extends) a paid plan.
     * MOCK payment for now - swap the paymentRef handling for Paystack/Mobile Money verification later.
     */
    public Subscription activate(User user, PlanType plan, Integer days, String paymentRef) {
        if (plan != PlanType.MONTHLY && plan != PlanType.YEARLY) {
            throw new BadRequestException("Plan must be MONTHLY or YEARLY");
        }
        int duration = days != null ? days : (plan == PlanType.YEARLY ? 365 : 30);
        LocalDate today = LocalDate.now();

        java.util.Optional<Subscription> existing = subscriptionRepository.findByUserId(user.getId());
        Subscription subscription = existing.orElseGet(() -> {
            Subscription fresh = new Subscription();
            fresh.setUser(user);
            return fresh;
        });

        // extend from the current end date only when an active subscription already exists
        boolean extendFromCurrentEnd = existing.isPresent()
                && subscription.getStatus() == SubscriptionStatus.ACTIVE
                && subscription.getEndDate() != null
                && subscription.getEndDate().isAfter(today);
        LocalDate base = extendFromCurrentEnd ? subscription.getEndDate() : today;

        subscription.setPlan(plan);
        subscription.setStatus(SubscriptionStatus.ACTIVE);
        subscription.setStartDate(base);
        subscription.setEndDate(base.plusDays(duration));
        subscription.setPaymentRef(paymentRef);
        return subscriptionRepository.save(subscription);
    }

    private void lazyExpire(Subscription subscription) {
        if (subscription.getStatus() == SubscriptionStatus.ACTIVE
                && LocalDate.now().isAfter(subscription.getEndDate())) {
            subscription.setStatus(SubscriptionStatus.EXPIRED);
            subscriptionRepository.save(subscription);
        }
    }

    /** Nightly sweep so subscriptions never sit ACTIVE past their end date. */
    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void expireEndedSubscriptions() {
        subscriptionRepository.expireEnded(SubscriptionStatus.ACTIVE,
                SubscriptionStatus.EXPIRED, LocalDate.now());
    }
}
