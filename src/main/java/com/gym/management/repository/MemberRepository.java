package com.gym.management.repository;

import com.gym.management.model.Member;
import com.gym.management.model.MemberStatus;
import com.gym.management.model.Trainer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

    List<Member> findByStatus(MemberStatus status);

    long countByStatus(MemberStatus status);

    List<Member> findByAssignedTrainer(Trainer trainer);

    @Query("SELECT m FROM Member m WHERE " +
           "LOWER(m.firstName) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(m.lastName) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(m.email) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "m.phone LIKE CONCAT('%', :q, '%')")
    List<Member> searchMembers(@Param("q") String query);

    java.util.Optional<Member> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);
}
