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

    @Test
    void testAttendanceDuplicateCheckInBlocked() {
        List<Member> members = memberService.findAll();
        // Pick an active member who currently has no open check-in
        Member activeMember = members.stream()
                .filter(m -> m.getStatus() == MemberStatus.ACTIVE)
                .findFirst()
                .orElseThrow();

        // 1st Check-in succeeds
        Attendance firstCheckIn = attendanceService.checkIn(activeMember.getId(), "Cardio Zone Session");
        assertNotNull(firstCheckIn.getId());

        // 2nd Check-in without checkout should throw IllegalStateException
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            attendanceService.checkIn(activeMember.getId(), "Trying duplicate check-in");
        });
        assertTrue(exception.getMessage().contains("already checked in today without checking out"));

        // Clean up by checking out
        attendanceService.checkOut(firstCheckIn.getId());
    }

    @Test
    void testAttendanceInactiveMemberBlocked() {
        List<Member> members = memberService.findAll();
        Member inactiveMember = members.stream()
                .filter(m -> m.getStatus() == MemberStatus.INACTIVE)
                .findFirst()
                .orElseThrow();

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            attendanceService.checkIn(inactiveMember.getId(), "Attempting entry while inactive");
        });
        assertTrue(exception.getMessage().contains("Membership must be ACTIVE"));
    }

    @Test
    void testGymResourceFutureBookingDoesNotDepleteTodayInventory() {
        List<GymResource> resources = resourceService.findAll();
        GymResource resource = resources.get(0);
        int availableBefore = resource.getAvailableQuantity();

        List<Member> members = memberService.findAll();
        Member member = members.stream().filter(m -> m.getStatus() == MemberStatus.ACTIVE).findFirst().orElseThrow();

        // Book for 5 days in the future
        LocalDate futureDate = LocalDate.now().plusDays(5);
        ResourceBooking futureBooking = resourceService.bookResource(
                resource.getId(),
                member.getId(),
                null,
                futureDate,
                java.time.LocalTime.of(10, 0),
                java.time.LocalTime.of(11, 0),
                "Future session"
        );
        assertNotNull(futureBooking.getId());

        // Today's available quantity MUST NOT change
        GymResource afterFutureBooking = resourceService.findById(resource.getId()).orElseThrow();
        assertEquals(availableBefore, afterFutureBooking.getAvailableQuantity(),
                "Future booking must not decrement today's available inventory");

        // Cancel future booking
        resourceService.cancelBooking(futureBooking.getId());
        GymResource afterCancel = resourceService.findById(resource.getId()).orElseThrow();
        assertEquals(availableBefore, afterCancel.getAvailableQuantity(),
                "Cancelling future booking must not alter today's available inventory");
    }

    @Test
    void testSupersedingPreviousActivePlans() {
        List<Member> members = memberService.findAll();
        Member member = members.stream().filter(m -> m.getStatus() == MemberStatus.ACTIVE).findFirst().orElseThrow();
        List<Trainer> trainers = trainerService.findAll();
        Trainer trainer = trainers.get(0);

        // 1. Save Initial Workout Plan
        WorkoutPlan plan1 = new WorkoutPlan(member, trainer, "Initial Routine", "Fat Loss", "Endomorph", "3 Days / Week", 4, LocalDate.now(), LocalDate.now().plusWeeks(4), "Routine 1", "Advice 1");
        WorkoutPlan savedPlan1 = fitnessPlanService.saveWorkoutPlan(plan1);
        assertEquals("ACTIVE", savedPlan1.getStatus());

        // 2. Save Second Workout Plan
        WorkoutPlan plan2 = new WorkoutPlan(member, trainer, "Advanced Routine", "Hypertrophy", "Endomorph", "5 Days / Week", 6, LocalDate.now(), LocalDate.now().plusWeeks(6), "Routine 2", "Advice 2");
        WorkoutPlan savedPlan2 = fitnessPlanService.saveWorkoutPlan(plan2);
        assertEquals("ACTIVE", savedPlan2.getStatus());

        // Verify Plan 1 is now SUPERSEDED
        WorkoutPlan retrievedPlan1 = fitnessPlanService.getWorkoutPlanById(savedPlan1.getId()).orElseThrow();
        assertEquals("SUPERSEDED", retrievedPlan1.getStatus(), "Previous active workout plan must be superseded");

        // Verify Active plan for member is Plan 2
        WorkoutPlan activePlan = fitnessPlanService.getActiveWorkoutPlanForMember(member.getId()).orElseThrow();
        assertEquals(savedPlan2.getId(), activePlan.getId());

        // 3. Same for Diet Plans
        DietPlan diet1 = new DietPlan(member, trainer, "Phase 1 Diet", "Maintenance", 2000, 140, 200, 50, 3.0, null, "Meals 1", "Water");
        DietPlan savedDiet1 = fitnessPlanService.saveDietPlan(diet1);
        assertEquals("ACTIVE", savedDiet1.getStatus());

        DietPlan diet2 = new DietPlan(member, trainer, "Phase 2 Diet", "Cut", 1800, 160, 150, 40, 3.5, null, "Meals 2", "Vitamins");
        DietPlan savedDiet2 = fitnessPlanService.saveDietPlan(diet2);
        assertEquals("ACTIVE", savedDiet2.getStatus());

        DietPlan retrievedDiet1 = fitnessPlanService.getDietPlanById(savedDiet1.getId()).orElseThrow();
        assertEquals("SUPERSEDED", retrievedDiet1.getStatus(), "Previous active diet plan must be superseded");
    }

    @Test
    void testExpiredSubscriptionStatusUpdate() {
        List<Member> members = memberService.findAll();
        Member member = members.stream().filter(m -> m.getStatus() == MemberStatus.ACTIVE).findFirst().orElseThrow();
        List<MembershipPlan> plans = planService.findAll();
        MembershipPlan plan = plans.get(0);

        // Create subscription with end date in the past
        LocalDate pastStart = LocalDate.now().minusDays(60);
        LocalDate pastEnd = LocalDate.now().minusDays(30);
        Subscription expiredSub = new Subscription(member, plan, pastStart, pastEnd, plan.getPrice(), PaymentStatus.PAID);
        expiredSub.setStatus(SubscriptionStatus.ACTIVE); // Manually set to ACTIVE
        expiredSub = subscriptionService.createSubscription(member.getId(), plan.getId(), pastStart, plan.getPrice(), PaymentStatus.PAID);
        // Ensure its end date is past
        expiredSub.setEndDate(pastEnd);
        expiredSub.setStatus(SubscriptionStatus.ACTIVE);

        assertTrue(expiredSub.isExpired());
        assertEquals(SubscriptionStatus.EXPIRED, expiredSub.getStatus(), "Entity getter must resolve to EXPIRED");

        // When subscriptions are synchronized, active count reflects properly
        subscriptionService.updateExpiredSubscriptions();
    }

    @Test
    void testMemberProgressMetricValidation() {
        Member member = memberService.save(new Member("Metric", "Tester", "metric.tester@example.com", "+1-555-9988", "Other", LocalDate.of(1995, 1, 1), null));

        // Valid progress entry
        MemberProgress valid = progressService.logProgress(member.getId(), 75.0, 180.0, "Mesomorph", 5.0, 15.0, "Muscle Gain", "Feeling great", "MEMBER");
        assertNotNull(valid.getId());
        assertEquals(75.0, valid.getWeightKg());

        // Invalid weight (< 20 kg or > 350 kg)
        assertThrows(IllegalArgumentException.class, () ->
                progressService.logProgress(member.getId(), 5.0, 180.0, "Mesomorph", 5.0, 15.0, "Goal", "Notes", "MEMBER")
        );
        assertThrows(IllegalArgumentException.class, () ->
                progressService.logProgress(member.getId(), 500.0, 180.0, "Mesomorph", 5.0, 15.0, "Goal", "Notes", "MEMBER")
        );

        // Invalid height (< 80 cm or > 250 cm)
        assertThrows(IllegalArgumentException.class, () ->
                progressService.logProgress(member.getId(), 75.0, 30.0, "Mesomorph", 5.0, 15.0, "Goal", "Notes", "MEMBER")
        );

        // Invalid body fat % (> 60% or < 3%)
        assertThrows(IllegalArgumentException.class, () ->
                progressService.logProgress(member.getId(), 75.0, 180.0, "Mesomorph", 5.0, 80.0, "Goal", "Notes", "MEMBER")
        );
    }

    @Test
    void testEditWorkoutPlanDoesNotSupersedeSelf() {
        List<Member> members = memberService.findAll();
        Member member = members.get(1);
        List<Trainer> trainers = trainerService.findAll();
        Trainer trainer = trainers.get(0);

        // Create plan
        WorkoutPlan plan = new WorkoutPlan(member, trainer, "Original Routine", "Strength", "Mesomorph", "4 Days", 4, LocalDate.now(), LocalDate.now().plusWeeks(4), "Original schedule", "Advice");
        plan = fitnessPlanService.saveWorkoutPlan(plan);
        assertEquals("ACTIVE", plan.getStatus());

        // Update/edit same plan
        plan.setTitle("Edited Routine Updated");
        plan.setScheduleDetails("Updated schedule details with more volume");
        WorkoutPlan edited = fitnessPlanService.saveWorkoutPlan(plan);

        assertEquals("ACTIVE", edited.getStatus());
        assertEquals("Edited Routine Updated", edited.getTitle());
    }

    @Test
    void testUnassignedMembersAndSelfAssignment() {
        // Create a member without trainer
        Member unassigned = new Member("Alex", "Mercer", "alex.mercer@example.com", "+1-555-7722", "Male", LocalDate.of(1996, 4, 12), null);
        Member savedUnassigned = memberService.save(unassigned);
        assertNull(savedUnassigned.getAssignedTrainer());
        Long unassignedId = savedUnassigned.getId();

        // Verify member appears in unassigned list
        List<Member> unassignedList = memberService.findAll().stream()
                .filter(m -> m.getAssignedTrainer() == null)
                .toList();
        assertTrue(unassignedList.stream().anyMatch(m -> m.getId().equals(unassignedId)));

        // Trainer self-assigns this member
        Trainer trainer = trainerService.findAll().get(0);
        savedUnassigned.setAssignedTrainer(trainer);
        Member assigned = memberService.save(savedUnassigned);
        assertNotNull(assigned.getAssignedTrainer());
        assertEquals(trainer.getId(), assigned.getAssignedTrainer().getId());
    }
}
