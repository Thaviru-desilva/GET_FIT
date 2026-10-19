package com.gym.management.controller;

import com.gym.management.model.Trainer;
import com.gym.management.service.TrainerService;
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

    public TrainerController(TrainerService trainerService) {
        this.trainerService = trainerService;
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
        if (bindingResult.hasErrors()) {
            return "trainers/form";
        }
        trainerService.save(trainer);
        redirectAttributes.addFlashAttribute("successMessage", "Trainer saved successfully!");
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
        trainerService.deleteById(id);
        redirectAttributes.addFlashAttribute("successMessage", "Trainer deleted successfully.");
        return "redirect:/trainers";
    }
}
