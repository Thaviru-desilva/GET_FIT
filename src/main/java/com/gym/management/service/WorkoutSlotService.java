package com.gym.management.service;

import com.gym.management.dto.WorkoutSlotAvailabilityDto;
import com.gym.management.model.Member;
import com.gym.management.model.MemberStatus;
import com.gym.management.model.Subscription;
import com.gym.management.model.WorkoutFocus;
import com.gym.management.model.WorkoutSlotBooking;
import com.gym.management.repository.MemberRepository;
import com.gym.management.repository.WorkoutSlotBookingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@Transactional
public class WorkoutSlotService {

    public static final List<LocalTime> STANDARD_START_TIMES = List.of(
            LocalTime.of(6, 0),
            LocalTime.of(7, 0),
            LocalTime.of(8, 0),
            LocalTime.of(9, 0),
            LocalTime.of(10, 0),
            LocalTime.of(11, 0),
            LocalTime.of(16, 0),
            LocalTime.of(17, 0),
            LocalTime.of(18, 0),
            LocalTime.of(19, 0),
            LocalTime.of(20, 0)
    );

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("hh:mm a");

    private final WorkoutSlotBookingRepository slotBookingRepository;
    private final MemberRepository memberRepository;
    private final SubscriptionService subscriptionService;

    public WorkoutSlotService(WorkoutSlotBookingRepository slotBookingRepository,
                              MemberRepository memberRepository,
                              SubscriptionService subscriptionService) {
        this.slotBookingRepository = slotBookingRepository;
        this.memberRepository = memberRepository;
        this.subscriptionService = subscriptionService;
    }

    @Transactional(readOnly = true)
    public List<WorkoutSlotAvailabilityDto> getSlotAvailability(LocalDate date, WorkoutFocus focus, Long currentMemberId) {
        if (date == null) {
            date = LocalDate.now();
        }
        if (focus == null) {
            focus = WorkoutFocus.LEGS;
        }

        LocalDate now = LocalDate.now();
        LocalTime currentTime = LocalTime.now();

        List<WorkoutSlotAvailabilityDto> dtos = new ArrayList<>();

        for (LocalTime startTime : STANDARD_START_TIMES) {
            LocalTime endTime = startTime.plusHours(1);
            long bookedCount = slotBookingRepository.countActiveBookings(
                    date, startTime, focus
            );

            int capacity = focus.getCapacity();
            int remaining = Math.max(0, capacity - (int) bookedCount);
            boolean isFull = bookedCount >= capacity;

            boolean isUserBooked = false;
            if (currentMemberId != null) {
                isUserBooked = slotBookingRepository.findActiveBookingsForMemberOnDate(currentMemberId, date).stream()
                        .anyMatch(b -> b.getStartTime().equals(startTime));
            }

            boolean isPast = date.isBefore(now) || (date.isEqual(now) && startTime.isBefore(currentTime));

            String label = startTime.format(TIME_FORMATTER) + " - " + endTime.format(TIME_FORMATTER);

            dtos.add(new WorkoutSlotAvailabilityDto(
                    startTime,
                    endTime,
                    label,
                    focus,
                    capacity,
                    (int) bookedCount,
                    remaining,
                    isFull,
                    isUserBooked,
                    isPast
            ));
        }

        return dtos;
    }

    public WorkoutSlotBooking bookSlot(Long memberId, LocalDate date, LocalTime startTime, WorkoutFocus focus, String notes) {
        if (memberId == null) {
            throw new IllegalArgumentException("Member ID is required to book a workout slot.");
        }
        if (date == null) {
            throw new IllegalArgumentException("Booking date is required.");
        }
        if (startTime == null) {
            throw new IllegalArgumentException("Start time is required.");
        }
        if (focus == null) {
            throw new IllegalArgumentException("Workout focus category is required.");
        }

        LocalDate now = LocalDate.now();
        if (date.isBefore(now)) {
            throw new IllegalArgumentException("Cannot book workout slots for past dates.");
        }
        if (date.isAfter(now.plusDays(14))) {
            throw new IllegalArgumentException("Workout slots can only be booked up to 14 days in advance.");
        }
        if (date.isEqual(now) && startTime.isBefore(LocalTime.now())) {
            throw new IllegalArgumentException("Cannot book a time slot that has already passed today.");
        }

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Member not found with ID: " + memberId));

        if (member.getStatus() != MemberStatus.ACTIVE) {
            throw new IllegalStateException("Your membership account is inactive. Please visit the front desk to activate your account.");
        }

        List<Subscription> subscriptions = subscriptionService.findByMemberId(memberId);
        boolean hasActiveSubscription = subscriptions.stream().anyMatch(s -> !s.isExpired());
        if (!hasActiveSubscription) {
            throw new IllegalStateException("Active membership subscription required to reserve limited equipment workout slots.");
        }

        // Prevent double booking at the exact same time
        List<WorkoutSlotBooking> memberBookings = slotBookingRepository.findActiveBookingsForMemberOnDate(memberId, date);
        for (WorkoutSlotBooking b : memberBookings) {
            if (b.getStartTime().equals(startTime)) {
                throw new IllegalStateException("You already have an active workout slot booked for " + startTime + " on " + date + " (" + b.getWorkoutFocus().getDisplayName() + ").");
            }
        }

        // Enforce equipment capacity limit
        long currentBooked = slotBookingRepository.countActiveBookings(date, startTime, focus);
        if (currentBooked >= focus.getCapacity()) {
            throw new IllegalStateException("Equipment capacity reached (" + focus.getCapacity() + "/" + focus.getCapacity() + ") for "
                    + focus.getDisplayName() + " at " + startTime + ". Please choose another time slot to prevent equipment wait times.");
        }

        LocalTime endTime = startTime.plusHours(1);
        WorkoutSlotBooking booking = new WorkoutSlotBooking(member, date, startTime, endTime, focus, notes);
        return slotBookingRepository.save(booking);
    }

