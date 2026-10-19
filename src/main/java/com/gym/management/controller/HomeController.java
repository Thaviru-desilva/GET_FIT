package com.gym.management.controller;

import com.gym.management.service.GymClassService;
import com.gym.management.service.MembershipPlanService;
import com.gym.management.service.TrainerService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final MembershipPlanService planService;
    private final TrainerService trainerService;
    private final GymClassService gymClassService;

    public HomeController(MembershipPlanService planService,
                          TrainerService trainerService,
                          GymClassService gymClassService) {
        this.planService = planService;
        this.trainerService = trainerService;
        this.gymClassService = gymClassService;
    }

    @GetMapping({"/", "/home", "/index"})
    public String index(Model model) {
        model.addAttribute("plans", planService.findActivePlans());
        model.addAttribute("trainers", trainerService.findAll());
        model.addAttribute("classes", gymClassService.findAll());
        return "index";
    }
}
