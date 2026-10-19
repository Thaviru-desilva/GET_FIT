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
        return progressRepository.findByMemberIdOrderByRecordDateDesc(memberId);
    }

    public Optional<MemberProgress> getLatestProgress(Long memberId) {
        return progressRepository.findTopByMemberIdOrderByRecordDateDesc(memberId);
    }

    public void deleteById(Long id) {
        progressRepository.deleteById(id);
    }
}
