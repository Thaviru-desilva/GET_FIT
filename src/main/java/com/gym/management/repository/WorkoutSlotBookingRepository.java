package com.gym.management.repository;

import com.gym.management.model.WorkoutFocus;
import com.gym.management.model.WorkoutSlotBooking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface WorkoutSlotBookingRepository extends JpaRepository<WorkoutSlotBooking, Long> {

    long countByBookingDateAndStartTimeAndWorkoutFocusAndStatus(
            LocalDate bookingDate,
            LocalTime startTime,
            WorkoutFocus workoutFocus,
            String status
    );

    List<WorkoutSlotBooking> findByBookingDateAndStartTimeAndStatus(
            LocalDate bookingDate,
            LocalTime startTime,
            String status
    );

    List<WorkoutSlotBooking> findByMemberIdAndBookingDateGreaterThanEqualAndStatusOrderByBookingDateAscStartTimeAsc(
            Long memberId,
            LocalDate bookingDate,
            String status
    );

    List<WorkoutSlotBooking> findByMemberIdOrderByBookingDateDescStartTimeDesc(Long memberId);

    Optional<WorkoutSlotBooking> findByMemberIdAndBookingDateAndStartTimeAndStatus(
            Long memberId,
            LocalDate bookingDate,
            LocalTime startTime,
            String status
    );

    @Query("SELECT COUNT(b) FROM WorkoutSlotBooking b WHERE b.bookingDate = :date AND b.startTime = :time AND b.workoutFocus = :focus AND b.status IN ('CONFIRMED', 'CHECKED_IN')")
    long countActiveBookings(
            @Param("date") LocalDate date,
            @Param("time") LocalTime time,
            @Param("focus") WorkoutFocus focus
    );

    @Query("SELECT b FROM WorkoutSlotBooking b WHERE b.bookingDate = :date AND b.status = 'CONFIRMED' AND b.startTime <= :thresholdTime")
    List<WorkoutSlotBooking> findPotentialNoShows(
            @Param("date") LocalDate date,
            @Param("thresholdTime") LocalTime thresholdTime
    );

    @Query("SELECT b FROM WorkoutSlotBooking b WHERE b.member.id = :memberId AND b.bookingDate = :date AND b.status IN ('CONFIRMED', 'CHECKED_IN')")
    List<WorkoutSlotBooking> findActiveBookingsForMemberOnDate(
            @Param("memberId") Long memberId,
            @Param("date") LocalDate date
    );

    @Query("SELECT COUNT(b) FROM WorkoutSlotBooking b WHERE b.bookingDate = :date AND b.status = 'CONFIRMED'")
    long countTotalConfirmedBookingsOnDate(@Param("date") LocalDate date);
}
