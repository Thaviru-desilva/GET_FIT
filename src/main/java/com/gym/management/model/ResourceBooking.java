package com.gym.management.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "resource_bookings")
public class ResourceBooking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "resource_id", nullable = false)
    private GymResource resource;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "trainer_id")
    private Trainer trainer;

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

    @Column(nullable = false)
    private String status = "CONFIRMED"; // CONFIRMED, COMPLETED, CANCELLED

    private String purpose; // e.g. "Squats & Deadlifts workout", "Client 1-on-1 PT"

    private LocalDateTime createdAt = LocalDateTime.now();

    public ResourceBooking() {
    }

    public ResourceBooking(GymResource resource, Member member, Trainer trainer, LocalDate bookingDate, LocalTime startTime, LocalTime endTime, String purpose) {
        this.resource = resource;
        this.member = member;
        this.trainer = trainer;
        this.bookingDate = bookingDate != null ? bookingDate : LocalDate.now();
        this.startTime = startTime;
        this.endTime = endTime;
        this.purpose = purpose;
        this.status = "CONFIRMED";
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public GymResource getResource() {
        return resource;
    }

    public void setResource(GymResource resource) {
        this.resource = resource;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getBookerName() {
        if (member != null) return member.getFullName() + " (Athlete)";
        if (trainer != null) return trainer.getFullName() + " (Coach)";
        return "Gym Operations";
    }
}
