package com.gym.management.controller;

import com.gym.management.model.ClassBooking;
import com.gym.management.model.GymClass;
import com.gym.management.model.Member;
import com.gym.management.service.GymClassService;
import com.gym.management.service.MemberService;
import com.gym.management.service.TrainerService;
import com.gym.management.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/classes")
public class GymClassController {

    private final GymClassService gymClassService;
    private final TrainerService trainerService;
    private final UserService userService;
    private final MemberService memberService;

    public GymClassController(GymClassService gymClassService,
                              TrainerService trainerService,
                              UserService userService,
                              MemberService memberService) {
        this.gymClassService = gymClassService;
        this.trainerService = trainerService;
        this.userService = userService;
        this.memberService = memberService;
    }

    @GetMapping
    public String listClasses(Authentication auth, Model model) {
        List<GymClass> classes = gymClassService.findAll();
        model.addAttribute("classes", classes);

        // Check if logged in user is a member, collect their booked class IDs
        Set<Long> bookedClassIds = new HashSet<>();
        if (auth != null) {
            userService.findMemberByUsername(auth.getName()).ifPresent(member -> {
                List<ClassBooking> bookings = gymClassService.getMemberBookings(member.getId());
                Set<Long> ids = bookings.stream()
                        .filter(b -> "CONFIRMED".equalsIgnoreCase(b.getStatus()))
                        .map(b -> b.getGymClass().getId())
                        .collect(Collectors.toSet());
                bookedClassIds.addAll(ids);
                model.addAttribute("currentMember", member);
            });
        }
        model.addAttribute("bookedClassIds", bookedClassIds);

        return "classes/list";
    }

    @GetMapping("/new")
    public String showNewClassForm(Model model) {
        model.addAttribute("gymClass", new GymClass());
        model.addAttribute("trainers", trainerService.findAll());
        return "classes/form";
    }

    @PostMapping("/save")
    public String saveClass(@Valid @ModelAttribute("gymClass") GymClass gymClass,
                            BindingResult bindingResult,
                            Model model,
                            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("trainers", trainerService.findAll());
            return "classes/form";
        }
        gymClassService.save(gymClass);
        redirectAttributes.addFlashAttribute("successMessage", "Fitness class scheduled successfully!");
        return "redirect:/classes";
    }

    @GetMapping("/edit/{id}")
    public String showEditClassForm(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        return gymClassService.findById(id).map(c -> {
            model.addAttribute("gymClass", c);
            model.addAttribute("trainers", trainerService.findAll());
            return "classes/form";
        }).orElseGet(() -> {
            redirectAttributes.addFlashAttribute("errorMessage", "Class not found.");
            return "redirect:/classes";
        });
    }

    @GetMapping("/view/{id}")
    public String viewClass(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        return gymClassService.findById(id).map(c -> {
            model.addAttribute("gymClass", c);
            model.addAttribute("bookings", gymClassService.getClassBookings(id));
            model.addAttribute("members", memberService.findAll());
            return "classes/view";
        }).orElseGet(() -> {
            redirectAttributes.addFlashAttribute("errorMessage", "Class not found.");
            return "redirect:/classes";
        });
    }

    @GetMapping("/delete/{id}")
    public String deleteClass(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        gymClassService.deleteById(id);
        redirectAttributes.addFlashAttribute("successMessage", "Class schedule deleted.");
        return "redirect:/classes";
    }

    @PostMapping("/book/{id}")
    public String bookClass(@PathVariable("id") Long id,
                            @RequestParam(value = "memberId", required = false) Long specificMemberId,
                            @RequestParam(value = "redirectUrl", defaultValue = "/classes") String redirectUrl,
                            Authentication auth,
                            RedirectAttributes redirectAttributes) {
        try {
            Long memberId = null;
            boolean isAdminOrStaff = auth != null && auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_TRAINER"));

            if (isAdminOrStaff && specificMemberId != null) {
                memberId = specificMemberId;
            } else if (auth != null) {
                Member currentMember = userService.findMemberByUsername(auth.getName()).orElse(null);
                if (currentMember != null) {
                    memberId = currentMember.getId();
                }
            }

            if (memberId == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Unable to determine member to enroll.");
                return "redirect:" + redirectUrl;
            }

            gymClassService.bookClass(id, memberId, LocalDate.now());
            redirectAttributes.addFlashAttribute("successMessage", "You have successfully booked this class!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:" + redirectUrl;
    }

    @PostMapping("/cancel-booking/{id}")
    public String cancelBooking(@PathVariable("id") Long id,
                                @RequestParam(value = "redirectUrl", defaultValue = "/classes") String redirectUrl,
                                Authentication auth,
                                RedirectAttributes redirectAttributes) {
        try {
            Long requestingMemberId = null;
            boolean isAdminOrStaff = false;
            if (auth != null) {
                isAdminOrStaff = auth.getAuthorities().stream()
                        .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_TRAINER"));
                if (!isAdminOrStaff) {
                    Member currentMember = userService.findMemberByUsername(auth.getName()).orElse(null);
                    if (currentMember != null) {
                        requestingMemberId = currentMember.getId();
                    }
                }
            }
            gymClassService.cancelBooking(id, requestingMemberId, isAdminOrStaff);
            redirectAttributes.addFlashAttribute("successMessage", "Class booking cancelled.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:" + redirectUrl;
    }
}
