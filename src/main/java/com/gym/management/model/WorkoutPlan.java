package com.gym.management.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "workout_plans")
public class WorkoutPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @NotNull
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "trainer_id", nullable = false)
    private Trainer trainer;

    @NotBlank(message = "Plan title is required")
    @Column(nullable = false)
    private String title;

    private String fitnessGoal; // e.g. Hypertrophy, Strength, Fat Loss, Endurance

    private String targetBodyType; // e.g. Ectomorph, Mesomorph, Endomorph, All

    private String frequency; // e.g. 4 Days / Week, 5 Days / Week

    private Integer durationWeeks = 8;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate = LocalDate.now();

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    @Column(nullable = false)
    private String status = "ACTIVE"; // ACTIVE, COMPLETED, ARCHIVED

    @Lob
    @Column(columnDefinition = "TEXT")
    private String scheduleDetails; // Daily routine breakdown (exercises, sets, reps, rests)

    @Lob
    @Column(columnDefinition = "TEXT")
    private String trainerAdvice; // Warmup, recovery, intensity guidance

    private LocalDateTime createdAt = LocalDateTime.now();

    public WorkoutPlan() {
    }

    public WorkoutPlan(Member member, Trainer trainer, String title, String fitnessGoal, String targetBodyType, String frequency, Integer durationWeeks, LocalDate startDate, LocalDate endDate, String scheduleDetails, String trainerAdvice) {
        this.member = member;
        this.trainer = trainer;
        this.title = title;
        this.fitnessGoal = fitnessGoal;
        this.targetBodyType = targetBodyType;
        this.frequency = frequency;
        this.durationWeeks = durationWeeks;
        this.startDate = startDate != null ? startDate : LocalDate.now();
        this.endDate = endDate;
        this.scheduleDetails = scheduleDetails;
        this.trainerAdvice = trainerAdvice;
        this.status = "ACTIVE";
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Member getMember() {
        return member;
    }

    public void setMember(Member member) {
        this.member = member;
    }

    public Trainer getTrainer() {
        return trainer;
    }

    public void setTrainer(Trainer trainer) {
        this.trainer = trainer;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getFitnessGoal() {
        return fitnessGoal;
    }

    public void setFitnessGoal(String fitnessGoal) {
        this.fitnessGoal = fitnessGoal;
    }

    public String getTargetBodyType() {
        return targetBodyType;
    }

    public void setTargetBodyType(String targetBodyType) {
        this.targetBodyType = targetBodyType;
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public Integer getDurationWeeks() {
        return durationWeeks;
    }

    public void setDurationWeeks(Integer durationWeeks) {
        this.durationWeeks = durationWeeks;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getScheduleDetails() {
        return scheduleDetails;
    }

    public void setScheduleDetails(String scheduleDetails) {
        this.scheduleDetails = scheduleDetails;
    }

    public String getTrainerAdvice() {
        return trainerAdvice;
    }

    public void setTrainerAdvice(String trainerAdvice) {
        this.trainerAdvice = trainerAdvice;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
