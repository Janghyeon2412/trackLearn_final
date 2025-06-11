package com.multi.tracklearn.controller;

import com.multi.tracklearn.domain.Diary;
import com.multi.tracklearn.domain.GoalLog;
import com.multi.tracklearn.domain.User;
import com.multi.tracklearn.dto.MyPageInfoDTO;
import com.multi.tracklearn.dto.RecentGoalDTO;
import com.multi.tracklearn.repository.DiaryRepository;
import com.multi.tracklearn.repository.GoalLogRepository;
import com.multi.tracklearn.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/mypage")
@RequiredArgsConstructor
public class MyPageController {

    private final UserService userService;
    private final DiaryRepository diaryRepository;
    private final GoalLogRepository goalLogRepository;

    @GetMapping("/info")
    public ResponseEntity<MyPageInfoDTO> getInfo(Authentication authentication) {
        User user = (User) authentication.getPrincipal();

        // 일지 통계
        List<Diary> diaries = diaryRepository.findByUserId(user.getId());
        int diaryCount = diaries.size();
        int totalStudyTime = diaries.stream().mapToInt(Diary::getStudyTime).sum();
        double avgSatisfaction = diaries.stream().mapToDouble(Diary::getSatisfaction).average().orElse(0.0);

        // 최근 달성 목표
        List<GoalLog> recentGoals = goalLogRepository.findTop5ByUserAndIsCheckedTrueOrderByDateDesc(user);
        List<RecentGoalDTO> goalDTOs = recentGoals.stream()
                .map(gl -> new RecentGoalDTO(gl.getGoal().getTitle(), gl.getGoal().getCategory().getName(), gl.getDate()))
                .toList();


        MyPageInfoDTO dto = new MyPageInfoDTO(
                user.getNickname(), user.getEmail(), user.getCreatedAt(),
                diaryCount, totalStudyTime, avgSatisfaction,
                goalDTOs // ✅ 이제 오류 없음
        );

        return ResponseEntity.ok(dto);
    }
}
