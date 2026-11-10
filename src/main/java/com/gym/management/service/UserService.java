package com.gym.management.service;

import com.gym.management.dto.RegistrationDto;
import com.gym.management.model.*;
import com.gym.management.repository.MemberRepository;
import com.gym.management.repository.SubscriptionRepository;
import com.gym.management.repository.UserRepository;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final MemberRepository memberRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       MemberRepository memberRepository,
                       SubscriptionRepository subscriptionRepository,
                       @Lazy PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.memberRepository = memberRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User createUser(String username, String rawPassword, String fullName, UserRole role) {
        String cleanUsername = username != null ? username.trim().toLowerCase() : "";
        String encodedPassword = passwordEncoder.encode(rawPassword);
        User user = new User(cleanUsername, encodedPassword, fullName, role);
        return userRepository.save(user);
    }

    public User createUserForMember(Member member, String rawPassword) {
        String cleanEmail = member.getEmail() != null ? member.getEmail().trim().toLowerCase() : "";
        String encodedPassword = passwordEncoder.encode(rawPassword);
        User user = new User(cleanEmail, encodedPassword, member.getFullName(), UserRole.ROLE_MEMBER);
        user.setMember(member);
        return userRepository.save(user);
    }

    public User createUserForTrainer(Trainer trainer, String rawPassword) {
        String cleanEmail = trainer.getEmail() != null ? trainer.getEmail().trim().toLowerCase() : "";
        String encodedPassword = passwordEncoder.encode(rawPassword);
        User user = new User(cleanEmail, encodedPassword, trainer.getFullName(), UserRole.ROLE_TRAINER);
        user.setTrainer(trainer);
        return userRepository.save(user);
    }

    public Member registerNewMember(RegistrationDto dto, MembershipPlan plan) {
        String cleanEmail = dto.getEmail().trim().toLowerCase();
        Member member = new Member();
        member.setFirstName(dto.getFirstName().trim());
        member.setLastName(dto.getLastName().trim());
        member.setEmail(cleanEmail);
        member.setPhone(dto.getPhone().trim());
        member.setGender(dto.getGender());
        member.setDateOfBirth(dto.getDateOfBirth());
        member.setEmergencyContactName(dto.getEmergencyContactName());
        member.setEmergencyContactPhone(dto.getEmergencyContactPhone());
        member.setStatus(MemberStatus.ACTIVE);
        member.setJoinDate(LocalDate.now());

        Member savedMember = memberRepository.save(member);

        if (plan != null) {
            LocalDate startDate = LocalDate.now();
            LocalDate endDate = startDate.plusDays(plan.getDurationDays());
            Subscription subscription = new Subscription(savedMember, plan, startDate, endDate, plan.getPrice(), PaymentStatus.PAID);
            subscriptionRepository.save(subscription);
        }

        String encodedPassword = passwordEncoder.encode(dto.getPassword());
        User user = new User(cleanEmail, encodedPassword, savedMember.getFullName(), UserRole.ROLE_MEMBER);
        user.setMember(savedMember);
        userRepository.save(user);

        return savedMember;
    }

    public void syncMemberUserEmail(Member member) {
        if (member == null || member.getId() == null) return;
        userRepository.findByMemberId(member.getId()).ifPresent(user -> {
            user.setUsername(member.getEmail().trim().toLowerCase());
            user.setFullName(member.getFullName());
            userRepository.save(user);
        });
    }

    public void syncTrainerUserEmail(Trainer trainer) {
        if (trainer == null || trainer.getId() == null) return;
        userRepository.findByTrainerId(trainer.getId()).ifPresent(user -> {
            user.setUsername(trainer.getEmail().trim().toLowerCase());
            user.setFullName(trainer.getFullName());
            userRepository.save(user);
        });
    }

    public void deleteUserByMemberId(Long memberId) {
        if (memberId == null) return;
        userRepository.findByMemberId(memberId).ifPresent(userRepository::delete);
    }

    public void deleteUserByTrainerId(Long trainerId) {
        if (trainerId == null) return;
        userRepository.findByTrainerId(trainerId).ifPresent(userRepository::delete);
    }

    public boolean existsByUsername(String username) {
        if (username == null || username.trim().isEmpty()) return false;
        return userRepository.existsByUsernameIgnoreCase(username.trim());
    }

    public Optional<User> findByUsername(String username) {
        if (username == null || username.trim().isEmpty()) return Optional.empty();
        return userRepository.findByUsernameIgnoreCase(username.trim());
    }

    public Optional<Member> findMemberByUsername(String username) {
        if (username == null || username.trim().isEmpty()) return Optional.empty();
        return userRepository.findByUsernameIgnoreCase(username.trim()).map(User::getMember);
    }

    public Optional<Trainer> findTrainerByUsername(String username) {
        if (username == null || username.trim().isEmpty()) return Optional.empty();
        return userRepository.findByUsernameIgnoreCase(username.trim()).map(User::getTrainer);
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }
}
