package com.multi.tracklearn.controller;

import com.multi.tracklearn.dto.LatestFeedbackDTO;
import com.multi.tracklearn.dto.NextScheduleDTO;
import com.multi.tracklearn.dto.TodayGoalDTO;
import com.multi.tracklearn.dto.WeeklyStatsDTO;
import com.multi.tracklearn.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

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
}
