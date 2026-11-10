package com.gym.management.controller;

import com.gym.management.dto.RegistrationDto;
import com.gym.management.model.MembershipPlan;
import com.gym.management.service.MembershipPlanService;
import com.gym.management.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.WebAttributes;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    private final UserService userService;
    private final MembershipPlanService planService;
    private final com.gym.management.service.MemberService memberService;

    public AuthController(UserService userService, MembershipPlanService planService, com.gym.management.service.MemberService memberService) {
        this.userService = userService;
        this.planService = planService;
        this.memberService = memberService;
    }

    @GetMapping("/login")
    public String loginPage(Authentication authentication,
                            @RequestParam(value = "error", required = false) String error,
                            @RequestParam(value = "logout", required = false) String logout,
                            @RequestParam(value = "registered", required = false) String registered,
                            HttpServletRequest request,
                            Model model) {
        // If already logged in, redirect to respective portal immediately
        if (authentication != null && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken)) {
            return "redirect:/default";
        }

        if (error != null) {
            String errorMsg = "Invalid username or password. Please verify your credentials.";
            HttpSession session = request.getSession(false);
            if (session != null) {
                Exception ex = (Exception) session.getAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
                if (ex instanceof DisabledException) {
                    errorMsg = "Your account is inactive. Please contact the front desk or renew your membership.";
                } else if (ex instanceof BadCredentialsException) {
                    errorMsg = "Incorrect email/username or password. Please try again.";
                }
            }
            model.addAttribute("errorMessage", errorMsg);
        }

        if (logout != null) {
            model.addAttribute("logoutMessage", "You have been safely signed out. See you next session!");
        }

        if (registered != null) {
            model.addAttribute("successMessage", "Account created successfully! Please sign in with your email and password.");
        }

        return "auth/login";
    }

    @GetMapping("/register")
    public String registerPage(Authentication authentication, Model model) {
        if (authentication != null && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken)) {
            return "redirect:/default";
        }
        model.addAttribute("registration", new RegistrationDto());
        model.addAttribute("plans", planService.findActivePlans());
        return "auth/register";
    }

    @PostMapping("/register")
    public String processRegistration(@Valid @ModelAttribute("registration") RegistrationDto dto,
                                      BindingResult bindingResult,
                                      Model model,
                                      RedirectAttributes redirectAttributes) {
        String cleanEmail = dto.getEmail() != null ? dto.getEmail().trim().toLowerCase() : "";

        if (dto.getPassword() != null && !dto.getPassword().equals(dto.getConfirmPassword())) {
            bindingResult.rejectValue("confirmPassword", "error.confirmPassword", "Passwords do not match.");
        }

        if (cleanEmail.isEmpty()) {
            bindingResult.rejectValue("email", "error.email", "Email address is required.");
        } else if (userService.existsByUsername(cleanEmail) || memberService.existsByEmail(cleanEmail)) {
            bindingResult.rejectValue("email", "error.email", "This email / Gmail address is already registered to another account. Please sign in or use a different email.");
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("plans", planService.findActivePlans());
            return "auth/register";
        }

        MembershipPlan selectedPlan = null;
        if (dto.getPlanId() != null) {
            selectedPlan = planService.findById(dto.getPlanId()).orElse(null);
        }

        userService.registerNewMember(dto, selectedPlan);
        redirectAttributes.addFlashAttribute("successMessage", "Welcome to Get U Fit! Your account is active. Please log in.");
        return "redirect:/login?registered=true";
    }

    @GetMapping("/access-denied")
    public String accessDenied(Authentication auth, Model model) {
        String portalLink = "/login";
        String portalName = "Sign In";

        if (auth != null) {
            for (GrantedAuthority authority : auth.getAuthorities()) {
                String role = authority.getAuthority();
                if ("ROLE_MEMBER".equals(role)) {
                    portalLink = "/member/portal";
                    portalName = "Athlete Portal";
                    break;
                } else if ("ROLE_TRAINER".equals(role)) {
                    portalLink = "/trainer/dashboard";
                    portalName = "Coach Portal";
                    break;
                } else if ("ROLE_ADMIN".equals(role)) {
                    portalLink = "/dashboard";
                    portalName = "Admin Dashboard";
                    break;
                }
            }
        }

        model.addAttribute("portalLink", portalLink);
        model.addAttribute("portalName", portalName);
        return "auth/access-denied";
    }

    @GetMapping("/default")
    public String defaultAfterLogin(Authentication authentication) {
        if (authentication == null) {
            return "redirect:/login";
        }

        for (GrantedAuthority authority : authentication.getAuthorities()) {
            if ("ROLE_ADMIN".equals(authority.getAuthority())) {
                return "redirect:/dashboard";
            } else if ("ROLE_TRAINER".equals(authority.getAuthority())) {
                return "redirect:/trainer/dashboard";
            } else if ("ROLE_MEMBER".equals(authority.getAuthority())) {
                return "redirect:/member/portal";
            }
        }

        return "redirect:/";
    }
}
