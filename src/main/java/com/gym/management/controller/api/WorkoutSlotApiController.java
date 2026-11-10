package com.gym.management.controller.api;

import com.gym.management.dto.WorkoutSlotAvailabilityDto;
import com.gym.management.model.Member;
import com.gym.management.model.WorkoutFocus;
import com.gym.management.service.UserService;
import com.gym.management.service.WorkoutSlotService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/workout-slots")
public class WorkoutSlotApiController {

    private final WorkoutSlotService slotService;
    private final UserService userService;

    public WorkoutSlotApiController(WorkoutSlotService slotService, UserService userService) {
        this.slotService = slotService;
        this.userService = userService;
    }

    @GetMapping("/availability")
    public ResponseEntity<List<WorkoutSlotAvailabilityDto>> getAvailability(
            @RequestParam(value = "date", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
            @RequestParam(value = "focus", required = false, defaultValue = "LEGS") WorkoutFocus focus,
            Authentication auth) {

        if (date == null) {
            date = LocalDate.now();
        }

        Long memberId = null;
        if (auth != null && auth.isAuthenticated()) {
            memberId = userService.findMemberByUsername(auth.getName()).map(Member::getId).orElse(null);
        }

        List<WorkoutSlotAvailabilityDto> dtos = slotService.getSlotAvailability(date, focus, memberId);
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/capacity-check")
    public ResponseEntity<Map<String, Object>> checkCapacity(
            @RequestParam("date") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
            @RequestParam("time") String timeStr,
            @RequestParam("focus") WorkoutFocus focus) {

        LocalTime time = LocalTime.parse(timeStr);
        boolean available = slotService.checkSlotCapacity(date, time, focus);
        Map<WorkoutFocus, Integer> summary = slotService.getRemainingCapacityMap(date, time);

        return ResponseEntity.ok(Map.of(
                "available", available,
                "focus", focus.name(),
                "remainingForFocus", summary.get(focus),
                "capacityLimit", focus.getCapacity(),
                "allFocusSummary", summary
        ));
    }

    @GetMapping("/next-available")
    public ResponseEntity<Map<String, Object>> getNextAvailableSlot(
            @RequestParam(value = "date", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
            @RequestParam(value = "focus", required = false, defaultValue = "LEGS") WorkoutFocus focus) {

        if (date == null) date = LocalDate.now();
        Optional<LocalTime> nextSlot = slotService.findNextAvailableSlot(date, focus);

        return ResponseEntity.ok(Map.of(
                "available", nextSlot.isPresent(),
                "time", nextSlot.map(LocalTime::toString).orElse(""),
                "date", date.toString(),
                "focus", focus.name()
        ));
    }

    @PostMapping("/book-async")
    public ResponseEntity<Map<String, Object>> bookSlotAsync(
            @RequestParam("bookingDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate bookingDate,
            @RequestParam("startTime") String startTimeStr,
            @RequestParam("workoutFocus") WorkoutFocus focus,
            @RequestParam(value = "notes", required = false) String notes,
            Authentication auth) {

        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "Please log in to reserve equipment slots."));
        }

        Member member = userService.findMemberByUsername(auth.getName()).orElse(null);
        if (member == null) {
            return ResponseEntity.status(403).body(Map.of("success", false, "message", "Only registered athletes can book slots."));
        }

        try {
            LocalTime startTime = LocalTime.parse(startTimeStr);
            com.gym.management.model.WorkoutSlotBooking booking = slotService.bookSlot(member.getId(), bookingDate, startTime, focus, notes);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Slot reserved! Equipment access is guaranteed for " + focus.getDisplayName() + " at " + startTimeStr + ".",
                    "bookingId", booking.getId()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    @PostMapping("/cancel-async/{id}")
    public ResponseEntity<Map<String, Object>> cancelSlotAsync(
            @PathVariable("id") Long bookingId,
            Authentication auth) {

        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "Authentication required."));
        }

        Member member = userService.findMemberByUsername(auth.getName()).orElse(null);
        boolean isAdmin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        try {
            slotService.cancelBooking(bookingId, member != null ? member.getId() : null, isAdmin);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Workout slot cancelled. Equipment spot released."
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }
}
