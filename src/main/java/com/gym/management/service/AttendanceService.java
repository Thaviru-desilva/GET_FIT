package com.gym.management.service;

import com.gym.management.model.Attendance;
import com.gym.management.model.Member;
import com.gym.management.model.MemberStatus;
import com.gym.management.model.SubscriptionStatus;
import com.gym.management.repository.AttendanceRepository;
import com.gym.management.repository.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final MemberRepository memberRepository;
    private final com.gym.management.repository.WorkoutSlotBookingRepository slotBookingRepository;

    public AttendanceService(AttendanceRepository attendanceRepository,
                             MemberRepository memberRepository,
                             com.gym.management.repository.WorkoutSlotBookingRepository slotBookingRepository) {
        this.attendanceRepository = attendanceRepository;
        this.memberRepository = memberRepository;
        this.slotBookingRepository = slotBookingRepository;
    }

    public List<Attendance> findAll() {
        return attendanceRepository.findAll();
    }

    public List<Attendance> getTodayAttendance() {
        return attendanceRepository.findByDateOrderByCheckInTimeDesc(LocalDate.now());
    }

    public List<Attendance> getRecentAttendance() {
        return attendanceRepository.findTop10ByOrderByDateDescCheckInTimeDesc();
    }

    public List<Attendance> getMemberAttendance(Long memberId) {
        return attendanceRepository.findByMemberIdOrderByDateDesc(memberId);
    }

    public Attendance checkIn(Long memberId, String notes) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid member ID: " + memberId));

        if (member.getStatus() != MemberStatus.ACTIVE) {
            throw new IllegalStateException("Check-in rejected: Member status is " + member.getStatus() + ". Membership must be ACTIVE to enter the facility.");
        }

        boolean hasValidSub = member.getSubscriptions().isEmpty() || member.getSubscriptions().stream()
                .anyMatch(s -> s.getStatus() == SubscriptionStatus.ACTIVE && !s.isExpired());
        if (!hasValidSub) {
            throw new IllegalStateException("Check-in rejected: All subscriptions for member have expired. Please renew membership at the desk.");
        }

        List<Attendance> openCheckIns = attendanceRepository.findOpenCheckIns(memberId, LocalDate.now());
        if (!openCheckIns.isEmpty()) {
            throw new IllegalStateException("Member " + member.getFullName() + " is already checked in today without checking out. Please record check-out first.");
        }

        Attendance attendance = new Attendance();
        attendance.setMember(member);
        attendance.setDate(LocalDate.now());
        attendance.setCheckInTime(LocalTime.now().withNano(0));
        attendance.setNotes(notes);

        Attendance savedAttendance = attendanceRepository.save(attendance);

        // Auto-confirm athlete's workout slot upon gym floor check-in
        List<com.gym.management.model.WorkoutSlotBooking> todaySlots = slotBookingRepository.findActiveBookingsForMemberOnDate(memberId, LocalDate.now());
        for (com.gym.management.model.WorkoutSlotBooking b : todaySlots) {
            b.setStatus("CHECKED_IN");
            slotBookingRepository.save(b);
        }

        return savedAttendance;
    }

    public Optional<Attendance> checkOut(Long attendanceId) {
        Optional<Attendance> attendanceOpt = attendanceRepository.findById(attendanceId);
        attendanceOpt.ifPresent(attendance -> {
            attendance.setCheckOutTime(LocalTime.now().withNano(0));
            attendanceRepository.save(attendance);
        });
        return attendanceOpt;
    }

    public long countTodayCheckIns() {
        return attendanceRepository.countByDate(LocalDate.now());
    }

    public void deleteById(Long id) {
        attendanceRepository.deleteById(id);
    }
}
