package com.gym.management.service;

import com.gym.management.model.Member;
import com.gym.management.model.Trainer;
import com.gym.management.model.User;
import com.gym.management.model.UserRole;
import com.gym.management.repository.UserRepository;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, @Lazy PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User createUser(String username, String rawPassword, String fullName, UserRole role) {
        String encodedPassword = passwordEncoder.encode(rawPassword);
        User user = new User(username, encodedPassword, fullName, role);
        return userRepository.save(user);
    }

    public User createUserForMember(Member member, String rawPassword) {
        String encodedPassword = passwordEncoder.encode(rawPassword);
        User user = new User(member.getEmail(), encodedPassword, member.getFullName(), UserRole.ROLE_MEMBER);
        user.setMember(member);
        return userRepository.save(user);
    }

    public User createUserForTrainer(Trainer trainer, String rawPassword) {
        String encodedPassword = passwordEncoder.encode(rawPassword);
        User user = new User(trainer.getEmail(), encodedPassword, trainer.getFullName(), UserRole.ROLE_TRAINER);
        user.setTrainer(trainer);
        return userRepository.save(user);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<Member> findMemberByUsername(String username) {
        return userRepository.findByUsername(username).map(User::getMember);
    }

    public Optional<Trainer> findTrainerByUsername(String username) {
        return userRepository.findByUsername(username).map(User::getTrainer);
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }
}
