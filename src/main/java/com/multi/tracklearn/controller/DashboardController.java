package com.multi.tracklearn.controller;

import com.multi.tracklearn.domain.User;
import com.multi.tracklearn.dto.*;
import com.multi.tracklearn.service.DashboardService;
import com.multi.tracklearn.service.GoalService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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
    public ResponseEntity<List<TodayGoalDTO>> getTodayGoals(Authentication authentication) {
        String email = extractEmail(authentication);
        if (email == null) return ResponseEntity.status(401).build();
        List<TodayGoalDTO> result = dashboardService.getTodayGoals(email);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/stats")
    public ResponseEntity<WeeklyStatsDTO> getWeeklyStats(Authentication authentication) {
        String email = extractEmail(authentication);
        if (email == null) return ResponseEntity.status(401).build();
        WeeklyStatsDTO stats = dashboardService.getWeeklyStats(email);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/latest-feedbacks")
    public ResponseEntity<List<LatestFeedbackDTO>> getLatestFeedbacks(Authentication authentication) {
        String email = extractEmail(authentication);
        if (email == null) return ResponseEntity.status(401).build();
        List<LatestFeedbackDTO> result = dashboardService.getLatestFeedbacks(email);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/next-schedule")
    public ResponseEntity<List<NextScheduleDTO>> getNextSchedule(Authentication authentication) {
        String email = extractEmail(authentication);
        if (email == null) return ResponseEntity.status(401).build();
        List<NextScheduleDTO> result = dashboardService.getNextSchedule(email);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/calendar")
    public ResponseEntity<List<CalendarGoalDTO>> getCalendarGoals(
            Authentication authentication,
            @RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam("end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {

        String email = extractEmail(authentication);
        if (email == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        List<CalendarGoalDTO> goals = dashboardService.getCalendarGoals(email, start, end);
        return ResponseEntity.ok(goals);
    }

    @GetMapping("/gpt-summary")
    public ResponseEntity<GptStatsDTO> getGptStats(Authentication authentication) {
        String email = extractEmail(authentication);
        if (email == null) return ResponseEntity.status(401).build();
        GptStatsDTO result = dashboardService.getGptSummary(email);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/monthly-stats")
    public ResponseEntity<MonthlyStatsDTO> getMonthlyStats(
            Authentication authentication,
            @RequestParam int year,
            @RequestParam int month) {

        String email = extractEmail(authentication);
        if (email == null) return ResponseEntity.status(401).build();

        MonthlyStatsDTO dto = dashboardService.getMonthlyStats(email, year, month);
        return ResponseEntity.ok(dto);
    }


    // 공통
    private String extractEmail(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) return null;
        Object principal = authentication.getPrincipal();
        if (principal instanceof User user) {
            return user.getEmail();
        }
        return null;
    }
}
