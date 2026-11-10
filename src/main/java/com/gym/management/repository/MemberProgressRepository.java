package com.gym.management.repository;

import com.gym.management.model.MemberProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MemberProgressRepository extends JpaRepository<MemberProgress, Long> {

    List<MemberProgress> findByMemberIdOrderByRecordDateDescIdDesc(Long memberId);

    Optional<MemberProgress> findTopByMemberIdOrderByRecordDateDescIdDesc(Long memberId);

    long countByMemberId(Long memberId);
}
