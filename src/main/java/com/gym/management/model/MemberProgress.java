package com.gym.management.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Entity
@Table(name = "member_progress")
public class MemberProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @NotNull
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Column(nullable = false)
    private LocalDate recordDate = LocalDate.now();

    @NotNull(message = "Weight is required")
    @DecimalMin(value = "20.0", message = "Weight must be at least 20 kg")
    @DecimalMax(value = "350.0", message = "Weight cannot exceed 350 kg")
    @Column(nullable = false)
    private Double weightKg;

    @NotNull(message = "Height is required")
    @DecimalMin(value = "80.0", message = "Height must be at least 80 cm")
    @DecimalMax(value = "250.0", message = "Height cannot exceed 250 cm")
    @Column(nullable = false)
    private Double heightCm;

    private String bodyType; // e.g. Ectomorph, Mesomorph, Endomorph, Athletic

    @DecimalMin(value = "0.0", message = "Workout hours cannot be negative")
    @DecimalMax(value = "100.0", message = "Workout hours cannot exceed 100 hrs/week")
    private Double workoutHoursPerWeek;

    @DecimalMin(value = "3.0", message = "Body fat percentage must be at least 3%")
    @DecimalMax(value = "60.0", message = "Body fat percentage cannot exceed 60%")
    private Double bodyFatPercentage;

    private String targetGoal; // e.g. Muscle Gain, Fat Loss, Endurance, General Fitness

    @Column(length = 1000)
    private String notes;

    private String loggedBy = "MEMBER"; // MEMBER or TRAINER

    public MemberProgress() {
    }

    public MemberProgress(Member member, LocalDate recordDate, Double weightKg, Double heightCm, String bodyType, Double workoutHoursPerWeek, String targetGoal, String notes, String loggedBy) {
        this.member = member;
        this.recordDate = recordDate != null ? recordDate : LocalDate.now();
        this.weightKg = weightKg;
        this.heightCm = heightCm;
        this.bodyType = bodyType;
        this.workoutHoursPerWeek = workoutHoursPerWeek;
        this.targetGoal = targetGoal;
        this.notes = notes;
        this.loggedBy = loggedBy;
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

    public LocalDate getRecordDate() {
        return recordDate;
    }

    public void setRecordDate(LocalDate recordDate) {
        this.recordDate = recordDate;
    }

    public Double getWeightKg() {
        return weightKg;
    }

    public void setWeightKg(Double weightKg) {
        this.weightKg = weightKg;
    }

    public Double getHeightCm() {
        return heightCm;
    }

    public void setHeightCm(Double heightCm) {
        this.heightCm = heightCm;
    }

    public String getBodyType() {
        return bodyType;
    }

    public void setBodyType(String bodyType) {
        this.bodyType = bodyType;
    }

    public Double getWorkoutHoursPerWeek() {
        return workoutHoursPerWeek;
    }

    public void setWorkoutHoursPerWeek(Double workoutHoursPerWeek) {
        this.workoutHoursPerWeek = workoutHoursPerWeek;
    }

    public Double getBodyFatPercentage() {
        return bodyFatPercentage;
    }

    public void setBodyFatPercentage(Double bodyFatPercentage) {
        this.bodyFatPercentage = bodyFatPercentage;
    }

    public String getTargetGoal() {
        return targetGoal;
    }

    public void setTargetGoal(String targetGoal) {
        this.targetGoal = targetGoal;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getLoggedBy() {
        return loggedBy;
    }

    public void setLoggedBy(String loggedBy) {
        this.loggedBy = loggedBy;
    }

    // Helper: calculate Body Mass Index (BMI)
    public Double getBmi() {
        if (weightKg == null || heightCm == null || heightCm <= 0) {
            return 0.0;
        }
        double heightM = heightCm / 100.0;
        double bmi = weightKg / (heightM * heightM);
        return Math.round(bmi * 10.0) / 10.0;
    }

    // Helper: BMI classification category
    public String getBmiCategory() {
        double bmi = getBmi();
        if (bmi <= 0) return "N/A";
        if (bmi < 18.5) return "Underweight";
        if (bmi < 25.0) return "Normal Weight";
        if (bmi < 30.0) return "Overweight";
        return "Obese";
    }
}
