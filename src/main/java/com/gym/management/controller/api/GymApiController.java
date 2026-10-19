package com.gym.management.controller.api;

import com.gym.management.model.Attendance;
import com.gym.management.model.Member;
import com.gym.management.model.MembershipPlan;
import com.gym.management.model.Trainer;
import com.gym.management.service.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class GymApiController {

    private final MemberService memberService;
    private final TrainerService trainerService;
    private final MembershipPlanService planService;
    private final SubscriptionService subscriptionService;
    private final AttendanceService attendanceService;
    private final DashboardService dashboardService;

    public GymApiController(MemberService memberService,
                            TrainerService trainerService,
                            MembershipPlanService planService,
                            SubscriptionService subscriptionService,
                            AttendanceService attendanceService,
                            DashboardService dashboardService) {
        this.memberService = memberService;
        this.trainerService = trainerService;
        this.planService = planService;
        this.subscriptionService = subscriptionService;
        this.attendanceService = attendanceService;
        this.dashboardService = dashboardService;
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        return ResponseEntity.ok(dashboardService.getDashboardStats());
    }

    @GetMapping("/members")
    public ResponseEntity<List<Member>> getMembers(@RequestParam(value = "q", required = false) String query) {
        return ResponseEntity.ok(memberService.search(query));
    }

    @GetMapping("/members/{id}")
    public ResponseEntity<Member> getMemberById(@PathVariable Long id) {
        return memberService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/members")
    public ResponseEntity<Member> createMember(@RequestBody Member member) {
        return ResponseEntity.ok(memberService.save(member));
    }

    @GetMapping("/trainers")
    public ResponseEntity<List<Trainer>> getTrainers() {
        return ResponseEntity.ok(trainerService.findAll());
    }

    @GetMapping("/plans")
    public ResponseEntity<List<MembershipPlan>> getPlans() {
        return ResponseEntity.ok(planService.findActivePlans());
    }

    @GetMapping("/attendance/today")
    public ResponseEntity<List<Attendance>> getTodayAttendance() {
        return ResponseEntity.ok(attendanceService.getTodayAttendance());
    }
}
