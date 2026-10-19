package com.gym.management.model;

import jakarta.persistence.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "class_bookings", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"gym_class_id", "member_id", "booking_date"})
})
public class ClassBooking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "gym_class_id", nullable = false)
    private GymClass gymClass;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Column(name = "booking_date", nullable = false)
    private LocalDate bookingDate = LocalDate.now();

    @Column(nullable = false)
    private LocalDateTime bookedAt = LocalDateTime.now();

    @Column(nullable = false)
    private String status = "CONFIRMED"; // CONFIRMED, CANCELLED

    public ClassBooking() {
    }

    public ClassBooking(GymClass gymClass, Member member, LocalDate bookingDate) {
        this.gymClass = gymClass;
        this.member = member;
        this.bookingDate = bookingDate != null ? bookingDate : LocalDate.now();
        this.bookedAt = LocalDateTime.now();
        this.status = "CONFIRMED";
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public GymClass getGymClass() {
        return gymClass;
    }

    public void setGymClass(GymClass gymClass) {
        this.gymClass = gymClass;
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

    public LocalDateTime getBookedAt() {
        return bookedAt;
    }

    public void setBookedAt(LocalDateTime bookedAt) {
        this.bookedAt = bookedAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
