package com.gym.management.service;

import com.gym.management.model.ClassBooking;
import com.gym.management.model.GymClass;
import com.gym.management.model.Member;
import com.gym.management.repository.ClassBookingRepository;
import com.gym.management.repository.GymClassRepository;
import com.gym.management.repository.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class GymClassService {

    private final GymClassRepository gymClassRepository;
    private final ClassBookingRepository bookingRepository;
    private final MemberRepository memberRepository;

    public GymClassService(GymClassRepository gymClassRepository,
                           ClassBookingRepository bookingRepository,
                           MemberRepository memberRepository) {
        this.gymClassRepository = gymClassRepository;
        this.bookingRepository = bookingRepository;
        this.memberRepository = memberRepository;
    }

    public List<GymClass> findAll() {
        return gymClassRepository.findAll();
    }

    public Optional<GymClass> findById(Long id) {
        return gymClassRepository.findById(id);
    }

    public List<GymClass> findByTrainerId(Long trainerId) {
        return gymClassRepository.findByTrainerId(trainerId);
    }

    public GymClass save(GymClass gymClass) {
        return gymClassRepository.save(gymClass);
    }

    public void deleteById(Long id) {
        gymClassRepository.deleteById(id);
    }

    public ClassBooking bookClass(Long gymClassId, Long memberId, LocalDate date) {
        if (date == null) {
            date = LocalDate.now();
        }

        GymClass gymClass = gymClassRepository.findById(gymClassId)
                .orElseThrow(() -> new IllegalArgumentException("Class not found: " + gymClassId));
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Member not found: " + memberId));

        // Check if already booked
        Optional<ClassBooking> existing = bookingRepository.findByGymClassIdAndMemberIdAndBookingDate(gymClassId, memberId, date);
        if (existing.isPresent()) {
            ClassBooking booking = existing.get();
            if ("CONFIRMED".equalsIgnoreCase(booking.getStatus())) {
                throw new IllegalStateException("You are already enrolled in this class session.");
            } else {
                // Reactivate cancelled booking
                booking.setStatus("CONFIRMED");
                return bookingRepository.save(booking);
            }
        }

        // Check capacity
        long currentCount = bookingRepository.countByGymClassIdAndBookingDateAndStatus(gymClassId, date, "CONFIRMED");
        if (currentCount >= gymClass.getCapacity()) {
            throw new IllegalStateException("Class is fully booked! No remaining spots available.");
        }

        ClassBooking newBooking = new ClassBooking(gymClass, member, date);
        return bookingRepository.save(newBooking);
    }

    public void cancelBooking(Long bookingId) {
        bookingRepository.findById(bookingId).ifPresent(booking -> {
            booking.setStatus("CANCELLED");
            bookingRepository.save(booking);
        });
    }

    public List<ClassBooking> getMemberBookings(Long memberId) {
        return bookingRepository.findByMemberIdOrderByBookingDateDesc(memberId);
    }

    public List<ClassBooking> getClassBookings(Long gymClassId) {
        return bookingRepository.findByGymClassId(gymClassId);
    }

    public boolean hasMemberBooked(Long gymClassId, Long memberId, LocalDate date) {
        if (date == null) date = LocalDate.now();
        return bookingRepository.findByGymClassIdAndMemberIdAndBookingDate(gymClassId, memberId, date)
                .map(b -> "CONFIRMED".equalsIgnoreCase(b.getStatus()))
                .orElse(false);
    }

    public long count() {
        return gymClassRepository.count();
    }
}
