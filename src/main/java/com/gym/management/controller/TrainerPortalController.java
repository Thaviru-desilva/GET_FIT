package com.gym.management.controller;

import com.gym.management.model.*;
import com.gym.management.service.*;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/trainer")
public class TrainerPortalController {

    private final UserService userService;
    private final GymClassService gymClassService;
    private final AttendanceService attendanceService;
    private final MemberService memberService;
    private final MemberProgressService progressService;
    private final FitnessPlanService fitnessPlanService;
    private final GymResourceService resourceService;

    public TrainerPortalController(UserService userService,
                                   GymClassService gymClassService,
                                   AttendanceService attendanceService,
                                   MemberService memberService,
                                   MemberProgressService progressService,
                                   FitnessPlanService fitnessPlanService,
                                   GymResourceService resourceService) {
        this.userService = userService;
        this.gymClassService = gymClassService;
        this.attendanceService = attendanceService;
        this.memberService = memberService;
        this.progressService = progressService;
        this.fitnessPlanService = fitnessPlanService;
        this.resourceService = resourceService;
    }

    @GetMapping("/dashboard")
    public String trainerDashboard(Authentication auth, Model model) {
        if (auth == null) {
            return "redirect:/login";
        }

        Trainer trainer = userService.findTrainerByUsername(auth.getName()).orElse(null);
        if (trainer == null) {
            return "redirect:/dashboard";
        }

        List<GymClass> myClasses = gymClassService.findByTrainerId(trainer.getId());
        List<Member> myMembers = trainer.getMembers();
        List<Member> allMembers = memberService.findAll();
        List<Member> unassignedMembers = allMembers.stream()
                .filter(m -> m.getAssignedTrainer() == null)
                .toList();
        List<WorkoutPlan> myWorkoutPlans = fitnessPlanService.getWorkoutPlansForTrainer(trainer.getId());
        List<DietPlan> myDietPlans = fitnessPlanService.getDietPlansForTrainer(trainer.getId());

        model.addAttribute("trainer", trainer);
        model.addAttribute("classes", myClasses);
        model.addAttribute("members", myMembers);
        model.addAttribute("allMembers", allMembers);
        model.addAttribute("unassignedMembers", unassignedMembers);
        model.addAttribute("workoutPlansCount", myWorkoutPlans.size());
        model.addAttribute("dietPlansCount", myDietPlans.size());
        model.addAttribute("todayAttendance", attendanceService.getTodayAttendance());

        // Limited Gym Equipment & Capacity
        model.addAttribute("resources", resourceService.findAll());
        model.addAttribute("resourceBookingsCount", resourceService.getTrainerBookings(trainer.getId()).size());
        model.addAttribute("floorOccupancy", resourceService.getCurrentFloorOccupancy());
        model.addAttribute("maxFloorCapacity", resourceService.getMaxFloorCapacity());
        model.addAttribute("floorOccupancyPercentage", resourceService.getFloorOccupancyPercentage());

        return "trainer/dashboard";
    }

    // Assign unassigned member to self
    @PostMapping("/members/{memberId}/assign-self")
    public String assignMemberToSelf(@PathVariable("memberId") Long memberId,
                                     Authentication auth,
                                     RedirectAttributes redirectAttributes) {
        Trainer trainer = getAuthenticatedTrainer(auth);
        if (trainer == null) return "redirect:/dashboard";

        memberService.findById(memberId).ifPresent(m -> {
            m.setAssignedTrainer(trainer);
            memberService.save(m);
        });

        redirectAttributes.addFlashAttribute("successMessage", "Athlete assigned to your 1-on-1 coaching roster!");
        return "redirect:/trainer/dashboard";
    }

    // View Member Progress & Plans Management for Trainer
    @GetMapping("/members/{memberId}/plans")
    public String viewMemberPlansAndProgress(@PathVariable("memberId") Long memberId, Authentication auth, Model model, RedirectAttributes redirectAttributes) {
        Trainer trainer = getAuthenticatedTrainer(auth);
        if (trainer == null) return "redirect:/dashboard";

        return memberService.findById(memberId).map(member -> {
            model.addAttribute("trainer", trainer);
            model.addAttribute("member", member);
            model.addAttribute("progressList", progressService.getProgressHistory(memberId));
            model.addAttribute("latestProgress", progressService.getLatestProgress(memberId).orElse(null));
            model.addAttribute("workoutPlans", fitnessPlanService.getWorkoutPlansForMember(memberId));
            model.addAttribute("dietPlans", fitnessPlanService.getDietPlansForMember(memberId));
            model.addAttribute("newProgress", new MemberProgress());
            return "trainer/member-plans";
        }).orElseGet(() -> {
            redirectAttributes.addFlashAttribute("errorMessage", "Member not found.");
            return "redirect:/trainer/dashboard";
        });
    }

