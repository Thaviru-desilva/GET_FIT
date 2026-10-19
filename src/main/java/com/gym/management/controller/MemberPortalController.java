package com.gym.management.controller;

import com.gym.management.model.ClassBooking;
import com.gym.management.model.Member;
import com.gym.management.model.MemberProgress;
import com.gym.management.model.Subscription;
import com.gym.management.service.*;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Controller
@RequestMapping("/member")
public class MemberPortalController {

    private final UserService userService;
    private final SubscriptionService subscriptionService;
    private final GymClassService gymClassService;
    private final AttendanceService attendanceService;
    private final MemberProgressService progressService;
    private final FitnessPlanService fitnessPlanService;
    private final GymResourceService resourceService;

    public MemberPortalController(UserService userService,
                                  SubscriptionService subscriptionService,
                                  GymClassService gymClassService,
                                  AttendanceService attendanceService,
                                  MemberProgressService progressService,
                                  FitnessPlanService fitnessPlanService,
                                  GymResourceService resourceService) {
        this.userService = userService;
        this.subscriptionService = subscriptionService;
        this.gymClassService = gymClassService;
        this.attendanceService = attendanceService;
        this.progressService = progressService;
        this.fitnessPlanService = fitnessPlanService;
        this.resourceService = resourceService;
    }

    @GetMapping("/portal")
    public String memberPortal(Authentication auth, Model model) {
        if (auth == null) {
            return "redirect:/login";
        }

        Member member = userService.findMemberByUsername(auth.getName()).orElse(null);
        if (member == null) {
            return "redirect:/dashboard";
        }

        List<Subscription> subscriptions = subscriptionService.findByMemberId(member.getId());
        Subscription activeSub = subscriptions.stream()
                .filter(s -> !s.isExpired())
                .findFirst()
                .orElse(null);

        long daysRemaining = 0;
        if (activeSub != null && activeSub.getEndDate() != null) {
            daysRemaining = Math.max(0, ChronoUnit.DAYS.between(LocalDate.now(), activeSub.getEndDate()));
        }

        List<ClassBooking> myBookings = gymClassService.getMemberBookings(member.getId());

        model.addAttribute("member", member);
        model.addAttribute("activeSubscription", activeSub);
        model.addAttribute("daysRemaining", daysRemaining);
        model.addAttribute("bookings", myBookings);
        model.addAttribute("attendances", attendanceService.getMemberAttendance(member.getId()));

        // Fitness & Diet Plans and Progress
        model.addAttribute("activeWorkoutPlan", fitnessPlanService.getActiveWorkoutPlanForMember(member.getId()).orElse(null));
        model.addAttribute("workoutPlans", fitnessPlanService.getWorkoutPlansForMember(member.getId()));
        model.addAttribute("activeDietPlan", fitnessPlanService.getActiveDietPlanForMember(member.getId()).orElse(null));
        model.addAttribute("dietPlans", fitnessPlanService.getDietPlansForMember(member.getId()));
        model.addAttribute("progressList", progressService.getProgressHistory(member.getId()));
        model.addAttribute("latestProgress", progressService.getLatestProgress(member.getId()).orElse(null));

        // Limited Gym Resources & Equipment
        model.addAttribute("resources", resourceService.findAll());
        model.addAttribute("resourceBookings", resourceService.getMemberBookings(member.getId()));
        model.addAttribute("floorOccupancy", resourceService.getCurrentFloorOccupancy());
        model.addAttribute("maxFloorCapacity", resourceService.getMaxFloorCapacity());
        model.addAttribute("floorOccupancyPercentage", resourceService.getFloorOccupancyPercentage());

        MemberProgress newProgress = new MemberProgress();
        progressService.getLatestProgress(member.getId()).ifPresent(lp -> {
            newProgress.setHeightCm(lp.getHeightCm());
            newProgress.setBodyType(lp.getBodyType());
            newProgress.setTargetGoal(lp.getTargetGoal());
            newProgress.setWorkoutHoursPerWeek(lp.getWorkoutHoursPerWeek());
        });
        model.addAttribute("newProgress", newProgress);

        return "member/portal";
    }

    @PostMapping("/progress/save")
    public String saveMemberProgress(@ModelAttribute("newProgress") MemberProgress progress,
                                     Authentication auth,
                                     RedirectAttributes redirectAttributes) {
        if (auth == null) {
            return "redirect:/login";
        }

        Member member = userService.findMemberByUsername(auth.getName()).orElse(null);
        if (member == null) {
            return "redirect:/dashboard";
        }

        try {
            progressService.logProgress(
                    member.getId(),
                    progress.getWeightKg(),
                    progress.getHeightCm(),
                    progress.getBodyType(),
                    progress.getWorkoutHoursPerWeek(),
                    progress.getBodyFatPercentage(),
                    progress.getTargetGoal(),
                    progress.getNotes(),
                    "MEMBER"
            );
            redirectAttributes.addFlashAttribute("successMessage", "Your body metrics and progress update have been recorded successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error logging progress: " + e.getMessage());
        }

        return "redirect:/member/portal";
    }

    @PostMapping("/resources/reserve")
    public String reserveGymResource(@org.springframework.web.bind.annotation.RequestParam("resourceId") Long resourceId,
                                     @org.springframework.web.bind.annotation.RequestParam(value = "bookingDate", required = false) @org.springframework.format.annotation.DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate bookingDate,
                                     @org.springframework.web.bind.annotation.RequestParam("startTime") String startTimeStr,
                                     @org.springframework.web.bind.annotation.RequestParam(value = "purpose", required = false) String purpose,
                                     Authentication auth,
                                     RedirectAttributes redirectAttributes) {
        if (auth == null) return "redirect:/login";

        Member member = userService.findMemberByUsername(auth.getName()).orElse(null);
        if (member == null) return "redirect:/dashboard";

        try {
            java.time.LocalTime startTime = java.time.LocalTime.parse(startTimeStr);
            java.time.LocalTime endTime = startTime.plusMinutes(60);
            if (bookingDate == null) bookingDate = LocalDate.now();

            resourceService.bookResource(resourceId, member.getId(), null, bookingDate, startTime, endTime, purpose);
            redirectAttributes.addFlashAttribute("successMessage", "Equipment reserved successfully for your workout session!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Could not reserve equipment: " + e.getMessage());
        }

        return "redirect:/member/portal";
    }

    @PostMapping("/resources/cancel/{id}")
    public String cancelResourceReservation(@org.springframework.web.bind.annotation.PathVariable("id") Long bookingId,
                                            Authentication auth,
                                            RedirectAttributes redirectAttributes) {
        if (auth == null) return "redirect:/login";

        try {
            resourceService.cancelBooking(bookingId);
            redirectAttributes.addFlashAttribute("successMessage", "Equipment reservation cancelled successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error cancelling reservation: " + e.getMessage());
        }

        return "redirect:/member/portal";
    }
}

