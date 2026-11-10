package com.gym.management.service;

import com.gym.management.model.Member;
import com.gym.management.model.MemberStatus;
import com.gym.management.model.MembershipPlan;
import com.gym.management.model.PaymentStatus;
import com.gym.management.model.Subscription;
import com.gym.management.model.SubscriptionStatus;
import com.gym.management.repository.MemberRepository;
import com.gym.management.repository.MembershipPlanRepository;
import com.gym.management.repository.SubscriptionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final MemberRepository memberRepository;
    private final MembershipPlanRepository planRepository;

    public SubscriptionService(SubscriptionRepository subscriptionRepository,
                               MemberRepository memberRepository,
                               MembershipPlanRepository planRepository) {
        this.subscriptionRepository = subscriptionRepository;
        this.memberRepository = memberRepository;
        this.planRepository = planRepository;
    }

    public void updateExpiredSubscriptions() {
        List<Subscription> activeSubscriptions = subscriptionRepository.findByStatus(SubscriptionStatus.ACTIVE);
        LocalDate today = LocalDate.now();
        for (Subscription s : activeSubscriptions) {
            if (s.getEndDate() != null && s.getEndDate().isBefore(today)) {
                s.setStatus(SubscriptionStatus.EXPIRED);
                subscriptionRepository.save(s);
            }
        }
    }

    public List<Subscription> findAll() {
        updateExpiredSubscriptions();
        return subscriptionRepository.findAll();
    }

    public Optional<Subscription> findById(Long id) {
        return subscriptionRepository.findById(id);
    }

    public List<Subscription> findByMemberId(Long memberId) {
        updateExpiredSubscriptions();
        return subscriptionRepository.findByMemberIdOrderByStartDateDesc(memberId);
    }

    public Subscription createSubscription(Long memberId, Long planId, LocalDate startDate, Double amountPaid, PaymentStatus paymentStatus) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid member ID: " + memberId));
        MembershipPlan plan = planRepository.findById(planId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid plan ID: " + planId));

        if (startDate == null) {
            startDate = LocalDate.now();
        }
        LocalDate endDate = startDate.plusDays(plan.getDurationDays());

        if (amountPaid == null) {
            amountPaid = plan.getPrice();
        }

        Subscription subscription = new Subscription(member, plan, startDate, endDate, amountPaid, paymentStatus != null ? paymentStatus : PaymentStatus.PAID);
        subscription = subscriptionRepository.save(subscription);

        // Update member status to ACTIVE if subscription is active
        if (subscription.getStatus() == SubscriptionStatus.ACTIVE) {
            member.setStatus(MemberStatus.ACTIVE);
            memberRepository.save(member);
        }

        return subscription;
    }

    public void cancelSubscription(Long id) {
        subscriptionRepository.findById(id).ifPresent(s -> {
            s.setStatus(SubscriptionStatus.CANCELLED);
            subscriptionRepository.save(s);
        });
    }

    public long countActiveSubscriptions() {
        updateExpiredSubscriptions();
        return subscriptionRepository.countByStatus(SubscriptionStatus.ACTIVE);
    }

    public Double getTotalRevenue() {
        return subscriptionRepository.findTotalRevenue();
    }

    public List<Subscription> getExpiringSoon() {
        updateExpiredSubscriptions();
        LocalDate today = LocalDate.now();
        return subscriptionRepository.findExpiringSoon(today, today.plusDays(7));
    }

    public List<Subscription> getRecentSubscriptions() {
        return subscriptionRepository.findTop10ByOrderByCreatedAtDesc();
    }
}