    public void cancelBooking(Long bookingId, Long memberId, boolean isPrivileged) {
        WorkoutSlotBooking booking = slotBookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Workout slot booking not found with ID: " + bookingId));

        if (!isPrivileged && (memberId == null || !booking.getMember().getId().equals(memberId))) {
            throw new SecurityException("Unauthorized: You can only cancel your own workout slot bookings.");
        }

        booking.setStatus("CANCELLED");
        slotBookingRepository.save(booking);
    }

    public int releaseNoShowSlots(LocalDate date, LocalTime currentTime) {
        if (date == null) date = LocalDate.now();
        if (currentTime == null) currentTime = LocalTime.now();

        // Grace period: 15 minutes past start time
        LocalTime threshold = currentTime.minusMinutes(15);
        List<WorkoutSlotBooking> candidates = slotBookingRepository.findPotentialNoShows(date, threshold);

        int released = 0;
        for (WorkoutSlotBooking b : candidates) {
            b.setStatus("NO_SHOW_RELEASED");
            b.setNotes((b.getNotes() != null ? b.getNotes() + " | " : "") + "Auto-released: Athlete did not check in within 15 minutes of slot start.");
            slotBookingRepository.save(b);
            released++;
        }
        return released;
    }

    public void markSlotCheckedInIfActive(Long memberId, LocalDate date, LocalTime currentTime) {
        if (memberId == null || date == null) return;
        if (currentTime == null) currentTime = LocalTime.now();

        List<WorkoutSlotBooking> bookings = slotBookingRepository.findActiveBookingsForMemberOnDate(memberId, date);
        for (WorkoutSlotBooking b : bookings) {
            LocalTime windowStart = b.getStartTime().minusMinutes(30);
            LocalTime windowEnd = b.getEndTime();
            if (!currentTime.isBefore(windowStart) && !currentTime.isAfter(windowEnd)) {
                b.setStatus("CHECKED_IN");
                slotBookingRepository.save(b);
            }
        }
    }

    @Transactional(readOnly = true)
    public Optional<LocalTime> findNextAvailableSlot(LocalDate date, WorkoutFocus focus) {
        if (date == null) date = LocalDate.now();
        if (focus == null) focus = WorkoutFocus.LEGS;

        LocalDate now = LocalDate.now();
        LocalTime currentTime = LocalTime.now();

        for (LocalTime startTime : STANDARD_START_TIMES) {
            boolean isPast = date.isBefore(now) || (date.isEqual(now) && startTime.isBefore(currentTime));
            if (!isPast) {
                long bookedCount = slotBookingRepository.countActiveBookings(date, startTime, focus);
                if (bookedCount < focus.getCapacity()) {
                    return Optional.of(startTime);
                }
            }
        }
        return Optional.empty();
    }

    @Transactional(readOnly = true)
    public List<WorkoutSlotBooking> getUpcomingMemberBookings(Long memberId) {
        return slotBookingRepository.findByMemberIdAndBookingDateGreaterThanEqualAndStatusOrderByBookingDateAscStartTimeAsc(
                memberId, LocalDate.now(), "CONFIRMED"
        );
    }

    @Transactional(readOnly = true)
    public List<WorkoutSlotBooking> getAllMemberBookings(Long memberId) {
        return slotBookingRepository.findByMemberIdOrderByBookingDateDescStartTimeDesc(memberId);
    }

    @Transactional(readOnly = true)
    public boolean checkSlotCapacity(LocalDate date, LocalTime startTime, WorkoutFocus focus) {
        if (date == null || startTime == null || focus == null) return false;
        long currentBooked = slotBookingRepository.countActiveBookings(
                date, startTime, focus
        );
        return currentBooked < focus.getCapacity();
    }

    @Transactional(readOnly = true)
    public Map<WorkoutFocus, Integer> getRemainingCapacityMap(LocalDate date, LocalTime startTime) {
        Map<WorkoutFocus, Integer> map = new EnumMap<>(WorkoutFocus.class);
        for (WorkoutFocus focus : WorkoutFocus.values()) {
            long currentBooked = slotBookingRepository.countActiveBookings(
                    date, startTime, focus
            );
            int remaining = Math.max(0, focus.getCapacity() - (int) currentBooked);
            map.put(focus, remaining);
        }
        return map;
    }

    public Optional<WorkoutSlotBooking> findById(Long id) {
        return slotBookingRepository.findById(id);
    }
}