    // New Workout Plan Form
    @GetMapping("/members/{memberId}/workout-plan/new")
    public String newWorkoutPlanForm(@PathVariable("memberId") Long memberId, Authentication auth, Model model, RedirectAttributes redirectAttributes) {
        Trainer trainer = getAuthenticatedTrainer(auth);
        if (trainer == null) return "redirect:/dashboard";

        return memberService.findById(memberId).map(member -> {
            WorkoutPlan plan = new WorkoutPlan();
            plan.setMember(member);
            plan.setTrainer(trainer);
            plan.setStartDate(LocalDate.now());

            model.addAttribute("trainer", trainer);
            model.addAttribute("member", member);
            model.addAttribute("workoutPlan", plan);
            model.addAttribute("latestProgress", progressService.getLatestProgress(memberId).orElse(null));
            model.addAttribute("isEdit", false);
            return "trainer/workout-plan-form";
        }).orElseGet(() -> {
            redirectAttributes.addFlashAttribute("errorMessage", "Member not found.");
            return "redirect:/trainer/dashboard";
        });
    }

    // Edit Workout Plan Form
    @GetMapping("/members/{memberId}/workout-plan/edit/{planId}")
    public String editWorkoutPlanForm(@PathVariable("memberId") Long memberId,
                                      @PathVariable("planId") Long planId,
                                      Authentication auth,
                                      Model model,
                                      RedirectAttributes redirectAttributes) {
        Trainer trainer = getAuthenticatedTrainer(auth);
        if (trainer == null) return "redirect:/dashboard";

        Member member = memberService.findById(memberId).orElse(null);
        if (member == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Member not found.");
            return "redirect:/trainer/dashboard";
        }

        WorkoutPlan plan = fitnessPlanService.getWorkoutPlanById(planId).orElse(null);
        if (plan == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Workout plan not found.");
            return "redirect:/trainer/members/" + memberId + "/plans";
        }

        model.addAttribute("trainer", trainer);
        model.addAttribute("member", member);
        model.addAttribute("workoutPlan", plan);
        model.addAttribute("latestProgress", progressService.getLatestProgress(memberId).orElse(null));
        model.addAttribute("isEdit", true);
        return "trainer/workout-plan-form";
    }

    // Save Workout Plan
    @PostMapping("/members/{memberId}/workout-plan/save")
    public String saveWorkoutPlan(@PathVariable("memberId") Long memberId,
                                  @Valid @ModelAttribute("workoutPlan") WorkoutPlan workoutPlan,
                                  BindingResult result,
                                  Authentication auth,
                                  Model model,
                                  RedirectAttributes redirectAttributes) {
        Trainer trainer = getAuthenticatedTrainer(auth);
        if (trainer == null) return "redirect:/dashboard";

        Member member = memberService.findById(memberId).orElse(null);
        if (member == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Member not found.");
            return "redirect:/trainer/dashboard";
        }

        if (result.hasErrors()) {
            model.addAttribute("trainer", trainer);
            model.addAttribute("member", member);
            model.addAttribute("latestProgress", progressService.getLatestProgress(memberId).orElse(null));
            model.addAttribute("isEdit", workoutPlan.getId() != null);
            return "trainer/workout-plan-form";
        }

        boolean isUpdate = workoutPlan.getId() != null;
        workoutPlan.setMember(member);
        workoutPlan.setTrainer(trainer);
        if (workoutPlan.getDurationWeeks() != null && workoutPlan.getStartDate() != null) {
            workoutPlan.setEndDate(workoutPlan.getStartDate().plusWeeks(workoutPlan.getDurationWeeks()));
        }
        fitnessPlanService.saveWorkoutPlan(workoutPlan);

        String msg = isUpdate ? "Custom exercise plan updated successfully for " + member.getFullName() + "!"
                              : "Custom exercise plan created successfully for " + member.getFullName() + "!";
        redirectAttributes.addFlashAttribute("successMessage", msg);
        return "redirect:/trainer/members/" + memberId + "/plans";
    }

