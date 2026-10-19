package com.gym.management.controller;

import com.gym.management.model.MembershipPlan;
import com.gym.management.service.MembershipPlanService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/plans")
public class MembershipPlanController {

    private final MembershipPlanService planService;

    public MembershipPlanController(MembershipPlanService planService) {
        this.planService = planService;
    }

    @GetMapping
    public String listPlans(Model model) {
        model.addAttribute("plans", planService.findAll());
        return "plans/list";
    }

    @GetMapping("/new")
    public String showNewPlanForm(Model model) {
        model.addAttribute("plan", new MembershipPlan());
        return "plans/form";
    }

    @PostMapping("/save")
    public String savePlan(@Valid @ModelAttribute("plan") MembershipPlan plan,
                           BindingResult bindingResult,
                           RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "plans/form";
        }
        planService.save(plan);
        redirectAttributes.addFlashAttribute("successMessage", "Membership plan saved successfully!");
        return "redirect:/plans";
    }

    @GetMapping("/edit/{id}")
    public String showEditPlanForm(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        return planService.findById(id).map(plan -> {
            model.addAttribute("plan", plan);
            return "plans/form";
        }).orElseGet(() -> {
            redirectAttributes.addFlashAttribute("errorMessage", "Plan not found.");
            return "redirect:/plans";
        });
    }

    @GetMapping("/delete/{id}")
    public String deletePlan(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        planService.deleteById(id);
        redirectAttributes.addFlashAttribute("successMessage", "Membership plan deleted successfully.");
        return "redirect:/plans";
    }
}
