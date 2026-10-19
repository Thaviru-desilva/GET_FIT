package com.gym.management.controller;

import com.gym.management.model.Member;
import com.gym.management.service.*;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/members")
public class MemberController {

    private final MemberService memberService;
    private final TrainerService trainerService;
    private final SubscriptionService subscriptionService;
    private final AttendanceService attendanceService;
    private final MemberProgressService progressService;
    private final FitnessPlanService fitnessPlanService;

    public MemberController(MemberService memberService,
                            TrainerService trainerService,
                            SubscriptionService subscriptionService,
                            AttendanceService attendanceService,
                            MemberProgressService progressService,
                            FitnessPlanService fitnessPlanService) {
        this.memberService = memberService;
        this.trainerService = trainerService;
        this.subscriptionService = subscriptionService;
        this.attendanceService = attendanceService;
        this.progressService = progressService;
        this.fitnessPlanService = fitnessPlanService;
    }

    @GetMapping
    public String listMembers(@RequestParam(value = "q", required = false) String query, Model model) {
        model.addAttribute("members", memberService.search(query));
        model.addAttribute("query", query);
        return "members/list";
    }

    @GetMapping("/new")
    public String showNewMemberForm(Model model) {
        model.addAttribute("member", new Member());
        model.addAttribute("trainers", trainerService.findAll());
        return "members/form";
    }

    @PostMapping("/save")
    public String saveMember(@Valid @ModelAttribute("member") Member member,
                             BindingResult bindingResult,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("trainers", trainerService.findAll());
            return "members/form";
        }
        memberService.save(member);
        redirectAttributes.addFlashAttribute("successMessage", "Member saved successfully!");
        return "redirect:/members";
    }

    @GetMapping("/edit/{id}")
    public String showEditMemberForm(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        return memberService.findById(id).map(member -> {
            model.addAttribute("member", member);
            model.addAttribute("trainers", trainerService.findAll());
            return "members/form";
        }).orElseGet(() -> {
            redirectAttributes.addFlashAttribute("errorMessage", "Member not found.");
            return "redirect:/members";
        });
    }

    @GetMapping("/view/{id}")
    public String viewMemberProfile(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        return memberService.findById(id).map(member -> {
            model.addAttribute("member", member);
            model.addAttribute("subscriptions", subscriptionService.findByMemberId(id));
            model.addAttribute("attendances", attendanceService.getMemberAttendance(id));
            model.addAttribute("progressList", progressService.getProgressHistory(id));
            model.addAttribute("latestProgress", progressService.getLatestProgress(id).orElse(null));
            model.addAttribute("workoutPlans", fitnessPlanService.getWorkoutPlansForMember(id));
            model.addAttribute("dietPlans", fitnessPlanService.getDietPlansForMember(id));
            return "members/view";
        }).orElseGet(() -> {
            redirectAttributes.addFlashAttribute("errorMessage", "Member not found.");
            return "redirect:/members";
        });
    }

    @GetMapping("/delete/{id}")
    public String deleteMember(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        memberService.deleteById(id);
        redirectAttributes.addFlashAttribute("successMessage", "Member deleted successfully.");
        return "redirect:/members";
    }
}
