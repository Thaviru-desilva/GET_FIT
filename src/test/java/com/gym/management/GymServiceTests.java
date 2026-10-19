package com.gym.management;

import com.gym.management.model.*;
import com.gym.management.service.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class GymServiceTests {

    @Autowired
    private MemberService memberService;

    @Autowired
    private MembershipPlanService planService;

    @Autowired
    private SubscriptionService subscriptionService;

    @Autowired
    private AttendanceService attendanceService;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private GymClassService gymClassService;

    @Autowired
    private MemberProgressService progressService;

    @Autowired
    private FitnessPlanService fitnessPlanService;

    @Autowired
    private TrainerService trainerService;

    @Autowired
    private GymResourceService resourceService;

    @Test
    void testInitialDataLoaded() {
        List<Member> members = memberService.findAll();
        assertFalse(members.isEmpty(), "Members should be pre-seeded by DataInitializer");

        List<MembershipPlan> plans = planService.findAll();
        assertFalse(plans.isEmpty(), "Plans should be pre-seeded");

        List<GymClass> classes = gymClassService.findAll();
        assertFalse(classes.isEmpty(), "Classes should be pre-seeded");
    }

    @Test
    void testUserDetailsService() {
        UserDetails adminUser = userDetailsService.loadUserByUsername("admin@apexfit.com");
        assertNotNull(adminUser);
        assertTrue(adminUser.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));

        UserDetails trainerUser = userDetailsService.loadUserByUsername("elena.rostova@apexfit.com");
        assertNotNull(trainerUser);
        assertTrue(trainerUser.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_TRAINER")));

        UserDetails memberUser = userDetailsService.loadUserByUsername("sophia.miller@example.com");
        assertNotNull(memberUser);
        assertTrue(memberUser.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_MEMBER")));
    }

    @Test
    void testClassBookingAndCancellation() {
        List<GymClass> classes = gymClassService.findAll();
        GymClass gymClass = classes.get(0);

        List<Member> members = memberService.findAll();
        Member member = members.get(members.size() - 1); // e.g. Liam Smith

        LocalDate tomorrow = LocalDate.now().plusDays(1);
        ClassBooking booking = gymClassService.bookClass(gymClass.getId(), member.getId(), tomorrow);
        assertNotNull(booking.getId());
        assertEquals("CONFIRMED", booking.getStatus());

        // Cancel
        gymClassService.cancelBooking(booking.getId());
        List<ClassBooking> memberBookings = gymClassService.getMemberBookings(member.getId());
        ClassBooking cancelled = memberBookings.stream()
                .filter(b -> b.getId().equals(booking.getId()))
                .findFirst()
                .orElseThrow();
        assertEquals("CANCELLED", cancelled.getStatus());
    }

    @Test
    void testMemberCreationAndSearch() {
        Member newMember = new Member("Bruce", "Wayne", "bruce.wayne@example.com", "+1-555-8888", "Male", LocalDate.of(1985, 2, 19), null);
        Member saved = memberService.save(newMember);

        assertNotNull(saved.getId());
        assertEquals("Bruce Wayne", saved.getFullName());

        List<Member> searchResult = memberService.search("bruce");
        assertFalse(searchResult.isEmpty());
        assertEquals("bruce.wayne@example.com", searchResult.get(0).getEmail());
    }

    @Test
    void testCheckInAndCheckOut() {
        List<Member> members = memberService.findAll();
        Member member = members.get(0);

        Attendance attendance = attendanceService.checkIn(member.getId(), "Morning Cardio");
        assertNotNull(attendance.getId());
        assertNull(attendance.getCheckOutTime());

        attendanceService.checkOut(attendance.getId());
        Attendance updated = attendanceService.findAll().stream()
                .filter(a -> a.getId().equals(attendance.getId()))
                .findFirst()
                .orElseThrow();
        assertNotNull(updated.getCheckOutTime());
    }

    @Test
    void testMemberProgressLoggingAndBmi() {
        List<Member> members = memberService.findAll();
        Member member = members.get(0);

        // Log progress: 70 kg, 175 cm
        MemberProgress progress = progressService.logProgress(
                member.getId(),
                70.0,
                175.0,
                "Mesomorph",
                4.5,
                16.0,
                "Hypertrophy",
                "Felt great during squat session",
                "MEMBER"
        );

        assertNotNull(progress.getId());
        assertEquals(70.0, progress.getWeightKg());
        assertEquals(175.0, progress.getHeightCm());
        // BMI = 70 / (1.75 * 1.75) = 22.857... -> rounded to 22.9
        assertEquals(22.9, progress.getBmi());
        assertEquals("Normal Weight", progress.getBmiCategory());
        assertEquals("Mesomorph", progress.getBodyType());
        assertEquals("MEMBER", progress.getLoggedBy());

        // Latest progress should return this record
        MemberProgress latest = progressService.getLatestProgress(member.getId()).orElseThrow();
        assertEquals(progress.getId(), latest.getId());

        // History should include all logged records
        List<MemberProgress> history = progressService.getProgressHistory(member.getId());
        assertFalse(history.isEmpty());
        assertTrue(history.stream().anyMatch(p -> p.getId().equals(progress.getId())));
    }

    @Test
    void testWorkoutAndDietPlanManagement() {
        List<Member> members = memberService.findAll();
        Member member = members.get(0);

        List<Trainer> trainers = trainerService.findAll();
        Trainer trainer = trainers.get(0);

        // 1. Create Workout Plan
        WorkoutPlan workout = new WorkoutPlan(
                member,
                trainer,
                "Strength Hypertrophy 5-Day Split",
                "Strength & Muscle Tone",
                "Mesomorph",
                "5 Days / Week",
                10,
                LocalDate.now(),
                LocalDate.now().plusWeeks(10),
                "Day 1: Chest & Triceps\nDay 2: Back & Biceps\nDay 3: Legs\nDay 4: Shoulders\nDay 5: Full Body MetCon",
                "Warmup 10 mins dynamically. Rest 90 seconds between sets."
        );
        WorkoutPlan savedWorkout = fitnessPlanService.saveWorkoutPlan(workout);
        assertNotNull(savedWorkout.getId());
        assertEquals("ACTIVE", savedWorkout.getStatus());

        // Retrieve active workout plan for member
        WorkoutPlan activeWorkout = fitnessPlanService.getActiveWorkoutPlanForMember(member.getId()).orElseThrow();
        assertEquals(savedWorkout.getId(), activeWorkout.getId());

        // 2. Create Diet Plan
        DietPlan diet = new DietPlan(
                member,
                trainer,
                "High Protein Muscle Fuel",
                "Clean Bulking",
                2500,
                175,
                260,
                65,
                3.5,
                "Nut Allergy",
                "Breakfast: Oats + Whey + Banana\nLunch: Chicken + Rice + Greens\nDinner: Steak + Sweet Potato",
                "Take 5g creatine monohydrate daily with post-workout meal."
        );
        DietPlan savedDiet = fitnessPlanService.saveDietPlan(diet);
        assertNotNull(savedDiet.getId());
        assertEquals(2500, savedDiet.getTargetCalories());
        assertEquals(175, savedDiet.getProteinGrams());

        // Retrieve active diet plan for member
        DietPlan activeDiet = fitnessPlanService.getActiveDietPlanForMember(member.getId()).orElseThrow();
        assertEquals(savedDiet.getId(), activeDiet.getId());

        // Check trainer plan listings
        List<WorkoutPlan> trainerWorkouts = fitnessPlanService.getWorkoutPlansForTrainer(trainer.getId());
        assertTrue(trainerWorkouts.stream().anyMatch(w -> w.getId().equals(savedWorkout.getId())));

        List<DietPlan> trainerDiets = fitnessPlanService.getDietPlansForTrainer(trainer.getId());
        assertTrue(trainerDiets.stream().anyMatch(d -> d.getId().equals(savedDiet.getId())));
    }

    @Test
    void testGymResourceAndEquipmentManagement() {
        List<GymResource> resources = resourceService.findAll();
        assertFalse(resources.isEmpty(), "Gym resources should be pre-seeded");

        GymResource squatRack = resources.stream()
                .filter(r -> r.getName().contains("Squat Rack"))
                .findFirst()
                .orElseThrow();

        int initialAvailable = squatRack.getAvailableQuantity();
        assertTrue(initialAvailable > 0);

        List<Member> members = memberService.findAll();
        Member member = members.get(0);

        // Member books squat rack for a workout session
        ResourceBooking booking = resourceService.bookResource(
                squatRack.getId(),
                member.getId(),
                null,
                LocalDate.now(),
                java.time.LocalTime.of(15, 0),
                java.time.LocalTime.of(16, 0),
                "Squats & Lunges progression"
        );

        assertNotNull(booking.getId());
        assertEquals("CONFIRMED", booking.getStatus());

        // Available count decremented
        GymResource updatedRack = resourceService.findById(squatRack.getId()).orElseThrow();
        assertEquals(initialAvailable - 1, updatedRack.getAvailableQuantity());

        // Cancel booking restores availability
        resourceService.cancelBooking(booking.getId());
        GymResource restoredRack = resourceService.findById(squatRack.getId()).orElseThrow();
        assertEquals(initialAvailable, restoredRack.getAvailableQuantity());

        // Check floor capacity tracking
        int maxCapacity = resourceService.getMaxFloorCapacity();
        assertEquals(50, maxCapacity);
        int currentOccupancy = resourceService.getCurrentFloorOccupancy();
        assertTrue(currentOccupancy >= 0);
        int percentage = resourceService.getFloorOccupancyPercentage();
        assertTrue(percentage >= 0 && percentage <= 100);
    }
}
