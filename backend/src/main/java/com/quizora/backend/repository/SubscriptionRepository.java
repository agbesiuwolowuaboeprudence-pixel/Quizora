package com.quizora.backend.repository;

import com.quizora.backend.domain.Subscription;
import com.quizora.backend.domain.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    Optional<Subscription> findByUserId(Long userId);

    @Modifying
    @Query("update Subscription s set s.status = :to where s.status = :from and s.endDate < :today")
    int expireEnded(@Param("from") SubscriptionStatus from,
                    @Param("to") SubscriptionStatus to,
                    @Param("today") LocalDate today);
}
