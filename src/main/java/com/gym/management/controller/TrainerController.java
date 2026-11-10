package com.gym.management.controller;

import com.gym.management.model.Trainer;
import com.gym.management.service.TrainerService;
import com.gym.management.service.UserService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/trainers")
public class TrainerController {

    private final TrainerService trainerService;
    private final UserService userService;

    public TrainerController(TrainerService trainerService, UserService userService) {
        this.trainerService = trainerService;
        this.userService = userService;
    }

    @GetMapping
    public String listTrainers(Model model) {
        model.addAttribute("trainers", trainerService.findAll());
        return "trainers/list";
    }

    @GetMapping("/new")
    public String showNewTrainerForm(Model model) {
        model.addAttribute("trainer", new Trainer());
        return "trainers/form";
    }

    @PostMapping("/save")
    public String saveTrainer(@Valid @ModelAttribute("trainer") Trainer trainer,
                              BindingResult bindingResult,
                              RedirectAttributes redirectAttributes) {
        boolean isNew = (trainer.getId() == null);
        String cleanEmail = trainer.getEmail() != null ? trainer.getEmail().trim().toLowerCase() : "";

        if (isNew) {
            if (trainerService.existsByEmail(cleanEmail) || userService.existsByUsername(cleanEmail)) {
                bindingResult.rejectValue("email", "error.email", "This email address is already in use by another registered account.");
            }
        } else {
            if (trainerService.isEmailTakenByOtherTrainer(cleanEmail, trainer.getId())) {
                bindingResult.rejectValue("email", "error.email", "This email address is already assigned to another coach.");
            }
        }

        if (bindingResult.hasErrors()) {
            return "trainers/form";
        }
        Trainer saved = trainerService.save(trainer);
        if (isNew) {
            userService.createUserForTrainer(saved, "password123");
            redirectAttributes.addFlashAttribute("successMessage",
                    "Trainer added successfully! Portal account created: " + saved.getEmail() + " (Password: password123)");
        } else {
            userService.syncTrainerUserEmail(saved);
            redirectAttributes.addFlashAttribute("successMessage", "Trainer details updated successfully!");
        }
        return "redirect:/trainers";
    }

    @GetMapping("/edit/{id}")
    public String showEditTrainerForm(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        return trainerService.findById(id).map(trainer -> {
            model.addAttribute("trainer", trainer);
            return "trainers/form";
        }).orElseGet(() -> {
            redirectAttributes.addFlashAttribute("errorMessage", "Trainer not found.");
            return "redirect:/trainers";
        });
    }

    @GetMapping("/delete/{id}")
    public String deleteTrainer(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        userService.deleteUserByTrainerId(id);
        trainerService.deleteById(id);
        redirectAttributes.addFlashAttribute("successMessage", "Trainer deleted successfully.");
        return "redirect:/trainers";
    }
}
