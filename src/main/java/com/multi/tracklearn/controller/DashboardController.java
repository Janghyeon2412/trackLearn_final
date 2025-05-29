package com.multi.tracklearn.controller;

import com.multi.tracklearn.dto.*;
import com.multi.tracklearn.service.DashboardService;
import com.multi.tracklearn.service.GoalService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;
    private final GoalService goalService;

    @GetMapping("/today-goals")
    public  ResponseEntity<List<TodayGoalDTO>> getTodayGoals(@AuthenticationPrincipal String email) {
        if (email == null || email.isBlank()) {
            return ResponseEntity.status(401).build(); // 인증 누락
        }

        List<TodayGoalDTO> result = dashboardService.getTodayGoals(email);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/stats")
    public ResponseEntity<WeeklyStatsDTO> getWeeklyStats(@AuthenticationPrincipal String email) {
        if (email == null || email.isBlank()) {
            return ResponseEntity.status(401).build();
        }
        WeeklyStatsDTO stats = dashboardService.getWeeklyStats(email);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/latest-feedbacks")
    public ResponseEntity<List<LatestFeedbackDTO>> getLatestFeedbacks(@AuthenticationPrincipal String email) {
        if (email == null || email.isBlank()) {
            return ResponseEntity.status(401).build();
        }
        List<LatestFeedbackDTO> result = dashboardService.getLatestFeedbacks(email);
        return ResponseEntity.ok(result);
    }


    @GetMapping("/next-schedule")
    public ResponseEntity<List<NextScheduleDTO>> getNextSchedule(@AuthenticationPrincipal String email) {
        if (email == null || email.isBlank()) {
            return ResponseEntity.status(401).build();
        }

        List<NextScheduleDTO> result = dashboardService.getNextSchedule(email);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/calendar")
    public ResponseEntity<List<CalendarGoalDTO>> getCalendarGoals(
            @AuthenticationPrincipal String email,
            @RequestParam("start") String start,
            @RequestParam("end") String end
    ) {
        // 날짜만 잘라서 LocalDate로 변환
        LocalDate startDate = LocalDate.parse(start.substring(0, 10));
        LocalDate endDate = LocalDate.parse(end.substring(0, 10));

        List<CalendarGoalDTO> goals = goalService.getCalendarGoals(email, startDate, endDate);
        return ResponseEntity.ok(goals);
    }


}
