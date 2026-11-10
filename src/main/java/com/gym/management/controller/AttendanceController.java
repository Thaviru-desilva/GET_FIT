package com.gym.management.controller;

import com.gym.management.service.AttendanceService;
import com.gym.management.service.MemberService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/attendance")
public class AttendanceController {

    private final AttendanceService attendanceService;
    private final MemberService memberService;

    public AttendanceController(AttendanceService attendanceService, MemberService memberService) {
        this.attendanceService = attendanceService;
        this.memberService = memberService;
    }

    @GetMapping
    public String viewAttendance(Model model) {
        model.addAttribute("todayAttendance", attendanceService.getTodayAttendance());
        model.addAttribute("recentAttendance", attendanceService.getRecentAttendance());
        model.addAttribute("members", memberService.findAll());
        return "attendance/list";
    }

    @PostMapping("/check-in")
    public String checkInMember(@RequestParam("memberId") Long memberId,
                                @RequestParam(value = "notes", required = false) String notes,
                                RedirectAttributes redirectAttributes) {
        try {
            attendanceService.checkIn(memberId, notes);
            redirectAttributes.addFlashAttribute("successMessage", "Member checked in successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/attendance";
    }

    @GetMapping("/check-out/{id}")
    public String checkOutMember(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        attendanceService.checkOut(id);
        redirectAttributes.addFlashAttribute("successMessage", "Member checked out successfully!");
        return "redirect:/attendance";
    }

    @GetMapping("/delete/{id}")
    public String deleteAttendance(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        attendanceService.deleteById(id);
        redirectAttributes.addFlashAttribute("successMessage", "Attendance record deleted.");
        return "redirect:/attendance";
    }
}
