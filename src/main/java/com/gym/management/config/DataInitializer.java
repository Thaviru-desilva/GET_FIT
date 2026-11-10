package com.gym.management.config;

import com.gym.management.model.*;
import com.gym.management.repository.*;
import com.gym.management.service.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private final TrainerRepository trainerRepository;
    private final MembershipPlanRepository planRepository;
    private final MemberRepository memberRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final AttendanceRepository attendanceRepository;
    private final GymClassRepository gymClassRepository;
    private final ClassBookingRepository classBookingRepository;
    private final MemberProgressRepository progressRepository;
    private final WorkoutPlanRepository workoutPlanRepository;
    private final DietPlanRepository dietPlanRepository;
    private final GymResourceRepository resourceRepository;
    private final ResourceBookingRepository resourceBookingRepository;
    private final WorkoutSlotBookingRepository workoutSlotBookingRepository;
    private final UserService userService;

    public DataInitializer(TrainerRepository trainerRepository,
                           MembershipPlanRepository planRepository,
                           MemberRepository memberRepository,
                           SubscriptionRepository subscriptionRepository,
                           AttendanceRepository attendanceRepository,
                           GymClassRepository gymClassRepository,
                           ClassBookingRepository classBookingRepository,
                           MemberProgressRepository progressRepository,
                           WorkoutPlanRepository workoutPlanRepository,
                           DietPlanRepository dietPlanRepository,
                           GymResourceRepository resourceRepository,
                           ResourceBookingRepository resourceBookingRepository,
                           WorkoutSlotBookingRepository workoutSlotBookingRepository,
                           UserService userService) {
        this.trainerRepository = trainerRepository;
        this.planRepository = planRepository;
        this.memberRepository = memberRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.attendanceRepository = attendanceRepository;
        this.gymClassRepository = gymClassRepository;
        this.classBookingRepository = classBookingRepository;
        this.progressRepository = progressRepository;
        this.workoutPlanRepository = workoutPlanRepository;
        this.dietPlanRepository = dietPlanRepository;
        this.resourceRepository = resourceRepository;
        this.resourceBookingRepository = resourceBookingRepository;
        this.workoutSlotBookingRepository = workoutSlotBookingRepository;
        this.userService = userService;
    }

    @Override
    public void run(String... args) {
        if (trainerRepository.count() > 0) {
            return; // Data already initialized
        }

        // 1. Seed Trainers
        Trainer t1 = new Trainer("Alex", "Hunter", "alex.hunter@apexfit.com", "+1-555-0101", "CrossFit & Functional Training", 6, 48000.0);
        Trainer t2 = new Trainer("Elena", "Rostova", "elena.rostova@apexfit.com", "+1-555-0102", "Bodybuilding & Hypertrophy", 8, 55000.0);
        Trainer t3 = new Trainer("Marcus", "Chen", "marcus.chen@apexfit.com", "+1-555-0103", "HIIT & Cardio Conditioning", 4, 40000.0);
        Trainer t4 = new Trainer("Priya", "Sharma", "priya.sharma@apexfit.com", "+1-555-0104", "Yoga & Mobility Training", 5, 42000.0);
        trainerRepository.saveAll(List.of(t1, t2, t3, t4));

        // 2. Seed Membership Plans (Get U Fit Packages)
        MembershipPlan p1 = new MembershipPlan("Basic", "Basic gym membership package with full floor access.", 30, 5000.0);
        MembershipPlan p2 = new MembershipPlan("Standard", "Standard gym membership package with steam bath and group classes.", 90, 13500.0);
        MembershipPlan p3 = new MembershipPlan("Premium", "Premium gym membership package with sauna & dedicated personal trainer sessions.", 180, 24000.0);
        MembershipPlan p4 = new MembershipPlan("Annual", "Annual all-access gym membership package with complete facility privileges.", 365, 45000.0);
        MembershipPlan p5 = new MembershipPlan("Student", "Special discounted package for high school & university students.", 90, 10000.0);
        planRepository.saveAll(List.of(p1, p2, p3, p4, p5));

        // 3. Seed Members
        Member m1 = new Member("James", "Anderson", "james.anderson@example.com", "+1-555-1101", "Male", LocalDate.of(1994, 5, 12), t1);
        m1.setEmergencyContactName("Laura Anderson");
        m1.setEmergencyContactPhone("+1-555-9901");

        Member m2 = new Member("Sophia", "Miller", "sophia.miller@example.com", "+1-555-1102", "Female", LocalDate.of(1998, 8, 23), t2);
        m2.setEmergencyContactName("Robert Miller");
        m2.setEmergencyContactPhone("+1-555-9902");

        Member m3 = new Member("Lucas", "Garcia", "lucas.garcia@example.com", "+1-555-1103", "Male", LocalDate.of(1990, 11, 30), t3);
        m3.setEmergencyContactName("Maria Garcia");
        m3.setEmergencyContactPhone("+1-555-9903");

        Member m4 = new Member("Emma", "Watson", "emma.watson@example.com", "+1-555-1104", "Female", LocalDate.of(1995, 3, 15), t4);
        m4.setEmergencyContactName("Arthur Watson");
        m4.setEmergencyContactPhone("+1-555-9904");

        Member m5 = new Member("Liam", "Smith", "liam.smith@example.com", "+1-555-1105", "Male", LocalDate.of(1999, 1, 9), t1);
        m5.setStatus(MemberStatus.INACTIVE);

        memberRepository.saveAll(List.of(m1, m2, m3, m4, m5));

        // 4. Seed Subscriptions
        LocalDate now = LocalDate.now();
        Subscription s1 = new Subscription(m1, p3, now.minusMonths(2), now.minusMonths(2).plusDays(p3.getDurationDays()), p3.getPrice(), PaymentStatus.PAID);
        Subscription s2 = new Subscription(m2, p4, now.minusMonths(1), now.minusMonths(1).plusDays(p4.getDurationDays()), p4.getPrice(), PaymentStatus.PAID);
        Subscription s3 = new Subscription(m3, p1, now.minusDays(25), now.minusDays(25).plusDays(p1.getDurationDays()), p1.getPrice(), PaymentStatus.PAID); // Expiring soon
        Subscription s4 = new Subscription(m4, p2, now.minusDays(10), now.minusDays(10).plusDays(p2.getDurationDays()), p2.getPrice(), PaymentStatus.PAID);
        subscriptionRepository.saveAll(List.of(s1, s2, s3, s4));

        // 5. Seed Attendance Records for Today
        Attendance a1 = new Attendance(m1, now, LocalTime.of(7, 30), LocalTime.of(8, 45), "Upper Body Workout");
        Attendance a2 = new Attendance(m2, now, LocalTime.of(8, 0), LocalTime.of(9, 15), "Leg Day & Squats");
        Attendance a3 = new Attendance(m4, now, LocalTime.of(9, 30), null, "Vinyasa Yoga Session");
        Attendance a4 = new Attendance(m3, now, LocalTime.of(10, 15), null, "Cardio treadmill run");
        attendanceRepository.saveAll(List.of(a1, a2, a3, a4));

        // 6. Seed Gym Classes
        GymClass c1 = new GymClass("CrossFit WOD & Functional Power", "Intense metcon session with Olympic lifting and cardio drills.", t1, "CrossFit Box", "Monday, Wednesday, Friday", LocalTime.of(7, 0), LocalTime.of(8, 0), 12);
        GymClass c2 = new GymClass("Mindful Vinyasa Flow", "Restorative dynamic yoga practice to build flexibility and core strength.", t4, "Zen Sanctuary", "Tuesday, Thursday", LocalTime.of(8, 30), LocalTime.of(9, 30), 15);
        GymClass c3 = new GymClass("Powerlifting & Muscle Hypertrophy", "Heavy compound movements focusing on bench, squat, and deadlift technique.", t2, "Strength Arena", "Monday, Thursday", LocalTime.of(17, 30), LocalTime.of(18, 45), 10);
        GymClass c4 = new GymClass("HIIT Sprint & Core Burn", "High-octane interval conditioning for maximum caloric burn.", t3, "Studio A", "Wednesday, Saturday", LocalTime.of(9, 0), LocalTime.of(10, 0), 18);
        gymClassRepository.saveAll(List.of(c1, c2, c3, c4));

        // 7. Seed Class Bookings
        ClassBooking b1 = new ClassBooking(c2, m2, now); // Sophia in Yoga
        ClassBooking b2 = new ClassBooking(c2, m4, now); // Emma in Yoga
        ClassBooking b3 = new ClassBooking(c1, m1, now); // James in CrossFit
        ClassBooking b4 = new ClassBooking(c3, m3, now); // Lucas in Powerlifting
        classBookingRepository.saveAll(List.of(b1, b2, b3, b4));

        // 8. Seed User Accounts for Authentication (password: "password123")
        // Admin
        userService.createUser("admin@apexfit.com", "password123", "Chief Admin", UserRole.ROLE_ADMIN);

        // Trainers
        userService.createUserForTrainer(t1, "password123");
        userService.createUserForTrainer(t2, "password123");
        userService.createUserForTrainer(t3, "password123");
        userService.createUserForTrainer(t4, "password123");

        // Members
        userService.createUserForMember(m1, "password123");
        userService.createUserForMember(m2, "password123");
        userService.createUserForMember(m3, "password123");
        userService.createUserForMember(m4, "password123");
        userService.createUserForMember(m5, "password123");

        // 9. Seed Member Progress Logs (Tracking Weight, Height, BMI, Body Type, Hours, Goals)
        MemberProgress mp1_initial = new MemberProgress(
                m2,
                now.minusDays(30),
                62.5,
                168.0,
                "Mesomorph",
                3.5,
                "Lean Muscle Tone & Athletic Endurance",
                "Initial consultation & body composition assessment. Starting baseline strength test.",
                "TRAINER"
        );
        mp1_initial.setBodyFatPercentage(24.5);

        MemberProgress mp1_mid = new MemberProgress(
                m2,
                now.minusDays(14),
                61.0,
                168.0,
                "Mesomorph",
                4.5,
                "Lean Muscle Tone & Athletic Endurance",
                "Squat and Romanian Deadlift form improved. Feeling more energetic during cardio.",
                "MEMBER"
        );
        mp1_mid.setBodyFatPercentage(23.0);

        MemberProgress mp1_current = new MemberProgress(
                m2,
                now,
                59.8,
                168.0,
                "Mesomorph",
                5.0,
                "Lean Muscle Tone & Athletic Endurance",
                "Hit target weight milestone! Maintained strength while reducing body fat.",
                "MEMBER"
        );
        mp1_current.setBodyFatPercentage(21.8);

        MemberProgress mp2_initial = new MemberProgress(
                m1,
                now.minusDays(45),
                85.0,
                182.0,
                "Endomorph",
                3.0,
                "Functional Strength & Fat Loss",
                "Coach assessment: Focus on conditioning and clean nutrition.",
                "TRAINER"
        );
        mp2_initial.setBodyFatPercentage(23.5);

        MemberProgress mp2_current = new MemberProgress(
                m1,
                now.minusDays(2),
                81.5,
                182.0,
                "Endomorph",
                4.5,
                "Functional Strength & Fat Loss",
                "Down 3.5kg, overhead mobility significantly improved.",
                "TRAINER"
        );
        mp2_current.setBodyFatPercentage(20.0);

        MemberProgress mp3_current = new MemberProgress(
                m3,
                now,
                73.5,
                175.0,
                "Ectomorph",
                4.0,
                "Hypertrophy & Lean Bulk",
                "Tracking high calorie meals. Consistent bench press progress.",
                "MEMBER"
        );
        mp3_current.setBodyFatPercentage(14.0);

        progressRepository.saveAll(List.of(mp1_initial, mp1_mid, mp1_current, mp2_initial, mp2_current, mp3_current));

        // 10. Seed Custom Workout Plans Prescribed by Trainers
        WorkoutPlan wp1 = new WorkoutPlan(
                m2,
                t2,
                "Hypertrophy & Core Sculpt 4-Day Split",
                "Lean Muscle & Functional Strength",
                "Mesomorph",
                "4 Days / Week",
                8,
                now.minusWeeks(3),
                now.minusWeeks(3).plusWeeks(8),
                "• Day 1 (Lower Body Focus):\n  - Barbell Back Squat: 4 sets x 8 reps (2 min rest)\n  - Romanian Deadlifts: 3 sets x 10 reps\n  - Hip Thrusts: 4 sets x 12 reps\n  - Standing Calf Raises: 3 sets x 15 reps\n\n• Day 2 (Upper Body Push/Pull):\n  - Dumbbell Incline Press: 4 sets x 10 reps\n  - Wide Grip Lat Pulldown: 4 sets x 10 reps\n  - Seated Cable Rows: 3 sets x 12 reps\n  - Dumbbell Shoulder Press: 3 sets x 10 reps\n  - Hanging Knee Raises: 3 sets x 15 reps\n\n• Day 3: Active Rest / 30-min brisk walk & foam rolling\n\n• Day 4 (Glute & Posterior Chain):\n  - Bulgarian Split Squats: 3 sets x 10 reps per leg\n  - Lying Leg Curls: 4 sets x 12 reps\n  - Cable Pull-Through: 3 sets x 15 reps\n  - Farmer's Carry: 3 walks x 40m\n\n• Day 5 (Upper Conditioning & Core):\n  - Assisted Pull-ups: 3 sets x 8 reps\n  - Push-ups: 3 sets to failure\n  - Kettlebell Swings: 4 sets x 20 reps\n  - Plank to Push-up: 3 sets x 12 reps",
                "Ensure a 10-minute dynamic warmup before each session. Track progressive overload weekly by adding 1-2 kg to compound lifts when all sets are completed with clean form."
        );

        WorkoutPlan wp2 = new WorkoutPlan(
                m1,
                t1,
                "Endomorph Functional MetCon & Fat Shredder",
                "Fat Loss & Cardiovascular Endurance",
                "Endomorph",
                "5 Days / Week",
                6,
                now.minusWeeks(2),
                now.minusWeeks(2).plusWeeks(6),
                "• Day 1: CrossFit MetCon - 5 Rounds: 400m Row, 15 Kettlebell Swings, 10 Burpees\n• Day 2: Heavy Compound Strength - Deadlift 5x5, Front Squats 4x8, Overhead Press 4x8\n• Day 3: Low-Intensity Zone 2 Cardio (45 min incline walk / stationary bike)\n• Day 4: Functional Circuit - 21-15-9 reps of Thrusters, Box Jump-overs, Wall Balls\n• Day 5: Strongman Conditioning - Sled Push (4x25m), Battle Ropes (5x30s), Dumbbell Snatches\n• Weekend: Light Yoga & Rest",
                "Keep rest between circuit stations under 45 seconds to maximize metabolic output. Wear a heart rate monitor to stay within target aerobic zones."
        );
        workoutPlanRepository.saveAll(List.of(wp1, wp2));

        // 11. Seed Custom Diet & Nutrition Plans Prescribed by Trainers
        DietPlan dp1 = new DietPlan(
                m2,
                t2,
                "Lean Tone & High Protein Recomposition Fuel",
                "Lean Muscle Building & Fat Loss",
                2050,
                145,
                210,
                55,
                3.2,
                "Dairy-Sensitive / Low Lactose",
                "• Breakfast (07:30):\n  - 70g Rolled Oats cooked with unsweetened almond milk\n  - 1 scoop Plant/Isolate Protein powder\n  - 1 tbsp Chia seeds & 1/2 cup fresh berries\n\n• Mid-Morning Boost (10:30):\n  - 2 Boiled organic eggs\n  - 1 Medium green apple\n  - 12 Raw almonds\n\n• Lunch (13:00):\n  - 160g Grilled chicken breast (or pan-seared tofu)\n  - 1 cup Steamed brown jasmine rice\n  - 1.5 cups Steamed broccoli, zucchini, and carrots with 1 tsp olive oil\n\n• Pre-Workout Fuel (16:15):\n  - 2 Whole grain rice cakes with 1 tbsp natural peanut butter\n  - 1 Banana + Black coffee / green tea\n\n• Dinner (19:30):\n  - 180g Baked wild salmon fillet (or white fish)\n  - 150g Roasted sweet potato cubes\n  - Large leafy green salad with cucumber, cherry tomatoes, and lemon vinaigrette\n\n• Night Recovery (21:30):\n  - Chamomile tea + 1 scoop Casein/Plant protein shake",
                "Consume 500ml water immediately upon waking. Take 5g Creatine Monohydrate daily with post-workout meal. Prioritize 7.5 to 8 hours of sleep for optimum recovery."
        );

        DietPlan dp2 = new DietPlan(
                m1,
                t1,
                "Carb-Cycling Fat Loss & Energy Protocol",
                "Rapid Fat Loss & Sustained Energy",
                2300,
                180,
                170,
                70,
                4.0,
                "No Restrictions",
                "• Breakfast: 3 Whole scrambled eggs + 2 egg whites, spinach, sliced avocado, 1 slice sourdough toast\n• Mid-Morning: Whey isolate shake with unsweetened oat milk and a handful of walnuts\n• Lunch: 180g Lean minced beef bowl with quinoa, roasted bell peppers, and avocado salsa\n• Pre-Workout: 1 Apple and black coffee with a pinch of sea salt\n• Dinner: 200g Grilled lemon-herb chicken thighs with roasted cauliflower and asparagus mash",
                "Carb cycling strategy: On high intensity MetCon days (Monday/Thursday), increase sweet potato/rice portions by 25%. Drink 1 liter of water during training."
        );
        dietPlanRepository.saveAll(List.of(dp1, dp2));

        // 12. Seed Limited Gym Resources & Equipment
        GymResource gr1 = new GymResource("Olympic Squat Rack & Lifting Platform", "STRENGTH", "Free Weight Zone", 3, 2, "AVAILABLE", "Eleiko Olympic barbell, bumper plates, safety spotters, and deadlift platform.");
        GymResource gr2 = new GymResource("Commercial Treadmill Matrix T70", "CARDIO", "Cardio Deck", 6, 4, "AVAILABLE", "Touchscreen console with sprint telemetry, heart-rate sensors, and auto-incline.");
        GymResource gr3 = new GymResource("Dual Adjustable Cable Cross Station", "STRENGTH", "Resistance Zone", 2, 1, "AVAILABLE", "Multi-pulley dual stack 100kg weight system with tricep ropes and D-handles.");
        GymResource gr4 = new GymResource("Concept2 Rowing Ergometer PM5", "FUNCTIONAL", "Functional Turf", 4, 3, "AVAILABLE", "Air resistance ergometer with PM5 performance monitor for interval training.");
        GymResource gr5 = new GymResource("Schwinn AC Performance Spin Bikes", "CARDIO", "Spin Studio B", 12, 9, "AVAILABLE", "Magnetic resistance spin cycle with carbon blue belt drive.");
        GymResource gr6 = new GymResource("VIP 1-on-1 Personal Training Studio Pod", "STUDIO", "VIP Coaching Wing", 2, 1, "AVAILABLE", "Private acoustic coaching studio equipped with dumbbells, kettlebells, and bench.");
        GymResource gr7 = new GymResource("Finnish Cedarwood Infrared Sauna Suite", "RECOVERY", "Wellness & Spa", 2, 1, "AVAILABLE", "Therapeutic recovery sauna for post-workout muscle rejuvenation.");
        GymResource gr8 = new GymResource("Smart RFID Member Lockers - Block A", "LOCKER", "Main Locker Room", 30, 22, "AVAILABLE", "Keyless electronic lockers with USB charging points.");

        resourceRepository.saveAll(List.of(gr1, gr2, gr3, gr4, gr5, gr6, gr7, gr8));

        // Seed Sample Resource Reservations
        ResourceBooking rb1 = new ResourceBooking(gr1, m2, t2, now, LocalTime.of(16, 0), LocalTime.of(17, 0), "Heavy Squats & Romanian Deadlifts PT Session");
        ResourceBooking rb2 = new ResourceBooking(gr3, m1, t1, now, LocalTime.of(17, 30), LocalTime.of(18, 30), "Cable Chest Flyes & Lat Pull-in");
        ResourceBooking rb3 = new ResourceBooking(gr6, m3, null, now, LocalTime.of(18, 0), LocalTime.of(19, 0), "Private Mobility & Core Workout");
        resourceBookingRepository.saveAll(List.of(rb1, rb2, rb3));

        // 13. Seed Workout Slot Reservations for Anti-Overcrowding System
        WorkoutSlotBooking wsb1 = new WorkoutSlotBooking(m1, now, LocalTime.of(18, 0), LocalTime.of(19, 0), WorkoutFocus.LEGS, "Heavy Squat Progression");
        WorkoutSlotBooking wsb2 = new WorkoutSlotBooking(m2, now, LocalTime.of(18, 0), LocalTime.of(19, 0), WorkoutFocus.LEGS, "Leg Press & Hamstring focus");
        WorkoutSlotBooking wsb3 = new WorkoutSlotBooking(m3, now, LocalTime.of(18, 0), LocalTime.of(19, 0), WorkoutFocus.LEGS, "Quad Hypertrophy Routine");
        WorkoutSlotBooking wsb4 = new WorkoutSlotBooking(m4, now, LocalTime.of(18, 0), LocalTime.of(19, 0), WorkoutFocus.LEGS, "Barbell Lunges & Glute Bridge");
        WorkoutSlotBooking wsb5 = new WorkoutSlotBooking(m2, now, LocalTime.of(9, 0), LocalTime.of(10, 0), WorkoutFocus.PUSH, "Incline DB Press & Triceps");
        WorkoutSlotBooking wsb6 = new WorkoutSlotBooking(m3, now.plusDays(1), LocalTime.of(18, 0), LocalTime.of(19, 0), WorkoutFocus.LEGS, "Reserved slot");
        WorkoutSlotBooking wsb7 = new WorkoutSlotBooking(m4, now.plusDays(1), LocalTime.of(18, 0), LocalTime.of(19, 0), WorkoutFocus.LEGS, "Squat Day");

        workoutSlotBookingRepository.saveAll(List.of(wsb1, wsb2, wsb3, wsb4, wsb5, wsb6, wsb7));
    }
}
