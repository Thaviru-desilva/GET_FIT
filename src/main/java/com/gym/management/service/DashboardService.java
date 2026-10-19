package com.gym.management.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class DashboardService {

    private final MemberService memberService;
    private final TrainerService trainerService;
    private final MembershipPlanService planService;
    private final SubscriptionService subscriptionService;
    private final AttendanceService attendanceService;
    private final GymClassService gymClassService;

    public DashboardService(MemberService memberService,
                            TrainerService trainerService,
                            MembershipPlanService planService,
                            SubscriptionService subscriptionService,
                            AttendanceService attendanceService,
                            GymClassService gymClassService) {
        this.memberService = memberService;
        this.trainerService = trainerService;
        this.planService = planService;
        this.subscriptionService = subscriptionService;
        this.attendanceService = attendanceService;
        this.gymClassService = gymClassService;
    }

    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalMembers", memberService.countTotalMembers());
        stats.put("activeMembers", memberService.countActiveMembers());
        stats.put("totalTrainers", trainerService.count());
        stats.put("activePlans", planService.count());
        stats.put("totalClasses", gymClassService.count());
        stats.put("activeSubscriptions", subscriptionService.countActiveSubscriptions());
        stats.put("totalRevenue", subscriptionService.getTotalRevenue());
        stats.put("todayCheckIns", attendanceService.countTodayCheckIns());
        stats.put("expiringSoon", subscriptionService.getExpiringSoon());
        stats.put("recentAttendance", attendanceService.getRecentAttendance());
        stats.put("recentSubscriptions", subscriptionService.getRecentSubscriptions());
        stats.put("classes", gymClassService.findAll());
        return stats;
    }
}