    // New Diet Plan Form
    @GetMapping("/members/{memberId}/diet-plan/new")
    public String newDietPlanForm(@PathVariable("memberId") Long memberId, Authentication auth, Model model, RedirectAttributes redirectAttributes) {
        Trainer trainer = getAuthenticatedTrainer(auth);
        if (trainer == null) return "redirect:/dashboard";

        return memberService.findById(memberId).map(member -> {
            DietPlan plan = new DietPlan();
            plan.setMember(member);
            plan.setTrainer(trainer);
            plan.setStartDate(LocalDate.now());

            model.addAttribute("trainer", trainer);
            model.addAttribute("member", member);
            model.addAttribute("dietPlan", plan);
            model.addAttribute("latestProgress", progressService.getLatestProgress(memberId).orElse(null));
            model.addAttribute("isEdit", false);
            return "trainer/diet-plan-form";
        }).orElseGet(() -> {
            redirectAttributes.addFlashAttribute("errorMessage", "Member not found.");
            return "redirect:/trainer/dashboard";
        });
    }

    // Edit Diet Plan Form
    @GetMapping("/members/{memberId}/diet-plan/edit/{planId}")
    public String editDietPlanForm(@PathVariable("memberId") Long memberId,
                                   @PathVariable("planId") Long planId,
                                   Authentication auth,
                                   Model model,
                                   RedirectAttributes redirectAttributes) {
        Trainer trainer = getAuthenticatedTrainer(auth);
        if (trainer == null) return "redirect:/dashboard";

        Member member = memberService.findById(memberId).orElse(null);
        if (member == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Member not found.");
            return "redirect:/trainer/dashboard";
        }

        DietPlan plan = fitnessPlanService.getDietPlanById(planId).orElse(null);
        if (plan == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Diet plan not found.");
            return "redirect:/trainer/members/" + memberId + "/plans";
        }

        model.addAttribute("trainer", trainer);
        model.addAttribute("member", member);
        model.addAttribute("dietPlan", plan);
        model.addAttribute("latestProgress", progressService.getLatestProgress(memberId).orElse(null));
        model.addAttribute("isEdit", true);
        return "trainer/diet-plan-form";
    }

    // Save Diet Plan
    @PostMapping("/members/{memberId}/diet-plan/save")
    public String saveDietPlan(@PathVariable("memberId") Long memberId,
                               @Valid @ModelAttribute("dietPlan") DietPlan dietPlan,
                               BindingResult result,
                               Authentication auth,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        Trainer trainer = getAuthenticatedTrainer(auth);
        if (trainer == null) return "redirect:/dashboard";

        Member member = memberService.findById(memberId).orElse(null);
        if (member == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Member not found.");
            return "redirect:/trainer/dashboard";
        }

        if (result.hasErrors()) {
            model.addAttribute("trainer", trainer);
            model.addAttribute("member", member);
            model.addAttribute("latestProgress", progressService.getLatestProgress(memberId).orElse(null));
            model.addAttribute("isEdit", dietPlan.getId() != null);
            return "trainer/diet-plan-form";
        }

        boolean isUpdate = dietPlan.getId() != null;
        dietPlan.setMember(member);
        dietPlan.setTrainer(trainer);
        fitnessPlanService.saveDietPlan(dietPlan);

        String msg = isUpdate ? "Custom nutrition plan updated successfully for " + member.getFullName() + "!"
                              : "Custom nutrition plan created successfully for " + member.getFullName() + "!";
        redirectAttributes.addFlashAttribute("successMessage", msg);
        return "redirect:/trainer/members/" + memberId + "/plans";
    }

    // Trainer logs progress assessment for member
    @PostMapping("/members/{memberId}/progress/save")
    public String saveProgressByTrainer(@PathVariable("memberId") Long memberId,
                                        @ModelAttribute("newProgress") MemberProgress progress,
                                        Authentication auth,
                                        RedirectAttributes redirectAttributes) {
        try {
            progressService.logProgress(
                    memberId,
                    progress.getWeightKg(),
                    progress.getHeightCm(),
                    progress.getBodyType(),
                    progress.getWorkoutHoursPerWeek(),
                    progress.getBodyFatPercentage(),
                    progress.getTargetGoal(),
                    progress.getNotes(),
                    "TRAINER"
            );
            redirectAttributes.addFlashAttribute("successMessage", "Progress assessment logged successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error saving progress: " + e.getMessage());
        }
        return "redirect:/trainer/members/" + memberId + "/plans";
    }

