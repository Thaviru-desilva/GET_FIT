package com.gym.management.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "workout_slot_bookings",
       indexes = {
           @Index(name = "idx_slot_date_time_focus", columnList = "bookingDate, startTime, workoutFocus, status")
       })
public class WorkoutSlotBooking {

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
    private LocalDate bookingDate = LocalDate.now();

    @NotNull
    @Column(nullable = false)
    private LocalTime startTime;

    @NotNull
    @Column(nullable = false)
    private LocalTime endTime;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WorkoutFocus workoutFocus;

    @Column(nullable = false)
    private String status = "CONFIRMED"; // CONFIRMED, COMPLETED, CANCELLED

    private String notes;

    private LocalDateTime createdAt = LocalDateTime.now();

    public WorkoutSlotBooking() {
    }

    public WorkoutSlotBooking(Member member, LocalDate bookingDate, LocalTime startTime, LocalTime endTime, WorkoutFocus workoutFocus, String notes) {
        this.member = member;
        this.bookingDate = bookingDate != null ? bookingDate : LocalDate.now();
        this.startTime = startTime;
        this.endTime = endTime != null ? endTime : startTime.plusHours(1);
        this.workoutFocus = workoutFocus;
        this.notes = notes;
        this.status = "CONFIRMED";
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

    public LocalDate getBookingDate() {
        return bookingDate;
    }

    public void setBookingDate(LocalDate bookingDate) {
        this.bookingDate = bookingDate;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public WorkoutFocus getWorkoutFocus() {
        return workoutFocus;
    }

    public void setWorkoutFocus(WorkoutFocus workoutFocus) {
        this.workoutFocus = workoutFocus;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isConfirmed() {
        return "CONFIRMED".equalsIgnoreCase(this.status);
    }
}
