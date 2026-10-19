package com.gym.management.repository;

import com.gym.management.model.Trainer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TrainerRepository extends JpaRepository<Trainer, Long> {

    List<Trainer> findBySpecializationContainingIgnoreCase(String specialization);
}
