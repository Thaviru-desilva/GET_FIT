package com.gym.management;

import com.gym.management.dto.WorkoutSlotAvailabilityDto;
import com.gym.management.model.*;
import com.gym.management.repository.WorkoutSlotBookingRepository;
import com.gym.management.service.MemberService;
import com.gym.management.service.MembershipPlanService;
import com.gym.management.service.SubscriptionService;
import com.gym.management.service.WorkoutSlotService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class WorkoutSlotServiceTests {

    @Autowired
    private WorkoutSlotService slotService;

    @Autowired
    private WorkoutSlotBookingRepository slotBookingRepository;

    @Autowired
    private MemberService memberService;

    @Autowired
    private SubscriptionService subscriptionService;

    @Autowired
    private com.gym.management.repository.SubscriptionRepository subscriptionRepository;

    @Autowired
    private MembershipPlanService planService;

    private Member activeMember;
    private Member inactiveMember;
    private Member nonSubscribedMember;

    @BeforeEach
    void setUp() {
        // Find existing pre-seeded member with active subscription
        List<Member> members = memberService.findAll();
        activeMember = members.stream()
                .filter(m -> m.getStatus() == MemberStatus.ACTIVE && subscriptionService.findByMemberId(m.getId()).stream().anyMatch(Subscription::isActive))
                .findFirst()
                .orElseGet(() -> {
                    Member m = new Member("Test", "Athlete", "test.athlete." + System.currentTimeMillis() + "@gym.com", "+1-555-0001", "Male", LocalDate.of(1995, 1, 1), null);
                    m = memberService.save(m);
                    MembershipPlan plan = planService.findAll().get(0);
                    Subscription sub = new Subscription(m, plan, LocalDate.now().minusDays(1), LocalDate.now().plusMonths(1), plan.getPrice(), PaymentStatus.PAID);
                    subscriptionRepository.save(sub);
                    return m;
                });

        // Create inactive member
        inactiveMember = new Member("Inactive", "User", "inactive." + System.currentTimeMillis() + "@gym.com", "+1-555-0002", "Male", LocalDate.of(1992, 2, 2), null);
        inactiveMember.setStatus(MemberStatus.INACTIVE);
        inactiveMember = memberService.save(inactiveMember);

        // Create active member without subscription
        nonSubscribedMember = new Member("NoSub", "User", "nosub." + System.currentTimeMillis() + "@gym.com", "+1-555-0003", "Female", LocalDate.of(1993, 3, 3), null);
        nonSubscribedMember.setStatus(MemberStatus.ACTIVE);
        nonSubscribedMember = memberService.save(nonSubscribedMember);
    }

    @Test
    @DisplayName("FR-1 & FR-2: Slot availability returns 11 standard 1-hour slots with capacity info")
    void testSlotAvailabilityListing() {
        LocalDate targetDate = LocalDate.now().plusDays(2);
        List<WorkoutSlotAvailabilityDto> slots = slotService.getSlotAvailability(targetDate, WorkoutFocus.LEGS, activeMember.getId());

        assertNotNull(slots);
        assertEquals(11, slots.size(), "Should provide 11 standard operational 1-hour slots");

        WorkoutSlotAvailabilityDto firstSlot = slots.get(0);
        assertEquals(LocalTime.of(6, 0), firstSlot.getStartTime());
        assertEquals(LocalTime.of(7, 0), firstSlot.getEndTime());
        assertEquals(WorkoutFocus.LEGS, firstSlot.getWorkoutFocus());
        assertEquals(6, firstSlot.getCapacityLimit(), "LEGS focus capacity limit must be 6");
        assertEquals(0, firstSlot.getBookedCount());
        assertEquals(6, firstSlot.getRemainingSpots());
        assertFalse(firstSlot.isFull());
        assertFalse(firstSlot.isUserBooked());
    }

    @Test
    @DisplayName("FR-3: Member can book a valid slot with one click")
    void testSuccessfulSlotBooking() {
        LocalDate bookingDate = LocalDate.now().plusDays(3);
        LocalTime slotTime = LocalTime.of(18, 0);

        WorkoutSlotBooking booking = slotService.bookSlot(
                activeMember.getId(),
                bookingDate,
                slotTime,
                WorkoutFocus.LEGS,
                "Heavy Squats & Leg Press"
        );

        assertNotNull(booking);
        assertNotNull(booking.getId());
        assertEquals("CONFIRMED", booking.getStatus());
        assertEquals(bookingDate, booking.getBookingDate());
        assertEquals(slotTime, booking.getStartTime());
        assertEquals(slotTime.plusHours(1), booking.getEndTime());
        assertEquals(WorkoutFocus.LEGS, booking.getWorkoutFocus());

        // Verify slot availability reflects this booking
        List<WorkoutSlotAvailabilityDto> slots = slotService.getSlotAvailability(bookingDate, WorkoutFocus.LEGS, activeMember.getId());
        WorkoutSlotAvailabilityDto targetSlot = slots.stream()
                .filter(s -> s.getStartTime().equals(slotTime))
                .findFirst()
                .orElseThrow();

        assertEquals(1, targetSlot.getBookedCount());
        assertEquals(5, targetSlot.getRemainingSpots());
        assertTrue(targetSlot.isUserBooked(), "Slot should be marked as booked by current member");
        assertFalse(targetSlot.isFull());
    }

    @Test
    @DisplayName("FR-8 & FR-9: System enforces equipment capacity limit and blocks overload")
    void testCapacityEnforcementBlocksOvercrowding() {
        LocalDate bookingDate = LocalDate.now().plusDays(4);
        LocalTime slotTime = LocalTime.of(17, 0);
        WorkoutFocus focus = WorkoutFocus.LEGS; // Capacity limit = 6
        int capacity = focus.getCapacity();

        MembershipPlan plan = planService.findAll().get(0);
        List<Member> athletes = new ArrayList<>();

        // Create 6 distinct subscribed members to fill all 6 spots
        for (int i = 0; i < capacity; i++) {
            Member m = new Member("Athlete" + i, "Bench", "athlete" + i + "." + System.currentTimeMillis() + "@gym.com", "+1-555-100" + i, "Male", LocalDate.of(1990 + i, 1, 1), null);
            m = memberService.save(m);
            Subscription sub = new Subscription(m, plan, LocalDate.now().minusDays(2), LocalDate.now().plusMonths(1), plan.getPrice(), PaymentStatus.PAID);
            subscriptionRepository.save(sub);
            athletes.add(m);

            // Book each athlete
            WorkoutSlotBooking b = slotService.bookSlot(m.getId(), bookingDate, slotTime, focus, "Athlete " + i + " workout");
            assertNotNull(b);
        }

        // Verify slot is now completely FULL
        List<WorkoutSlotAvailabilityDto> slots = slotService.getSlotAvailability(bookingDate, focus, null);
        WorkoutSlotAvailabilityDto fullSlot = slots.stream()
                .filter(s -> s.getStartTime().equals(slotTime))
                .findFirst()
                .orElseThrow();

        assertEquals(6, fullSlot.getBookedCount());
        assertEquals(0, fullSlot.getRemainingSpots());
        assertTrue(fullSlot.isFull(), "Slot must be marked full when capacity reached");

        // Attempting to book a 7th member into this slot MUST fail
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> {
            slotService.bookSlot(activeMember.getId(), bookingDate, slotTime, focus, "Should exceed capacity");
        });

        assertTrue(ex.getMessage().contains("capacity") || ex.getMessage().contains("Full") || ex.getMessage().contains("available"),
                "Exception message should indicate slot capacity limit reached: " + ex.getMessage());
    }

    @Test
    @DisplayName("FR-9: Prevent double-booking same member at identical date and time")
    void testPreventDoubleBooking() {
        LocalDate bookingDate = LocalDate.now().plusDays(5);
        LocalTime slotTime = LocalTime.of(8, 0);

        // Book slot 1
        slotService.bookSlot(activeMember.getId(), bookingDate, slotTime, WorkoutFocus.PUSH, "Chest day");

        // Attempt to book another slot at the exact same date and time
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> {
            slotService.bookSlot(activeMember.getId(), bookingDate, slotTime, WorkoutFocus.PULL, "Back day");
        });

        assertTrue(ex.getMessage().contains("already have an active workout slot booked"),
                "Double booking should be prevented: " + ex.getMessage());
    }

    @Test
    @DisplayName("FR-4: Member can cancel booking and free up slot capacity")
    void testCancelBookingFreesCapacity() {
        LocalDate bookingDate = LocalDate.now().plusDays(3);
        LocalTime slotTime = LocalTime.of(19, 0);

        WorkoutSlotBooking booking = slotService.bookSlot(
                activeMember.getId(),
                bookingDate,
                slotTime,
                WorkoutFocus.LEGS,
                "Evening session"
        );

        // Check booked count is 1
        assertEquals(1, slotBookingRepository.countByBookingDateAndStartTimeAndWorkoutFocusAndStatus(bookingDate, slotTime, WorkoutFocus.LEGS, "CONFIRMED"));

        // Cancel booking
        slotService.cancelBooking(booking.getId(), activeMember.getId(), false);

        // Verify status is CANCELLED and count is back to 0
        WorkoutSlotBooking cancelledBooking = slotBookingRepository.findById(booking.getId()).orElseThrow();
        assertEquals("CANCELLED", cancelledBooking.getStatus());
        assertEquals(0, slotBookingRepository.countByBookingDateAndStartTimeAndWorkoutFocusAndStatus(bookingDate, slotTime, WorkoutFocus.LEGS, "CONFIRMED"));
    }

    @Test
    @DisplayName("FR-8: Inactive or unsubscribed members cannot reserve equipment slots")
    void testUnsubscribedMembersBlocked() {
        LocalDate bookingDate = LocalDate.now().plusDays(2);
        LocalTime slotTime = LocalTime.of(10, 0);

        // Inactive member blocked
        assertThrows(IllegalStateException.class, () -> {
            slotService.bookSlot(inactiveMember.getId(), bookingDate, slotTime, WorkoutFocus.LEGS, "Test");
        });

        // Non-subscribed member blocked
        assertThrows(IllegalStateException.class, () -> {
            slotService.bookSlot(nonSubscribedMember.getId(), bookingDate, slotTime, WorkoutFocus.LEGS, "Test");
        });
    }

    @Test
    @DisplayName("FR-6 & FR-7: Live capacity checks for trainers")
    void testTrainerCapacityChecks() {
        LocalDate bookingDate = LocalDate.now().plusDays(6);
        LocalTime slotTime = LocalTime.of(16, 0);

        // Initially capacity check should return true
        assertTrue(slotService.checkSlotCapacity(bookingDate, slotTime, WorkoutFocus.LEGS));

        Map<WorkoutFocus, Integer> summary = slotService.getRemainingCapacityMap(bookingDate, slotTime);
        assertNotNull(summary);
        assertEquals(6, summary.get(WorkoutFocus.LEGS));
        assertEquals(8, summary.get(WorkoutFocus.PUSH));
        assertEquals(8, summary.get(WorkoutFocus.PULL));
        assertEquals(10, summary.get(WorkoutFocus.CARDIO_FUNCTIONAL));
        assertEquals(10, summary.get(WorkoutFocus.FULL_BODY));
    }
}
