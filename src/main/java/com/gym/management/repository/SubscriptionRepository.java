package com.gym.management.repository;

import com.gym.management.model.Subscription;
import com.gym.management.model.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    List<Subscription> findByMemberIdOrderByStartDateDesc(Long memberId);

    List<Subscription> findByStatus(SubscriptionStatus status);

    long countByStatus(SubscriptionStatus status);

    @Query("SELECT COALESCE(SUM(s.amountPaid), 0.0) FROM Subscription s WHERE s.paymentStatus = com.gym.management.model.PaymentStatus.PAID")
    Double findTotalRevenue();

    @Query("SELECT s FROM Subscription s WHERE s.status = com.gym.management.model.SubscriptionStatus.ACTIVE AND s.endDate BETWEEN :start AND :end")
    List<Subscription> findExpiringSoon(@Param("start") LocalDate start, @Param("end") LocalDate end);

    List<Subscription> findTop10ByOrderByCreatedAtDesc();
}
