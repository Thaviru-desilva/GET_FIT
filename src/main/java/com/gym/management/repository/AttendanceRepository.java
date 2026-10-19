package com.gym.management.repository;

import com.gym.management.model.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    List<Attendance> findByDateOrderByCheckInTimeDesc(LocalDate date);

    long countByDate(LocalDate date);

    List<Attendance> findByMemberIdOrderByDateDesc(Long memberId);

    List<Attendance> findTop10ByOrderByDateDescCheckInTimeDesc();

    @Query("SELECT a FROM Attendance a WHERE a.member.id = :memberId AND a.date = :date AND a.checkOutTime IS NULL")
    List<Attendance> findOpenCheckIns(@Param("memberId") Long memberId, @Param("date") LocalDate date);
}
