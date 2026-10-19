package com.gym.management.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "diet_plans")
public class DietPlan {

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

    private String dietaryGoal; // e.g. Clean Bulking, Fat Loss Cutting, Maintenance, Ketogenic

    private Integer targetCalories; // e.g. 2500 kcal

    private Integer proteinGrams; // e.g. 160 g

    private Integer carbsGrams; // e.g. 260 g

    private Integer fatsGrams; // e.g. 65 g

    private Double waterLiters = 3.0; // e.g. 3.5 L / day

    private String dietaryRestrictions; // e.g. No Dairy, Nut Allergy, Vegetarian, Halal

    @Lob
    @Column(columnDefinition = "TEXT")
    private String mealBreakdown; // Breakfast, Mid-Morning, Lunch, Pre-Workout, Dinner, Snacks

    @Lob
    @Column(columnDefinition = "TEXT")
    private String trainerNotes; // Supplement advice, meal timing, hydration

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate = LocalDate.now();

    @Column(nullable = false)
    private String status = "ACTIVE"; // ACTIVE, COMPLETED, ARCHIVED

    private LocalDateTime createdAt = LocalDateTime.now();

    public DietPlan() {
    }

    public DietPlan(Member member, Trainer trainer, String title, String dietaryGoal, Integer targetCalories, Integer proteinGrams, Integer carbsGrams, Integer fatsGrams, Double waterLiters, String dietaryRestrictions, String mealBreakdown, String trainerNotes) {
        this.member = member;
        this.trainer = trainer;
        this.title = title;
        this.dietaryGoal = dietaryGoal;
        this.targetCalories = targetCalories;
        this.proteinGrams = proteinGrams;
        this.carbsGrams = carbsGrams;
        this.fatsGrams = fatsGrams;
        this.waterLiters = waterLiters != null ? waterLiters : 3.0;
        this.dietaryRestrictions = dietaryRestrictions;
        this.mealBreakdown = mealBreakdown;
        this.trainerNotes = trainerNotes;
        this.status = "ACTIVE";
        this.startDate = LocalDate.now();
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

    public String getDietaryGoal() {
        return dietaryGoal;
    }

    public void setDietaryGoal(String dietaryGoal) {
        this.dietaryGoal = dietaryGoal;
    }

    public Integer getTargetCalories() {
        return targetCalories;
    }

    public void setTargetCalories(Integer targetCalories) {
        this.targetCalories = targetCalories;
    }

    public Integer getProteinGrams() {
        return proteinGrams;
    }

    public void setProteinGrams(Integer proteinGrams) {
        this.proteinGrams = proteinGrams;
    }

    public Integer getCarbsGrams() {
        return carbsGrams;
    }

    public void setCarbsGrams(Integer carbsGrams) {
        this.carbsGrams = carbsGrams;
    }

    public Integer getFatsGrams() {
        return fatsGrams;
    }

    public void setFatsGrams(Integer fatsGrams) {
        this.fatsGrams = fatsGrams;
    }

    public Double getWaterLiters() {
        return waterLiters;
    }

    public void setWaterLiters(Double waterLiters) {
        this.waterLiters = waterLiters;
    }

    public String getDietaryRestrictions() {
        return dietaryRestrictions;
    }

    public void setDietaryRestrictions(String dietaryRestrictions) {
        this.dietaryRestrictions = dietaryRestrictions;
    }

    public String getMealBreakdown() {
        return mealBreakdown;
    }

    public void setMealBreakdown(String mealBreakdown) {
        this.mealBreakdown = mealBreakdown;
    }

    public String getTrainerNotes() {
        return trainerNotes;
    }

    public void setTrainerNotes(String trainerNotes) {
        this.trainerNotes = trainerNotes;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