    // Plans list for trainer
    @GetMapping("/plans")
    public String trainerAllPlans(Authentication auth, Model model) {
        Trainer trainer = getAuthenticatedTrainer(auth);
        if (trainer == null) return "redirect:/dashboard";

        model.addAttribute("trainer", trainer);
        model.addAttribute("workoutPlans", fitnessPlanService.getWorkoutPlansForTrainer(trainer.getId()));
        model.addAttribute("dietPlans", fitnessPlanService.getDietPlansForTrainer(trainer.getId()));
        return "trainer/plans-list";
    }

    // Resources & Equipment console for trainer
    @GetMapping("/resources")
    public String trainerResources(Authentication auth, Model model) {
        Trainer trainer = getAuthenticatedTrainer(auth);
        if (trainer == null) return "redirect:/dashboard";

        model.addAttribute("trainer", trainer);
        model.addAttribute("resources", resourceService.findAll());
        model.addAttribute("myBookings", resourceService.getTrainerBookings(trainer.getId()));
        model.addAttribute("todayBookings", resourceService.getTodayBookings());
        model.addAttribute("members", trainer.getMembers());
        model.addAttribute("floorOccupancy", resourceService.getCurrentFloorOccupancy());
        model.addAttribute("maxFloorCapacity", resourceService.getMaxFloorCapacity());
        model.addAttribute("floorOccupancyPercentage", resourceService.getFloorOccupancyPercentage());

        return "trainer/resources";
    }

    // Trainer reserves equipment or studio pod
    @PostMapping("/resources/reserve")
    public String reserveResourceByTrainer(@RequestParam("resourceId") Long resourceId,
                                           @RequestParam(value = "memberId", required = false) Long memberId,
                                           @RequestParam(value = "bookingDate", required = false) @org.springframework.format.annotation.DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate bookingDate,
                                           @RequestParam("startTime") String startTimeStr,
                                           @RequestParam(value = "purpose", required = false) String purpose,
                                           Authentication auth,
                                           RedirectAttributes redirectAttributes) {
        Trainer trainer = getAuthenticatedTrainer(auth);
        if (trainer == null) return "redirect:/dashboard";

        try {
            java.time.LocalTime startTime = java.time.LocalTime.parse(startTimeStr);
            java.time.LocalTime endTime = startTime.plusMinutes(60);
            if (bookingDate == null) bookingDate = LocalDate.now();

            resourceService.bookResource(resourceId, memberId, trainer.getId(), bookingDate, startTime, endTime, purpose);
            redirectAttributes.addFlashAttribute("successMessage", "Resource reserved successfully for your session!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Could not reserve resource: " + e.getMessage());
        }

        return "redirect:/trainer/resources";
    }

    // Trainer/Staff reports equipment maintenance status
    @PostMapping("/resources/{id}/status")
    public String updateResourceStatus(@PathVariable("id") Long resourceId,
                                       @RequestParam("status") String status,
                                       Authentication auth,
                                       RedirectAttributes redirectAttributes) {
        Trainer trainer = getAuthenticatedTrainer(auth);
        if (trainer == null) return "redirect:/dashboard";

        try {
            resourceService.updateResourceStatus(resourceId, status);
            redirectAttributes.addFlashAttribute("successMessage", "Resource status updated to " + status + ".");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating status: " + e.getMessage());
        }

        return "redirect:/trainer/resources";
    }

    @PostMapping("/resources/cancel/{id}")
    public String cancelTrainerReservation(@PathVariable("id") Long bookingId,
                                           Authentication auth,
                                           RedirectAttributes redirectAttributes) {
        Trainer trainer = getAuthenticatedTrainer(auth);
        if (trainer == null) return "redirect:/dashboard";

        try {
            boolean isAdmin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            resourceService.cancelBooking(bookingId, null, trainer.getId(), isAdmin);
            redirectAttributes.addFlashAttribute("successMessage", "Reservation cancelled successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/trainer/resources";
    }

    private Trainer getAuthenticatedTrainer(Authentication auth) {
        if (auth == null) return null;
        return userService.findTrainerByUsername(auth.getName()).orElse(null);
    }
}
