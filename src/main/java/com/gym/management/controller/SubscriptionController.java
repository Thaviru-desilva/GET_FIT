package com.gym.management.controller;

import com.gym.management.model.PaymentStatus;
import com.gym.management.service.MemberService;
import com.gym.management.service.MembershipPlanService;
import com.gym.management.service.SubscriptionService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
@RequestMapping("/subscriptions")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;
    private final MemberService memberService;
    private final MembershipPlanService planService;

    public SubscriptionController(SubscriptionService subscriptionService,
                                  MemberService memberService,
                                  MembershipPlanService planService) {
        this.subscriptionService = subscriptionService;
        this.memberService = memberService;
        this.planService = planService;
    }

    @GetMapping
    public String listSubscriptions(Model model) {
        model.addAttribute("subscriptions", subscriptionService.findAll());
        return "subscriptions/list";
    }

    @GetMapping("/new")
    public String showNewSubscriptionForm(@RequestParam(value = "memberId", required = false) Long preselectedMemberId,
                                          Model model) {
        model.addAttribute("members", memberService.findAll());
        model.addAttribute("plans", planService.findActivePlans());
        model.addAttribute("preselectedMemberId", preselectedMemberId);
        model.addAttribute("paymentStatuses", PaymentStatus.values());
        model.addAttribute("today", LocalDate.now());
        return "subscriptions/form";
    }

    @PostMapping("/save")
    public String saveSubscription(@RequestParam("memberId") Long memberId,
                                   @RequestParam("planId") Long planId,
                                   @RequestParam(value = "startDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
                                   @RequestParam(value = "amountPaid", required = false) Double amountPaid,
                                   @RequestParam(value = "paymentStatus", defaultValue = "PAID") PaymentStatus paymentStatus,
                                   RedirectAttributes redirectAttributes) {
        try {
            subscriptionService.createSubscription(memberId, planId, startDate, amountPaid, paymentStatus);
            redirectAttributes.addFlashAttribute("successMessage", "Subscription activated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error activating subscription: " + e.getMessage());
        }
        return "redirect:/subscriptions";
    }

    @GetMapping("/cancel/{id}")
    public String cancelSubscription(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        subscriptionService.cancelSubscription(id);
        redirectAttributes.addFlashAttribute("successMessage", "Subscription cancelled.");
        return "redirect:/subscriptions";
    }
}
