package com.multi.tracklearn.controller;

import com.multi.tracklearn.auth.JwtUserAuthentication;
import com.multi.tracklearn.domain.Diary;
import com.multi.tracklearn.domain.GoalLog;
import com.multi.tracklearn.domain.User;
import com.multi.tracklearn.dto.*;
import com.multi.tracklearn.gpt.FeedbackService;
import com.multi.tracklearn.repository.DiaryRepository;
import com.multi.tracklearn.repository.GoalLogRepository;
import com.multi.tracklearn.repository.UserRepository;
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
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.*;

@RestController
@RequestMapping("/api/diary")
@RequiredArgsConstructor
@Validated
public class DiaryController {

    private final GoalService goalService;
    private final DiaryService diaryService;
    private final UserService userService;
    private final GoalLogRepository goalLogRepository;
    private final DiaryRepository diaryRepository;
    private final UserRepository userRepository;
    private final FeedbackService feedbackService;


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

        if (bindingResult.hasErrors()) {
            List<String> errors = bindingResult.getAllErrors().stream()
                    .map(error -> error.getDefaultMessage())
                    .toList();
            return ResponseEntity.badRequest().body(errors);
        }

        String email = auth.getEmail();

        try {
            diaryService.saveDiary(email, diarySaveDTO);
            return ResponseEntity.ok("일지 저장이 완료되었습니다");
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }


    @GetMapping("/list")
    public ResponseEntity<Page<DiaryListDTO>> getDiaries(
            Authentication authentication,
            @RequestParam(defaultValue = "latest") String sort,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "9") int size) {

        if (authentication == null || !(authentication instanceof JwtUserAuthentication auth)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String email = auth.getEmail();
        Page<DiaryListDTO> result = diaryService.getDiariesSorted(email, sort, page, size);
        return ResponseEntity.ok(result);
    }

    @PatchMapping("/{diaryId}/favorite")
    public ResponseEntity<?> toggleFavorite(
            @PathVariable Long diaryId,
            @RequestParam boolean value,
            Authentication authentication
    ) {
        if (authentication == null || !(authentication instanceof JwtUserAuthentication auth)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String email = auth.getEmail();
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
        } catch (NoSuchElementException e) {
            // 💥 이 부분 추가: 일지가 존재하지 않는 경우
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("해당 일지를 찾을 수 없습니다.");
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

    @GetMapping("/today-written")
    public ResponseEntity<?> checkTodayDiaryWritten(Authentication authentication) {
        if (authentication == null || !(authentication instanceof JwtUserAuthentication auth)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String email = auth.getEmail();
        boolean exists = diaryService.existsTodayDiaryByEmail(email);
        if (exists) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(Map.of("message", "이미 오늘 일지를 작성하셨습니다.")); // ✅ JSON 형태로 반환
        }
        return ResponseEntity.ok(Map.of("written", false));
    }


    @GetMapping("/by-goallog/{goalLogId}")
    public ResponseEntity<?> getDiaryPageByGoalLogId(
            @PathVariable Long goalLogId,
            Authentication authentication) {

        if (authentication == null || !(authentication instanceof JwtUserAuthentication auth)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String email = auth.getEmail();

        try {
            Optional<Diary> optionalDiary = diaryService.findDiaryByGoalLogId(goalLogId, email);
            if (optionalDiary.isPresent()) {
                Long diaryId = optionalDiary.get().getId();
                return ResponseEntity.ok("/diary/edit/" + diaryId);  // 이미 작성된 경우 -> 수정 페이지
            } else {
                return ResponseEntity.ok("/diary/write?goalLogId=" + goalLogId);  // 미작성 -> 작성 페이지
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("요청 처리 중 오류 발생: " + e.getMessage());
        }
    }

    @PostMapping("/gpt-feedback")
    public ResponseEntity<?> generateAndSaveFeedback(
            @RequestBody GptFeedbackRequestDTO dto,
            Authentication authentication
    ) {
        if (authentication == null || !(authentication instanceof JwtUserAuthentication auth)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String email = auth.getEmail();

        try {
            String feedbackText = diaryService.generateAndSaveFeedback(dto, email);
            return ResponseEntity.ok(feedbackText);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("GPT 피드백 생성 실패: " + e.getMessage());
        }
    }



    @PostMapping("/api/diary/{diaryId}/generate-feedback")
    public ResponseEntity<?> generateFeedback(@PathVariable Long diaryId, Authentication authentication) {
        if (authentication == null || !(authentication instanceof JwtUserAuthentication auth)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String email = auth.getEmail();

        try {
            String feedback = diaryService.generateFeedbackByDiaryId(diaryId, email);
            return ResponseEntity.ok(feedback);
        } catch (IllegalArgumentException | AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }



}
