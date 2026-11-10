package com.gym.management.service;

import com.gym.management.model.DietPlan;
import com.gym.management.model.Member;
import com.gym.management.model.Trainer;
import com.gym.management.model.WorkoutPlan;
import com.gym.management.repository.DietPlanRepository;
import com.gym.management.repository.MemberRepository;
import com.gym.management.repository.TrainerRepository;
import com.gym.management.repository.WorkoutPlanRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class FitnessPlanService {

    private final WorkoutPlanRepository workoutPlanRepository;
    private final DietPlanRepository dietPlanRepository;
    private final MemberRepository memberRepository;
    private final TrainerRepository trainerRepository;

    public FitnessPlanService(WorkoutPlanRepository workoutPlanRepository,
                              DietPlanRepository dietPlanRepository,
                              MemberRepository memberRepository,
                              TrainerRepository trainerRepository) {
        this.workoutPlanRepository = workoutPlanRepository;
        this.dietPlanRepository = dietPlanRepository;
        this.memberRepository = memberRepository;
        this.trainerRepository = trainerRepository;
    }

    // Workout Plans
    public WorkoutPlan saveWorkoutPlan(WorkoutPlan plan) {
        if ("ACTIVE".equalsIgnoreCase(plan.getStatus()) && plan.getMember() != null && plan.getMember().getId() != null) {
            List<WorkoutPlan> existingPlans = workoutPlanRepository.findByMemberIdOrderByCreatedAtDesc(plan.getMember().getId());
            for (WorkoutPlan existing : existingPlans) {
                if ("ACTIVE".equalsIgnoreCase(existing.getStatus()) && (plan.getId() == null || !existing.getId().equals(plan.getId()))) {
                    existing.setStatus("SUPERSEDED");
                    workoutPlanRepository.save(existing);
                }
            }
        }
        return workoutPlanRepository.save(plan);
    }

    public Optional<WorkoutPlan> getWorkoutPlanById(Long id) {
        return workoutPlanRepository.findById(id);
    }

    public List<WorkoutPlan> getWorkoutPlansForMember(Long memberId) {
        return workoutPlanRepository.findByMemberIdOrderByCreatedAtDesc(memberId);
    }

    public Optional<WorkoutPlan> getActiveWorkoutPlanForMember(Long memberId) {
        return workoutPlanRepository.findFirstByMemberIdAndStatusOrderByCreatedAtDesc(memberId, "ACTIVE");
    }

    public List<WorkoutPlan> getWorkoutPlansForTrainer(Long trainerId) {
        return workoutPlanRepository.findByTrainerIdOrderByCreatedAtDesc(trainerId);
    }

    public void deleteWorkoutPlan(Long id) {
        workoutPlanRepository.deleteById(id);
    }

    // Diet Plans
    public DietPlan saveDietPlan(DietPlan plan) {
        if ("ACTIVE".equalsIgnoreCase(plan.getStatus()) && plan.getMember() != null && plan.getMember().getId() != null) {
            List<DietPlan> existingPlans = dietPlanRepository.findByMemberIdOrderByCreatedAtDesc(plan.getMember().getId());
            for (DietPlan existing : existingPlans) {
                if ("ACTIVE".equalsIgnoreCase(existing.getStatus()) && (plan.getId() == null || !existing.getId().equals(plan.getId()))) {
                    existing.setStatus("SUPERSEDED");
                    dietPlanRepository.save(existing);
                }
            }
        }
        return dietPlanRepository.save(plan);
    }

    public Optional<DietPlan> getDietPlanById(Long id) {
        return dietPlanRepository.findById(id);
    }

    public List<DietPlan> getDietPlansForMember(Long memberId) {
        return dietPlanRepository.findByMemberIdOrderByCreatedAtDesc(memberId);
    }

    public Optional<DietPlan> getActiveDietPlanForMember(Long memberId) {
        return dietPlanRepository.findFirstByMemberIdAndStatusOrderByCreatedAtDesc(memberId, "ACTIVE");
    }

    public List<DietPlan> getDietPlansForTrainer(Long trainerId) {
        return dietPlanRepository.findByTrainerIdOrderByCreatedAtDesc(trainerId);
    }

    public void deleteDietPlan(Long id) {
        dietPlanRepository.deleteById(id);
    }
}
