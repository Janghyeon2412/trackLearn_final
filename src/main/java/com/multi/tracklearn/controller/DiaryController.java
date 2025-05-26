package com.multi.tracklearn.controller;

import com.multi.tracklearn.auth.JwtUserAuthentication;
import com.multi.tracklearn.dto.DiarySaveDTO;
import com.multi.tracklearn.dto.GoalListDTO;
import com.multi.tracklearn.dto.ResetPasswordChangeDTO;
import com.multi.tracklearn.dto.TodayGoalDTO;
import com.multi.tracklearn.service.DiaryService;
import com.multi.tracklearn.service.GoalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/diary")
@RequiredArgsConstructor
public class DiaryController {

    private final GoalService goalService;
    private final DiaryService diaryService;

    @GetMapping("/today-goals")
    public ResponseEntity<List<TodayGoalDTO>> getTodayGoals(Authentication authentication) {
        if (authentication == null || !(authentication instanceof JwtUserAuthentication auth)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String email = auth.getEmail();
        List<TodayGoalDTO> goals = goalService.getTodayGoals(email);
        return ResponseEntity.ok(goals);
    }

    @PostMapping("/diaries")
    public ResponseEntity<?> saveDiary(
            Authentication authentication,
            @Valid @RequestBody DiarySaveDTO diarySaveDTO,
            BindingResult bindingResult
    ) {
        if (authentication == null || !(authentication instanceof JwtUserAuthentication auth)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String email = auth.getEmail();
        diaryService.saveDiary(email, diarySaveDTO);
        return ResponseEntity.ok("일지 저장이 완료되었습니다");
    }


}
