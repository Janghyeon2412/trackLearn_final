package com.multi.tracklearn.controller;

import com.multi.tracklearn.auth.JwtUserAuthentication;
import com.multi.tracklearn.domain.Diary;
import com.multi.tracklearn.domain.GoalLog;
import com.multi.tracklearn.domain.User;
import com.multi.tracklearn.dto.*;
import com.multi.tracklearn.repository.DiaryRepository;
import com.multi.tracklearn.repository.GoalLogRepository;
import com.multi.tracklearn.service.DiaryService;
import com.multi.tracklearn.service.GoalService;
import com.multi.tracklearn.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/diary")
@RequiredArgsConstructor
public class DiaryController {

    private final GoalService goalService;
    private final DiaryService diaryService;
    private final UserService userService;
    private final GoalLogRepository goalLogRepository;
    private final DiaryRepository diaryRepository;

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

        // ✅ 벨리데이션 에러 처리 추가
        if (bindingResult.hasErrors()) {
            List<String> errors = bindingResult.getAllErrors().stream()
                    .map(error -> error.getDefaultMessage())
                    .toList();
            return ResponseEntity.badRequest().body(errors);
        }

        String email = ((JwtUserAuthentication) authentication).getEmail();
        diaryService.saveDiary(email, diarySaveDTO);
        return ResponseEntity.ok("일지 저장이 완료되었습니다");
    }


    @GetMapping("/list")
    public ResponseEntity<Page<DiaryListDTO>> getDiaries(
            @AuthenticationPrincipal String email,
            @RequestParam(defaultValue = "latest") String sort,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "9") int size) {

        Page<DiaryListDTO> result = diaryService.getDiariesSorted(email, sort, page, size);
        return ResponseEntity.ok(result);
    }

    @PatchMapping("/{diaryId}/favorite")
    public ResponseEntity<?> toggleFavorite(
            @PathVariable Long diaryId,
            @RequestParam boolean value,
            @AuthenticationPrincipal String email
    ) {
        if (email == null || email.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        diaryService.updateFavoriteStatus(email, diaryId, value);
        return ResponseEntity.ok().build();
    }

    // ✅ 기존 일지 조회 (수정 폼 채우기용)
    @GetMapping("/{diaryId}")
    public ResponseEntity<?> getDiary(@PathVariable Long diaryId, Authentication authentication) {
        if (authentication == null || !(authentication instanceof JwtUserAuthentication auth)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String email = auth.getEmail();

        try {
            DiaryEditDTO dto = diaryService.prepareEditFormByDiaryId(diaryId, email);
            return ResponseEntity.ok(dto);
        } catch (IllegalArgumentException | AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // ✅ 기존 일지 수정
    @PutMapping("/{diaryId}")
    public ResponseEntity<?> updateDiary(@PathVariable Long diaryId,
                                         @Valid @RequestBody DiaryEditDTO dto,
                                         BindingResult bindingResult,
                                         Authentication authentication) {
        if (authentication == null || !(authentication instanceof JwtUserAuthentication auth)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if (bindingResult.hasErrors()) {
            List<String> errors = bindingResult.getAllErrors().stream()
                    .map(error -> error.getDefaultMessage())
                    .toList();
            return ResponseEntity.badRequest().body(errors);
        }

        String email = auth.getEmail();

        try {
            diaryService.updateDiaryByDiaryId(diaryId, dto, email);
            return ResponseEntity.ok("수정 완료");
        } catch (IllegalArgumentException | AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/detail/{diaryId}")
    public ResponseEntity<?> getDiaryDetail(@PathVariable Long diaryId, Authentication authentication) {
        if (authentication == null || !(authentication instanceof JwtUserAuthentication auth)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String email = auth.getEmail();

        try {
            DiaryDetailDTO dto = diaryService.getDiaryDetail(diaryId, email);
            return ResponseEntity.ok(dto);
        } catch (IllegalArgumentException | AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }



}
