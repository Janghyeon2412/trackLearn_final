package com.multi.tracklearn.controller;

import com.multi.tracklearn.domain.Category;
import com.multi.tracklearn.domain.Goal;
import com.multi.tracklearn.domain.User;

import com.multi.tracklearn.dto.*;
import com.multi.tracklearn.repository.GoalRepository;
import com.multi.tracklearn.service.CategoryService;
import com.multi.tracklearn.service.GoalService;
import com.multi.tracklearn.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/goals")
public class GoalController {

    private final GoalService goalService;
    private final CategoryService categoryService;
    private final UserService userService;
    private final GoalRepository goalRepository;


    @PostMapping
    public ResponseEntity<?> createGoal(@AuthenticationPrincipal String email, @RequestBody GoalCreateDTO goalCreateDTO) {
        goalService.createGoal(email, goalCreateDTO);

        return ResponseEntity.ok("목표가 저장되었습니다.");
    }

    @GetMapping
    public ResponseEntity<List<GoalListDTO>> getGoals(@AuthenticationPrincipal String email) {
        return ResponseEntity.ok(goalService.getGoals(email));
    }


    @GetMapping("/today")
    public ResponseEntity<List<TodayGoalDTO>> getTodayGoals(Authentication authentication) {
        String email = (String) authentication.getPrincipal();
        return ResponseEntity.ok(goalService.getTodayGoals(email));
    }




    @PatchMapping("/{goalId}")
    public ResponseEntity<?> updateGoal(@AuthenticationPrincipal String email, @PathVariable Long goalId, @RequestBody GoalUpdateDTO goalUpdateDTO) {
        goalService.updateGoal(email, goalId, goalUpdateDTO);
        return ResponseEntity.ok("목표가 수정되었습니다.");
    }



    @DeleteMapping("/{goalId}")
    public ResponseEntity<String> deleteGoal(@AuthenticationPrincipal String email, @PathVariable Long goalId) {
        goalService.softDeleteGoal(email, goalId);  // 서비스에서 soft delete 처리
        return ResponseEntity.ok("목표가 삭제되었습니다");
    }


    @PatchMapping("/{goalId}/complete")
    public ResponseEntity<?> completeGoal(@AuthenticationPrincipal String email, @PathVariable Long goalId) {
        goalService.completeGoal(email, goalId);
        return ResponseEntity.ok("목표 완료 처리됨");
    }




    @GetMapping("/paged")
    public ResponseEntity<List<GoalListDTO>> getGoalsPaged(
            @AuthenticationPrincipal String email,
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(goalService.getGoalsPaged(email, cursor, size));
    }



    @GetMapping("/list")
    public ResponseEntity<List<GoalListDTO>> getPagedGoals(
            @AuthenticationPrincipal String email,
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "5") int size) {

        if (email == null || email.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        User user = userService.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));

        List<Goal> goals;

        if (cursor == null) {
            // 최신순 정렬된 첫 페이지
            goals = goalRepository.findByUserAndDeletedFalseOrderByIdDesc(user, PageRequest.of(0, size));
        } else {
            // cursor보다 작은 ID 중 최신순 정렬
            goals = goalRepository.findByUserAndIdLessThanAndDeletedFalseOrderByIdDesc(user, cursor, PageRequest.of(0, size));
        }

        List<GoalListDTO> result = goals.stream()
                .map(GoalListDTO::fromEntity)
                .toList();

        return ResponseEntity.ok(result);
    }



}
