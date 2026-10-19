package com.gym.management.service;

import com.gym.management.model.Attendance;
import com.gym.management.model.Member;
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

    public AttendanceService(AttendanceRepository attendanceRepository, MemberRepository memberRepository) {
        this.attendanceRepository = attendanceRepository;
        this.memberRepository = memberRepository;
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

        Attendance attendance = new Attendance();
        attendance.setMember(member);
        attendance.setDate(LocalDate.now());
        attendance.setCheckInTime(LocalTime.now().withNano(0));
        attendance.setNotes(notes);

        return attendanceRepository.save(attendance);
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
