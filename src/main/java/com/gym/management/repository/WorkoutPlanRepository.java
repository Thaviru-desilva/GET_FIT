package com.gym.management.repository;

import com.gym.management.model.WorkoutPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WorkoutPlanRepository extends JpaRepository<WorkoutPlan, Long> {

    List<WorkoutPlan> findByMemberIdOrderByCreatedAtDesc(Long memberId);

    List<WorkoutPlan> findByTrainerIdOrderByCreatedAtDesc(Long trainerId);

    Optional<WorkoutPlan> findFirstByMemberIdAndStatusOrderByCreatedAtDesc(Long memberId, String status);

    long countByTrainerId(Long trainerId);
}
