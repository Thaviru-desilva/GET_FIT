package com.gym.management.repository;

import com.gym.management.model.Member;
import com.gym.management.model.Trainer;
import com.gym.management.model.User;
import com.gym.management.model.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    List<User> findByRole(UserRole role);

    Optional<User> findByTrainer(Trainer trainer);

    Optional<User> findByMember(Member member);
}
