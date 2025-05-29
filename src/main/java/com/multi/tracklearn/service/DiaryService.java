package com.multi.tracklearn.service;


import com.multi.tracklearn.domain.Diary;
import com.multi.tracklearn.domain.Goal;
import com.multi.tracklearn.domain.GoalLog;
import com.multi.tracklearn.domain.User;
import com.multi.tracklearn.dto.DiaryDetailDTO;
import com.multi.tracklearn.dto.DiaryEditDTO;
import com.multi.tracklearn.dto.DiaryListDTO;
import com.multi.tracklearn.dto.DiarySaveDTO;
import com.multi.tracklearn.repository.DiaryRepository;
import com.multi.tracklearn.repository.GoalLogRepository;
import com.multi.tracklearn.repository.GoalRepository;
import com.multi.tracklearn.repository.UserRepository;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DiaryService {

    private final UserService userService;
    private final UserRepository userRepository;
    private final DiaryRepository diaryRepository;
    private final GoalLogRepository goalLogRepository;
    private final GoalRepository goalRepository;

    @Transactional
    public void saveDiary(String email, DiarySaveDTO diarySaveDTO) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new IllegalArgumentException("사용자를 찾을 수 없습니다.");
        }

        Diary diary;

        if (diarySaveDTO.getDiaryId() != null) {
            // 수정 모드
            diary = diaryRepository.findById(diarySaveDTO.getDiaryId())
                    .orElseThrow(() -> new IllegalArgumentException("일지를 찾을 수 없습니다."));

            if (!diary.getUser().getId().equals(user.getId())) {
                throw new AccessDeniedException("수정 권한이 없습니다.");
            }

            diary.setUpdatedAt(LocalDateTime.now());
            diary.setModifiedPerson(user.getNickname());

        } else {
            // 작성 모드
            diary = new Diary();
            diary.setUser(user);
            diary.setCreatedAt(LocalDateTime.now());
            diary.setCreatedPerson(user.getNickname());

            // ✅ 연관 goalLog 설정: 체크된 목표 중 첫 번째를 diary에 연결
            List<Long> completedGoalIds = diarySaveDTO.getCompletedGoalIds();
            if (completedGoalIds != null && !completedGoalIds.isEmpty()) {
                diary.setGoalLogIds(completedGoalIds);  // ✅ goalLogIds 전체를 직접 설정
            }

        }

        diary.setTitle(diarySaveDTO.getTitle());
        diary.setContent(diarySaveDTO.getContent());

        // 최대 30자로 잘라야 DB와 일치
        String summary = generateSummary(diarySaveDTO.getContent(), 30);
        if (summary.length() > 30) {
            summary = summary.substring(0, 30);
        }
        diary.setSummary(summary);




        diary.setSatisfaction(diarySaveDTO.getSatisfaction());
        diary.setStudyTime(diarySaveDTO.getStudyTime());
        diary.setDate(LocalDate.now());
        diary.setRetrospectives(
                diarySaveDTO.getRetrospectives() != null && !diarySaveDTO.getRetrospectives().isEmpty()
                        ? diarySaveDTO.getRetrospectives()
                        : new ArrayList<>()
        );

        diaryRepository.save(diary);

        // 체크된 목표 처리
        List<Long> logIds = diarySaveDTO.getCompletedGoalIds();
        if (logIds != null && !logIds.isEmpty()) {
            List<GoalLog> logs = goalLogRepository.findAllById(logIds);
            for (GoalLog log : logs) {
                log.markChecked();
                goalLogRepository.save(log);
            }

            // 목표 달성률 재계산
            Set<Long> goalIds = logs.stream()
                    .map(log -> log.getGoal().getId())
                    .collect(Collectors.toSet());

            goalIds.forEach(this::updateGoalProgress);
        }
    }

    // 요약 생성 함수
    private String generateSummary(String content, int maxLength) {
        if (content == null) return "";
        // 1. 줄바꿈, 탭 제거
        String flat = content.replaceAll("[\\n\\r\\t]+", " ");
        // 2. 연속 공백 제거
        flat = flat.replaceAll(" +", " ").trim();
        // 3. 길이 자르기
        return flat.length() > maxLength ? flat.substring(0, maxLength) + "..." : flat;
    }


    private void updateGoalProgress(Long goalId) {
        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 목표"));

        int total = goalLogRepository.countByGoal(goal);
        int completed = goalLogRepository.countByGoalAndIsCheckedIsTrue(goal);

        int rate = total == 0 ? 0 : (int) ((completed / (double) total) * 100);
        goal.updateProgress(rate);
        goalRepository.save(goal);
        goalRepository.flush();
    }


    @Transactional(readOnly = true)
    public List<DiaryListDTO> getDiaryList(String email) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new IllegalArgumentException("사용자를 찾을 수 없습니다.");
        }

        List<Diary> diaries = diaryRepository.findByUserIdOrderByDateDesc(user.getId());

        return diaries.stream()
                .map(diary -> new DiaryListDTO(
                        diary.getId(),
                        diary.getTitle(),
                        diary.getSummary(),
                        diary.getDate().toString(),
                        "",  // 태그 아직 없으면 빈 문자열로
                        diary.getSatisfaction(),
                        diary.isFavorite(),
                        diary.getContent(),
                        diary.getGoalLogIds()
                ))
                .collect(Collectors.toList());

    }

    @Transactional(readOnly = true)
    public Page<DiaryListDTO> getDiariesSorted(String email, String sort, int page, int size) {
        User user = userService.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Diary> diaryPage;

        switch (sort) {
            case "oldest":
                diaryPage = diaryRepository.findByUserIdOrderByCreatedAtAsc(user.getId(), pageable);
                break;
            case "rating":
                diaryPage = diaryRepository.findByUserIdOrderBySatisfactionDesc(user.getId(), pageable);
                break;
            case "favorite":
                diaryPage = diaryRepository.findByUserIdOrderByIsFavoriteDescCreatedAtDesc(user.getId(), pageable);
                break;
            case "latest":
            default:
                diaryPage = diaryRepository.findByUserIdOrderByCreatedAtDesc(user.getId(), pageable);
                break;
        }

        return diaryPage.map(diary -> new DiaryListDTO(
                diary.getId(),
                diary.getTitle(),
                diary.getSummary(),
                diary.getDate().toString(),
                "", // tags
                diary.getSatisfaction(),
                diary.isFavorite(),
                diary.getContent(),
                diary.getGoalLogIds()
        ));


    }


    @Transactional
    public void updateFavoriteStatus(String email, Long diaryId, boolean value) {
        User user = userRepository.findByEmail(email);
        if (user == null) throw new IllegalArgumentException("사용자를 찾을 수 없습니다.");

        Diary diary = diaryRepository.findById(diaryId)
                .orElseThrow(() -> new IllegalArgumentException("일지를 찾을 수 없습니다."));

        if (!diary.getUser().getId().equals(user.getId())) {
            throw new SecurityException("해당 일지에 접근할 권한이 없습니다.");
        }

        diary.setFavorite(value);
    }



    @Transactional(readOnly = true)
    public DiaryEditDTO prepareEditFormByDiaryId(Long diaryId, String email) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new IllegalArgumentException("사용자를 찾을 수 없습니다.");
        }

        Diary diary = diaryRepository.findById(diaryId)
                .orElseThrow(() -> new IllegalArgumentException("일지를 찾을 수 없습니다."));

        if (!diary.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("수정 권한이 없습니다.");
        }

        List<GoalLog> goalLogs = goalLogRepository.findByDateAndUserId(diary.getDate(), user.getId());
        if (goalLogs.isEmpty()) {
            throw new IllegalStateException("해당 날짜에 연결된 목표가 없습니다.");
        }

        return DiaryEditDTO.fromEntity(diary, goalLogs);
    }




    @Transactional
    public void updateDiaryByDiaryId(Long diaryId, DiaryEditDTO dto, String email) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new IllegalArgumentException("사용자를 찾을 수 없습니다.");
        }

        Diary diary = diaryRepository.findById(diaryId)
                .orElseThrow(() -> new IllegalArgumentException("수정할 일지를 찾을 수 없습니다."));

        if (!diary.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("수정 권한이 없습니다.");
        }

        diary.setTitle(dto.getTitle());
        diary.setContent(dto.getContent());
        diary.setStudyTime(dto.getStudyTime());
        diary.setSatisfaction(dto.getSatisfaction());

        // ✅ [추가] 체크된 목표 ID 리스트 다시 저장
        if (dto.getGoalLogIds() != null && !dto.getGoalLogIds().isEmpty()) {
            diary.setGoalLogIds(dto.getGoalLogIds());
        }

        // ✅ 회고 리스트
        diary.setRetrospectives(
                dto.getRetrospectives() != null
                        ? new ArrayList<>(dto.getRetrospectives())
                        : new ArrayList<>()
        );

        diary.setUpdatedAt(LocalDateTime.now());
        diary.setModifiedPerson(user.getNickname());

        diaryRepository.save(diary);
    }




    @Transactional(readOnly = true)
    public DiaryEditDTO prepareEditForm(Long goalLogId, String email) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new IllegalArgumentException("사용자를 찾을 수 없습니다.");
        }

        GoalLog goalLog = goalLogRepository.findById(goalLogId)
                .orElseThrow(() -> new IllegalArgumentException("목표 일정을 찾을 수 없습니다."));

        List<GoalLog> goalLogs = goalLogRepository.findByDateAndUserId(goalLog.getDate(), user.getId());

        // ✅ 변경된 쿼리 사용
        Optional<Diary> optionalDiary = diaryRepository.findByUserAndGoalLogId(user, goalLogId);

        System.out.println("goalLogId: " + goalLogId);
        System.out.println("userId: " + user.getId());
        System.out.println("goalLogs: " + goalLogs.size());
        System.out.println("optionalDiary.isPresent(): " + optionalDiary.isPresent());

        if (optionalDiary.isPresent()) {
            Diary diary = optionalDiary.get();

            if (!diary.getUser().getId().equals(user.getId())) {
                throw new AccessDeniedException("수정 권한이 없습니다.");
            }

            return DiaryEditDTO.fromEntity(diary, goalLogs);
        }

        if (!goalLog.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("권한이 없습니다.");
        }

        return DiaryEditDTO.fromGoalLogs(goalLogs);
    }

    // 읽기 전용 상세보기
    public DiaryDetailDTO getDiaryDetail(Long diaryId, String email) {
        User user = userService.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));

        Diary diary = diaryRepository.findById(diaryId)
                .orElseThrow(() -> new IllegalArgumentException("일지 없음"));

        if (!diary.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("조회 권한 없음");
        }

        List<GoalLog> goalLogs = goalLogRepository.findByDiaryId(diaryId);
        return DiaryDetailDTO.fromEntity(diary, goalLogs);
    }


}
