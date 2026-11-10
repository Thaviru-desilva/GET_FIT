package com.gym.management.service;

import com.gym.management.model.Member;
import com.gym.management.model.MemberProgress;
import com.gym.management.repository.MemberProgressRepository;
import com.gym.management.repository.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class MemberProgressService {

    private final MemberProgressRepository progressRepository;
    private final MemberRepository memberRepository;

    public MemberProgressService(MemberProgressRepository progressRepository, MemberRepository memberRepository) {
        this.progressRepository = progressRepository;
        this.memberRepository = memberRepository;
    }

    public MemberProgress logProgress(Long memberId,
                                      Double weightKg,
                                      Double heightCm,
                                      String bodyType,
                                      Double workoutHours,
                                      Double bodyFatPercentage,
                                      String targetGoal,
                                      String notes,
                                      String loggedBy) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Member not found with ID: " + memberId));

        if (weightKg == null || weightKg < 20.0 || weightKg > 350.0) {
            throw new IllegalArgumentException("Weight must be between 20 kg and 350 kg.");
        }
        if (heightCm == null || heightCm < 80.0 || heightCm > 250.0) {
            throw new IllegalArgumentException("Height must be between 80 cm and 250 cm.");
        }
        if (workoutHours != null && (workoutHours < 0.0 || workoutHours > 100.0)) {
            throw new IllegalArgumentException("Workout hours per week must be between 0 and 100 hours.");
        }
        if (bodyFatPercentage != null && (bodyFatPercentage < 3.0 || bodyFatPercentage > 60.0)) {
            throw new IllegalArgumentException("Body fat percentage must be between 3% and 60%.");
        }

        MemberProgress progress = new MemberProgress(
                member,
                LocalDate.now(),
                weightKg,
                heightCm,
                bodyType,
                workoutHours,
                targetGoal,
                notes,
                loggedBy != null ? loggedBy : "MEMBER"
        );
        progress.setBodyFatPercentage(bodyFatPercentage);
        return progressRepository.save(progress);
    }

    public MemberProgress save(MemberProgress progress) {
        return progressRepository.save(progress);
    }

    public List<MemberProgress> getProgressHistory(Long memberId) {
        return progressRepository.findByMemberIdOrderByRecordDateDescIdDesc(memberId);
    }

    public Optional<MemberProgress> getLatestProgress(Long memberId) {
        return progressRepository.findTopByMemberIdOrderByRecordDateDescIdDesc(memberId);
    }

    public void deleteById(Long id) {
        progressRepository.deleteById(id);
    }
}
