package com.gym.management.service;

import com.gym.management.model.Trainer;
import com.gym.management.repository.TrainerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class TrainerService {

    private final TrainerRepository trainerRepository;

    public TrainerService(TrainerRepository trainerRepository) {
        this.trainerRepository = trainerRepository;
    }

    public List<Trainer> findAll() {
        return trainerRepository.findAll();
    }

    public Optional<Trainer> findById(Long id) {
        return trainerRepository.findById(id);
    }

    public Trainer save(Trainer trainer) {
        return trainerRepository.save(trainer);
    }

    public void deleteById(Long id) {
        trainerRepository.deleteById(id);
    }

    public long count() {
        return trainerRepository.count();
    }
}
