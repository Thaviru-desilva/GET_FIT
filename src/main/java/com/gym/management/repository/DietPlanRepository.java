package com.gym.management.repository;

import com.gym.management.model.DietPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DietPlanRepository extends JpaRepository<DietPlan, Long> {

    List<DietPlan> findByMemberIdOrderByCreatedAtDesc(Long memberId);

    List<DietPlan> findByTrainerIdOrderByCreatedAtDesc(Long trainerId);

    Optional<DietPlan> findFirstByMemberIdAndStatusOrderByCreatedAtDesc(Long memberId, String status);

    long countByTrainerId(Long trainerId);
}
