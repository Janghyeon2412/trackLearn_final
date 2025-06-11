package com.multi.tracklearn.controller;

import com.multi.tracklearn.auth.JwtUserAuthentication;
import com.multi.tracklearn.domain.Category;
import com.multi.tracklearn.domain.Goal;
import com.multi.tracklearn.domain.User;

import com.multi.tracklearn.dto.*;
import com.multi.tracklearn.repository.DiaryRepository;
import com.multi.tracklearn.repository.GoalRepository;
import com.multi.tracklearn.service.CategoryService;
import com.multi.tracklearn.service.DiaryService;
import com.multi.tracklearn.service.GoalService;
import com.multi.tracklearn.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/goals")
public class GoalController {

    private final GoalService goalService;
    private final CategoryService categoryService;
    private final UserService userService;
    private final GoalRepository goalRepository;
    private final DiaryService diaryService;

    @PostMapping
    public ResponseEntity<?> createGoal(Authentication authentication, @RequestBody GoalCreateDTO goalCreateDTO) {
        if (!(authentication instanceof JwtUserAuthentication auth)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        goalService.createGoal(auth.getEmail(), goalCreateDTO);
        return ResponseEntity.ok("목표가 저장되었습니다.");
    }

    @GetMapping
    public ResponseEntity<List<GoalListDTO>> getGoals(Authentication authentication) {
        if (!(authentication instanceof JwtUserAuthentication auth)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(goalService.getGoals(auth.getEmail()));
    }

    @GetMapping("/today")
    public ResponseEntity<List<TodayGoalDTO>> getTodayGoals(Authentication authentication) {
        if (!(authentication instanceof JwtUserAuthentication auth)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(goalService.getTodayGoals(auth.getEmail()));
    }

    @PatchMapping("/{goalId}")
    public ResponseEntity<?> updateGoal(Authentication authentication, @PathVariable Long goalId, @RequestBody GoalUpdateDTO goalUpdateDTO) {
        if (!(authentication instanceof JwtUserAuthentication auth)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        goalService.updateGoal(auth.getEmail(), goalId, goalUpdateDTO);
        return ResponseEntity.ok("목표가 수정되었습니다.");
    }

    @DeleteMapping("/{goalId}")
    public ResponseEntity<String> deleteGoal(Authentication authentication, @PathVariable Long goalId) {
        if (!(authentication instanceof JwtUserAuthentication auth)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        goalService.softDeleteGoal(auth.getEmail(), goalId);
        return ResponseEntity.ok("목표가 삭제되었습니다");
    }

    @PatchMapping("/{goalId}/complete")
    public ResponseEntity<?> completeGoal(Authentication authentication, @PathVariable Long goalId) {
        if (!(authentication instanceof JwtUserAuthentication auth)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        goalService.completeGoal(auth.getEmail(), goalId);
        return ResponseEntity.ok("목표 완료 처리됨");
    }

    @GetMapping("/paged")
    public ResponseEntity<List<GoalListDTO>> getGoalsPaged(
            Authentication authentication,
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "10") int size
    ) {
        if (!(authentication instanceof JwtUserAuthentication auth)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(goalService.getGoalsPaged(auth.getEmail(), cursor, size));
    }

    @GetMapping("/list")
    public ResponseEntity<List<GoalListDTO>> getPagedGoals(
            Authentication authentication,
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "5") int size) {

        if (!(authentication instanceof JwtUserAuthentication auth)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        User user = userService.findByEmail(auth.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));

        List<Goal> goals;

        if (cursor == null) {
            goals = goalRepository.findByUserAndDeletedFalseOrderByIdDesc(user, PageRequest.of(0, size));
        } else {
            goals = goalRepository.findByUserAndIdLessThanAndDeletedFalseOrderByIdDesc(user, cursor, PageRequest.of(0, size));
        }

        List<GoalListDTO> result = goals.stream()
                .map(GoalListDTO::fromEntity)
                .toList();

        return ResponseEntity.ok(result);
    }

    @GetMapping("/edit")
    public String editGoalForm(@RequestParam("goalLogId") Long goalLogId,
                               Authentication authentication,
                               Model model) {
        if (!(authentication instanceof JwtUserAuthentication auth)) {
            return "redirect:/login";
        }
        DiaryEditDTO dto = diaryService.prepareEditForm(goalLogId, auth.getEmail());
        model.addAttribute("editDTO", dto);
        return "goal-edit";
    }

    @GetMapping("/statistics")
    public ResponseEntity<List<GoalStatisticsDTO>> getGoalStatistics(
            Authentication authentication,
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        if (!(authentication instanceof JwtUserAuthentication auth)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        List<GoalStatisticsDTO> stats = goalService.getGoalStatistics(auth.getEmail(), startDate, endDate);
        return ResponseEntity.ok(stats);
    }
}
