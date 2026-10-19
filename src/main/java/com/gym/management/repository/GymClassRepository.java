package com.gym.management.repository;

import com.gym.management.model.GymClass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GymClassRepository extends JpaRepository<GymClass, Long> {

    List<GymClass> findByTrainerId(Long trainerId);

    List<GymClass> findByDayOfWeekIgnoreCase(String dayOfWeek);
}
