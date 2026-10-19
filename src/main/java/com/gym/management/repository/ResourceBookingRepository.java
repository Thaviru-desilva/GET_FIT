package com.gym.management.repository;

import com.gym.management.model.ResourceBooking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ResourceBookingRepository extends JpaRepository<ResourceBooking, Long> {
    List<ResourceBooking> findByResourceIdAndBookingDateAndStatus(Long resourceId, LocalDate bookingDate, String status);
    List<ResourceBooking> findByMemberIdOrderByBookingDateDescStartTimeDesc(Long memberId);
    List<ResourceBooking> findByTrainerIdOrderByBookingDateDescStartTimeDesc(Long trainerId);
    List<ResourceBooking> findByBookingDateAndStatus(LocalDate bookingDate, String status);
    List<ResourceBooking> findByBookingDateOrderByStartTimeAsc(LocalDate bookingDate);
}
