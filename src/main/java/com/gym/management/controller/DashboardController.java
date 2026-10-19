package com.gym.management.controller;

import com.gym.management.service.DashboardService;
import com.gym.management.service.MemberService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    private final DashboardService dashboardService;
    private final MemberService memberService;

    public DashboardController(DashboardService dashboardService, MemberService memberService) {
        this.dashboardService = dashboardService;
        this.memberService = memberService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("stats", dashboardService.getDashboardStats());
        model.addAttribute("members", memberService.findAll());
        return "dashboard";
    }
}
