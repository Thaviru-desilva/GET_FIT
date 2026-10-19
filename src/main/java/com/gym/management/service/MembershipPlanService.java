package com.gym.management.service;

import com.gym.management.model.MembershipPlan;
import com.gym.management.repository.MembershipPlanRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class MembershipPlanService {

    private final MembershipPlanRepository planRepository;

    public MembershipPlanService(MembershipPlanRepository planRepository) {
        this.planRepository = planRepository;
    }

    public List<MembershipPlan> findAll() {
        return planRepository.findAll();
    }

    public List<MembershipPlan> findActivePlans() {
        return planRepository.findByActiveTrue();
    }

    public Optional<MembershipPlan> findById(Long id) {
        return planRepository.findById(id);
    }

    public MembershipPlan save(MembershipPlan plan) {
        return planRepository.save(plan);
    }

    public void deleteById(Long id) {
        planRepository.deleteById(id);
    }

    public long count() {
        return planRepository.count();
    }
}
