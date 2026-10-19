package com.gym.management.repository;

import com.gym.management.model.ClassBooking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ClassBookingRepository extends JpaRepository<ClassBooking, Long> {

    List<ClassBooking> findByGymClassId(Long gymClassId);

    List<ClassBooking> findByMemberIdOrderByBookingDateDesc(Long memberId);

    List<ClassBooking> findByMemberIdAndStatus(Long memberId, String status);

    Optional<ClassBooking> findByGymClassIdAndMemberIdAndBookingDate(Long gymClassId, Long memberId, LocalDate bookingDate);

    List<ClassBooking> findByGymClassIdAndBookingDateAndStatus(Long gymClassId, LocalDate bookingDate, String status);

    long countByGymClassIdAndBookingDateAndStatus(Long gymClassId, LocalDate bookingDate, String status);
}
